package com.knappsack.swagger4springweb.util

import com.wordnik.swagger.model.{Authorization, ApiDescription, Model, ApiListing}

object ApiListingUtil {

  def baseApiListing(apiVersion: String, swaggerVersion: String, basePath: String, resourcePath: String): ApiListing = {
    new ApiListing(apiVersion, swaggerVersion, basePath, resourcePath,
      List.empty[String], List.empty[String], List.empty[String], List.empty[Authorization], List.empty[ApiDescription], None, None, 0)
  }

  def mergeApiListing(apiListing1: ApiListing, apiListing2: ApiListing): ApiListing = {

    val mergedModels: Map[String, Model] = (apiListing1.models, apiListing2.models) match {
      case (Some(x), Some(y)) => apiListing1.models.get ++ apiListing2.models.get
      case (Some(x), None) => apiListing1.models.get
      case (None, Some(y)) => apiListing2.models.get
      case (None, None) => Map.empty[String, Model]
    }

    new ApiListing(apiListing1.apiVersion, apiListing1.swaggerVersion, apiListing1.basePath, apiListing1.resourcePath,
      (apiListing1.produces ++ apiListing2.produces).distinct, (apiListing1.consumes ++ apiListing2.consumes).distinct,
      (apiListing1.protocols ++ apiListing2.protocols).distinct, (apiListing1.authorizations ++ apiListing2.authorizations).distinct,
      (apiListing1.apis ++ apiListing2.apis).distinct, Option(mergedModels), apiListing1.description, apiListing1.position)
  }

}
