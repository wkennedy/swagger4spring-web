package com.wordnik.swagger.converter

import java.lang.annotation.Annotation
import java.lang.reflect.Type

import com.wordnik.swagger.model._
import org.slf4j.LoggerFactory

import scala.collection.mutable.LinkedHashMap

class CustomModelPropertyParser(cls: Class[_])(implicit properties: LinkedHashMap[String, ModelProperty]) extends ModelPropertyParser(cls) {
  private val LOGGER = LoggerFactory.getLogger(classOf[ModelPropertyParser])

  override def parsePropertyAnnotations(returnClass: Class[_], propertyName: String, propertyAnnotations: Array[Annotation], genericReturnType: Type, returnType: Type): Any = {
    val e = extractGetterProperty(propertyName)
    var name = e._1
    val isGetter = e._2

    var isFieldExists = false
    var isJsonProperty = false
    val hasAccessorNoneAnnotation = false
    val processedAnnotations = processAnnotations(name, propertyAnnotations)
    var required = processedAnnotations("required").asInstanceOf[Boolean]
    var position = processedAnnotations("position").asInstanceOf[Int]

    var description = {
      if (processedAnnotations.contains("description") && processedAnnotations("description") != null)
        Some(processedAnnotations("description").asInstanceOf[String])
      else None
    }
    var isTransient = processedAnnotations("isTransient").asInstanceOf[Boolean]
    var isXmlElement = processedAnnotations("isXmlElement").asInstanceOf[Boolean]
    val isDocumented = processedAnnotations("isDocumented").asInstanceOf[Boolean]
    var allowableValues = {
      if (returnClass.isEnum)
        Some(AllowableListValues((for (v <- returnClass.getEnumConstants) yield v.toString).toList))
      else
        processedAnnotations("allowableValues").asInstanceOf[Option[AllowableValues]]
    }

    try {
      val fieldAnnotations = getDeclaredField(this.cls, name).getAnnotations()
      val propAnnoOutput = processAnnotations(name, fieldAnnotations)
      val propPosition = propAnnoOutput("position").asInstanceOf[Int]

      if (allowableValues == None)
        allowableValues = propAnnoOutput("allowableValues").asInstanceOf[Option[AllowableValues]]
      if (description == None && propAnnoOutput.contains("description") && propAnnoOutput("description") != null)
        description = Some(propAnnoOutput("description").asInstanceOf[String])
      if (propPosition != 0) position = propAnnoOutput("position").asInstanceOf[Int]
      if (required == false) required = propAnnoOutput("required").asInstanceOf[Boolean]
      isFieldExists = true
      if (!isTransient) isTransient = propAnnoOutput("isTransient").asInstanceOf[Boolean]
      if (!isXmlElement) isXmlElement = propAnnoOutput("isXmlElement").asInstanceOf[Boolean]
      isJsonProperty = propAnnoOutput("isJsonProperty").asInstanceOf[Boolean]
    } catch {
      //this means there is no field declared to look for field level annotations.
      case e: java.lang.NoSuchFieldException => isTransient = false
    }

    //if class has accessor none annotation, the method/field should have explicit xml element annotations, if not
    // consider it as transient
    if (!isXmlElement && hasAccessorNoneAnnotation)
      isTransient = true

    if (!(isTransient && !isXmlElement && !isJsonProperty) && name != null && (isFieldExists || isGetter || isDocumented)) {
      var paramType = getDataType(genericReturnType, returnType, false)
      LOGGER.debug("inspecting " + paramType)
      var simpleName = getDataType(genericReturnType, returnType, true)

      if (!"void".equals(paramType) && null != paramType && !processedFields.contains(name)) {
        if (!excludedFieldTypes.contains(paramType)) {
          val items = {
            val ComplexTypeMatcher = "([a-zA-Z]*)\\[([a-zA-Z\\.\\-\\$0-9_]*)\\].*".r // TODO fix for generics
            paramType match {
              case ComplexTypeMatcher(containerType, basePart) => {
                LOGGER.debug("containerType: " + containerType + ", basePart: " + basePart + ", simpleName: " + simpleName)
                paramType = containerType
                val ComplexTypeMatcher(t, simpleTypeRef) = simpleName
                val typeRef = {
                  if (simpleTypeRef.indexOf(",") > 0) // it's a map, use the value only
                    simpleTypeRef.split(",").last
                  else simpleTypeRef
                }
                simpleName = containerType
                if (isComplex(simpleTypeRef)) {
                  Some(ModelRef(null, Some(simpleTypeRef), Some(basePart)))
                }
                else Some(ModelRef(simpleTypeRef, None, Some(basePart)))
              }
              case _ => None
            }
          }
          val param = ModelProperty(
            validateDatatype(simpleName),
            paramType,
            position,
            required,
            description,
            allowableValues.getOrElse(AnyAllowableValues),
            items)
          LOGGER.debug("added param type " + paramType + " for field " + name)
          properties += name -> param
        }
        else {
          LOGGER.debug("field " + paramType + " is has been explicitly excluded")
        }
      }
      else {
        LOGGER.debug("skipping " + name)
      }
      processedFields += name
    }
  }
}