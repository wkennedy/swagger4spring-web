package com.knappsack.swagger4springweb.util

import scalaj.collection.Imports._

object ScalaToJavaUtil {

  def toJavaList[A](immutableList : scala.collection.immutable.List[A]) : java.util.List[A] = {
    immutableList.asJava
  }
}
