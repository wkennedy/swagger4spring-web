package com.knappsack.swagger4springweb.parser

import org.springframework.web.bind.annotation._
import org.springframework.context.ApplicationContext

import com.wordnik.swagger.model._
import com.wordnik.swagger.annotations._
import com.wordnik.swagger.core.ApiValues._

import java.lang.annotation.Annotation

class SpringApiReader() extends SpringMVCApiReader {

  override def processParamAnnotations(mutable: MutableParameter, paramAnnotations: Array[Annotation]): Option[Parameter] = {
    var shouldIgnore = false
    for (pa <- paramAnnotations) {
      pa match {
        case apiParam: ApiParam => parseApiParamAnnotation(mutable, apiParam)
        case wsParam: RequestParam => {
          mutable.name = readString(wsParam.value, mutable.name)
          mutable.paramType = readString(TYPE_QUERY, mutable.paramType)
        }
        case wsParam: PathVariable => {
          mutable.name = readString(wsParam.value, mutable.name)
          mutable.required = true
          mutable.paramType = readString(TYPE_PATH, mutable.paramType)
        }
          //not supported
//        case wsParam: MatrixParam => {
//          docParam.name = readString(wsParam.value, docParam.name)
//          docParam.paramType = readString(TYPE_MATRIX, docParam.paramType)
//        }
        case wsParam: RequestHeader => {
          mutable.name = readString(wsParam.value, mutable.name)
          mutable.paramType = readString(TYPE_HEADER, mutable.paramType)
        }
        case wsParam: ModelAttribute => {
          mutable.name = readString(wsParam.value, mutable.name)
          mutable.paramType = readString(TYPE_FORM, mutable.paramType)
        }
        case wsParam: CookieValue => {
          mutable.name = readString(wsParam.value, mutable.name)
          mutable.paramType = readString(TYPE_COOKIE, mutable.paramType)
        }
        case wsParam: ApplicationContext => shouldIgnore = true
        case _ =>
      }
    }
    if(!shouldIgnore) {
      if(mutable.paramType == null) {
        mutable.paramType = TYPE_BODY
        mutable.name = TYPE_BODY
      }
      Some(mutable.asParameter)
    }
    else None
  }

//  def parseHttpMethod(method: Method, apiOperation: ApiOperation): String = {
//    if (apiOperation.httpMethod() != null && apiOperation.httpMethod().trim().length() > 0)
//      apiOperation.httpMethod().trim()
//    else {
//      val requestMapping = method.getAnnotation(classOf[org.springframework.web.bind.annotation.RequestMapping])
//      val requestMethod = requestMapping.method()(0)
//      if(requestMethod.equals(RequestMethod.GET)) ApiMethodType.GET
//      if(requestMethod.equals(RequestMethod.POST)) ApiMethodType.POST
//      if(requestMethod.equals(RequestMethod.PUT)) ApiMethodType.PUT
//      if(requestMethod.equals(RequestMethod.DELETE)) ApiMethodType.DELETE
//      if(requestMethod.equals(RequestMethod.HEAD)) ApiMethodType.HEAD
//
//      null
//    }
//  }

}
