package com.knappsack.swagger4springweb.parser


import java.lang.annotation.Annotation
import java.lang.reflect.{Field, Method, Type}

import com.knappsack.swagger4springweb.annotation.ApiExclude
import com.wordnik.swagger.annotations._
import com.wordnik.swagger.config._
import com.wordnik.swagger.core._
import com.wordnik.swagger.core.util._
import com.wordnik.swagger.model._
import com.wordnik.swagger.reader.{ClassReader, ClassReaderUtils}
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation._

import scala.collection.mutable.ListBuffer

trait SpringMVCApiReader extends ClassReader with ClassReaderUtils {

  private val LOGGER = LoggerFactory.getLogger(classOf[SpringMVCApiReader])
  val GenericTypeMapper = "([a-zA-Z\\.]*)<([a-zA-Z0-9\\.\\,\\s]*)>".r

  // decorates a Parameter based on annotations, returns None if param should be ignored
  def processParamAnnotations(mutable: MutableParameter, paramAnnotations: Array[Annotation]): Option[Parameter]

  // Finds the type of the subresource this method produces, in case it's a subresource locator
  // In case it's not a subresource locator the entity type is returned
  def findSubresourceType(method: Method): Class[_]

  def processDataType(paramType: Class[_], genericParamType: Type) = {
    paramType.getName match {
      case "[I" => "Array[int]"
      case "[Z" => "Array[boolean]"
      case "[D" => "Array[double]"
      case "[F" => "Array[float]"
      case "[J" => "Array[long]"
      case _ => {
        if(paramType.isArray) {
          "Array[%s]".format(paramType.getComponentType.getName)
        }
        else {
          genericParamType.toString match {
            case GenericTypeMapper(container, base) => {
              val qt = SwaggerTypes(base.split("\\.").last) match {
                case "object" => base
                case e: String => e
              }
              val b = ModelUtil.modelFromString(qt) match {
                case Some(e) => e._2.qualifiedType
                case None => qt
              }
              "%s[%s]".format(normalizeContainer(container), b)
            }
            case _ => paramType.getName
          }
        }
      }
    }
  }

  def normalizeContainer(str: String) = {
    if(str.indexOf(".List") >= 0) "List"
    else if(str.indexOf(".Set") >= 0) "Set"
    else {
      println("UNKNOWN TYPE: " + str)
      "UNKNOWN"
    }
  }

  def parseOperation(
                      method: Method,
                      apiOperation: ApiOperation,
                      apiResponses: List[ResponseMessage],
                      isDeprecated: String,
                      parentParams: List[Parameter],
                      parentMethods: ListBuffer[Method]
                      ) = {
//    val api = method.getAnnotation(classOf[Api])
    val responseClass = {
      if(apiOperation != null){
        val baseName = apiOperation.response.getName
        val output = apiOperation.responseContainer match {
          case "" => baseName
          case e: String => "%s[%s]".format(e, baseName)
        }
        output
      }
      else {
        if(!"javax.ws.rs.core.Response".equals(method.getReturnType.getCanonicalName))
          method.getReturnType.getName
        else
          "void"
      }
    }
    var paramAnnotations: Array[Array[java.lang.annotation.Annotation]] = null
    var paramTypes: Array[java.lang.Class[_]] = null
    var genericParamTypes: Array[java.lang.reflect.Type] = null

    if (parentMethods.isEmpty) {
      paramAnnotations = method.getParameterAnnotations
      paramTypes = method.getParameterTypes
      genericParamTypes = method.getGenericParameterTypes
    } else {
      paramAnnotations = parentMethods.map(pm => pm.getParameterAnnotations).reduceRight(_ ++ _) ++ method.getParameterAnnotations
      paramTypes = parentMethods.map(pm => pm.getParameterTypes).reduceRight(_ ++ _) ++ method.getParameterTypes
      genericParamTypes = parentMethods.map(pm => pm.getGenericParameterTypes).reduceRight(_ ++ _) ++ method.getGenericParameterTypes
    }

    val (nickname, produces, consumes, protocols, authorizations) = {
      if(apiOperation != null) {
        (
          (if(apiOperation.nickname != null && apiOperation.nickname != "")
            apiOperation.nickname
          else
            method.getName
            ),
          Option(apiOperation.produces) match {
            case Some(e) if(e != "") => e.split(",").map(_.trim).toList
            case _ => method.getAnnotation(classOf[RequestMapping]).produces() match {
              case e: Array[String] => e.toList
              case _ => List()
            }
          },
          Option(apiOperation.consumes) match {
            case Some(e) if(e != "") => e.split(",").map(_.trim).toList
            case _ => method.getAnnotation(classOf[RequestMapping]).consumes() match {
              case e: Array[String] => e.toList
              case _ => List()
            }
          },
          Option(apiOperation.protocols) match {
            case Some(e) if(e != "") => e.split(",").map(_.trim).toList
            case _ => List()
          },
          Option(apiOperation.authorizations) match {
            case Some(e) => (for(a <- e) yield {
              val scopes = (for(s <- a.scopes) yield com.wordnik.swagger.model.AuthorizationScope(s.scope, s.description)).toArray
              new com.wordnik.swagger.model.Authorization(a.value, scopes)
            }).toList
            case _ => List()
          })
      }
      else(method.getName, List(), List(), List(), List())
    }
    val params = parentParams ++ (for((annotations, paramType, genericParamType) <- (paramAnnotations, paramTypes, genericParamTypes).zipped.toList) yield {
      if(annotations.length > 0) {
        val param = new MutableParameter
        param.dataType = processDataType(paramType, genericParamType)
        processParamAnnotations(param, annotations)
      }
      else None
    }).flatten.toList

    val implicitParams = {
      val returnType = method.getReturnType
      LOGGER.debug("checking for implicits")
      Option(method.getAnnotation(classOf[ApiImplicitParams])) match {
        case Some(e) => {
          (for(param <- e.value) yield {
            LOGGER.debug("processing " + param)
            val allowableValues = toAllowableValues(param.allowableValues)
            Parameter(
              name = param.name,
              description = Option(readString(param.value)),
              defaultValue = Option(param.defaultValue).filter(_.trim.nonEmpty),
              required = param.required,
              allowMultiple = param.allowMultiple,
              dataType = param.dataType,
              allowableValues = allowableValues,
              paramType = param.paramType,
              paramAccess = Option(param.access).filter(_.trim.nonEmpty))
          }).toList
        }
        case _ => List()
      }
    }

    val (summary, notes, position) = {
      if(apiOperation != null) (apiOperation.value, apiOperation.notes, apiOperation.position)
      else ("","",0)
    }

    Operation(
      method = parseHttpMethod(method, apiOperation),
      summary = summary,
      notes = notes,
      responseClass = responseClass,
      nickname = nickname,
      position = position,
      produces = produces,
      consumes = consumes,
      protocols = protocols,
      authorizations = authorizations,
      parameters = params ++ implicitParams,
      responseMessages = apiResponses,
      `deprecated` = Option(isDeprecated))
  }

  def readMethod(method: Method, parentParams: List[Parameter], parentMethods: ListBuffer[Method]): Option[Operation] = {
    val apiOperation = method.getAnnotation(classOf[ApiOperation])
    val responseAnnotation = method.getAnnotation(classOf[ApiResponses])
    val apiResponses = {
      if(responseAnnotation == null) List()
      else (for(response <- responseAnnotation.value) yield {
        val apiResponseClass = {
          if(response.response != classOf[Void])
            Some(response.response.getName)
          else None
        }
        ResponseMessage(response.code, response.message, apiResponseClass)}
        ).toList
    }
    val isDeprecated = Option(method.getAnnotation(classOf[Deprecated])).map(m => "true").getOrElse(null)

    //Don't process methods that are marked with ApiExclude or are synthetic (in the case of Java lambda expressions)
    val hidden = if(method.getAnnotation(classOf[ApiExclude]) != null || method.isSynthetic) true
    else  if(apiOperation != null) apiOperation.hidden
    else false

    if(!hidden) Some(parseOperation(method, apiOperation, apiResponses, isDeprecated, parentParams, parentMethods))
    else None
  }

  def appendOperation(endpoint: String, path: String, op: Operation, operations: ListBuffer[Tuple3[String, String, ListBuffer[Operation]]]) = {
    operations.filter(op => op._1 == endpoint) match {
      case e: ListBuffer[Tuple3[String, String, ListBuffer[Operation]]] if(e.size > 0) => e.head._3 += op
      case _ => operations += Tuple3(endpoint, path, new ListBuffer[Operation]() ++= List(op))
    }
  }

  def read(docRoot: String, cls: Class[_], config: SwaggerConfig): Option[ApiListing] = {
    readRecursive(docRoot, "", cls, config, new ListBuffer[Tuple3[String, String, ListBuffer[Operation]]], new ListBuffer[Method])
  }

  var ignoredRoutes: Set[String] = Set()

  def ignoreRoutes = ignoredRoutes

  def readRecursive(
                     docRoot: String,
                     parentPath: String, cls: Class[_],
                     config: SwaggerConfig,
                     operations: ListBuffer[Tuple3[String, String, ListBuffer[Operation]]],
                     parentMethods: ListBuffer[Method]): Option[ApiListing] = {
    val api = cls.getAnnotation(classOf[Api])
    if(api == null || cls.getAnnotation(classOf[ApiExclude]) != null) return None

    val pathAnnotation = cls.getAnnotation(classOf[RequestMapping])

    val r = Option(api) match {
      case Some(api) => api.value
      case None => Option(pathAnnotation) match {
        case Some(p) => p.value()(0)
        case None => null
      }
    }
    if(r != null && !ignoreRoutes.contains(r)) {
//      var resourcePath = addLeadingSlash(r)
      val position = Option(api) match {
        case Some(api) => api.position
        case None => 0
      }
      val (consumes, produces, protocols, description) = {
        if(api != null){
          (Option(api.consumes) match {
            case Some(e) if(e != "") => e.split(",").map(_.trim).toList
            case _ => cls.getAnnotation(classOf[RequestMapping]).consumes() match {
              case e: Array[String] => e.toList
              case _ => List()
            }
          },
            Option(api.produces) match {
              case Some(e) if(e != "") => e.split(",").map(_.trim).toList
              case _ => cls.getAnnotation(classOf[RequestMapping]).produces() match {
                case e: Array[String] => e.toList
                case _ => List()
              }
            },
            Option(api.protocols) match {
              case Some(e) if(e != "") => e.split(",").map(_.trim).toList
              case _ => List()
            },
            api.description match {
              case e: String if(e != "") => Some(e)
              case _ => None
            }
            )}
        else ((List(), List(), List(), None))
      }
      // look for method-level annotated properties
      val parentParams: List[Parameter] = (for(field <- getAllFields(cls))
      yield {
        // only process fields with @ApiParam, @QueryParam, @HeaderParam, @PathParam
        if(field.getAnnotation(classOf[RequestParam]) != null || field.getAnnotation(classOf[RequestHeader]) != null ||
          field.getAnnotation(classOf[RequestHeader]) != null || field.getAnnotation(classOf[PathVariable]) != null ||
          field.getAnnotation(classOf[ApiParam]) != null) {
          val param = new MutableParameter
          param.dataType = field.getType.getName
          Option (field.getAnnotation(classOf[ApiParam])) match {
            case Some(annotation) => toAllowableValues(annotation.allowableValues)
            case _ =>
          }
          val annotations = field.getAnnotations
          processParamAnnotations(param, annotations)
        }
        else None
      }
        ).flatten.toList

      for(method <- cls.getDeclaredMethods) {
        val returnType = findSubresourceType(method)
        var path = ""
        if(method.getAnnotation(classOf[RequestMapping]) != null) {
          val paths = method.getAnnotation(classOf[RequestMapping]).value()
          if(paths.size > 0) {
            path = paths(0)
          }
        }
//      val endpoint = (parentPath + pathFromMethod(method)).replace("//", "/")
        val endpoint = parentPath + api.value + pathFromMethod(method)
        Option(returnType.getAnnotation(classOf[Api])) match {
          case Some(e) => {
            val root = docRoot + api.value + pathFromMethod(method)
            parentMethods += method
            readRecursive(root, endpoint, returnType, config, operations, parentMethods)
            parentMethods -= method
          }
          case _ => {
            readMethod(method, parentParams, parentMethods) match {
              case Some(op) => appendOperation(endpoint, path, op, operations)
              case None => None
            }
          }
        }
      }
      // sort them by min position in the operations
      val s = (for(op <- operations) yield {
        (op, op._3.map(_.position).toList.min)
      }).sortWith(_._2 < _._2).toList
      val orderedOperations = new ListBuffer[Tuple3[String, String, ListBuffer[Operation]]]
      s.foreach(op => {
        val ops = op._1._3.sortWith(_.position < _.position)
        orderedOperations += Tuple3(op._1._1, op._1._2, ops)
      })
      val apis = (for ((endpoint, resourcePath, operationList) <- orderedOperations) yield {
        val orderedOperations = new ListBuffer[Operation]
        operationList.sortWith(_.position < _.position).foreach(e => orderedOperations += e)
        ApiDescription(
          addLeadingSlash(endpoint),
          None,
          orderedOperations.toList)
      }).toList
      val models = ModelUtil.modelsFromApis(apis)
      Some(ApiListing (
        apiVersion = config.apiVersion,
        swaggerVersion = config.swaggerVersion,
        basePath = config.basePath,
//        resourcePath = addLeadingSlash(api.value),
        resourcePath = addLeadingSlash(docRoot),
        apis = ModelUtil.stripPackages(apis),
        models = models,
        description = description,
        produces = produces,
        consumes = consumes,
        protocols = protocols,
        position = position)
      )
    }
    else None
  }

  def getAllFields(cls: Class[_]): List[Field] = {
    var fields = cls.getDeclaredFields.toList
    if (cls.getSuperclass != null) {
      fields = getAllFields(cls.getSuperclass) ++ fields
    }
    fields
  }

  def pathFromMethod(method: Method): String = {
    val requestMapping = method.getAnnotation(classOf[org.springframework.web.bind.annotation.RequestMapping])
    if(requestMapping != null) {
      val requestMappingValues = requestMapping.value
        if(requestMappingValues.length > 0)
          return requestMapping.value()(0)
    }
    ""
  }

  def parseApiParamAnnotation(param: MutableParameter, annotation: ApiParam) {
    param.name = readString(annotation.name, param.name)
    param.description = Option(readString(annotation.value))
    param.defaultValue = Option(readString(annotation.defaultValue))

    try {
      param.allowableValues = toAllowableValues(annotation.allowableValues)
    } catch {
      case e: Exception =>
        LOGGER.error("Allowable values annotation problem in method for parameter " + param.name)
    }
    param.required = annotation.required
    param.allowMultiple = annotation.allowMultiple
    param.paramAccess = Option(readString(annotation.access))
  }

  def readString(value: String, defaultValue: String = null, ignoreValue: String = null): String = {
    if (defaultValue != null && defaultValue.trim.length > 0) defaultValue
    else if (value == null) null
    else if (value.trim.length == 0) null
    else if (ignoreValue != null && value.equals(ignoreValue)) null
    else value.trim
  }

  def parseHttpMethod(method: Method, op: ApiOperation): String = {
    if (op != null && op.httpMethod() != null && op.httpMethod().trim().length() > 0)
      op.httpMethod().trim
    else {
      val requestMappingAnnotation = method.getAnnotation(classOf[RequestMapping])
      val requestMapping = if(requestMappingAnnotation != null && !requestMappingAnnotation.method().isEmpty) requestMappingAnnotation.method()(0) else "GET"
      if(RequestMethod.GET.equals(requestMapping)) "GET"
      else if(RequestMethod.DELETE.equals(requestMapping)) "DELETE"
      else if(RequestMethod.PATCH.equals(requestMapping)) "PATCH"
      else if(RequestMethod.POST.equals(requestMapping)) "POST"
      else if(RequestMethod.PUT.equals(requestMapping)) "PUT"
      else if(RequestMethod.HEAD.equals(requestMapping)) "HEAD"
      else if(RequestMethod.OPTIONS.equals(requestMapping)) "OPTIONS"
      else null
    }
  }

  def addLeadingSlash(e: String): String = {
    e.startsWith("/") match {
      case true => e
      case false => "/" + e
    }
  }
}


class MutableParameter(param: Parameter) {
  var name: String = _
  var description: Option[String] = None
  var defaultValue: Option[String] = None
  var required: Boolean = _
  var allowMultiple: Boolean = false
  var dataType: String = _
  var allowableValues: AllowableValues = AnyAllowableValues
  var paramType: String = _
  var paramAccess: Option[String] = None

  if(param != null) {
    this.name = param.name
    this.description = param.description
    this.defaultValue = param.defaultValue
    this.required = param.required
    this.allowMultiple = param.allowMultiple
    this.dataType = param.dataType
    this.allowableValues = param.allowableValues
    this.paramType = param.paramType
    this.paramAccess = param.paramAccess
  }

  def this() = this(null)

  def asParameter() = {
    Parameter(name,
      description,
      defaultValue,
      required,
      allowMultiple,
      dataType,
      allowableValues,
      paramType,
      paramAccess)
  }
}
