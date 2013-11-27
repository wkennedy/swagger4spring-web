package com.knappsack.swagger4springweb.util

import scala.collection.JavaConverters

object JavaToScalaUtil {

  def toScalaImmutableMap[A, B](javaMap : java.util.Map[A, B]) : scala.collection.immutable.Map[A, B]  = {
    JavaConverters.mapAsScalaMapConverter(javaMap).asScala.toMap
  }

  def toScalaList[T](javaList: java.util.List[T]): List[T] = {
    if (javaList != null) {
      return scala.collection.JavaConversions.collectionAsScalaIterable(javaList).toList
    }
    scala.collection.immutable.List.empty
  }

}
