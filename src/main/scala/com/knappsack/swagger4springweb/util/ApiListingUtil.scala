package com.knappsack.swagger4springweb.util

import com.wordnik.swagger.model.ApiListing

/**
 * @author Allar Tammik
 */
object ApiListingUtil {
  def combine(main: ApiListing, additional : ApiListing) : ApiListing = main.copy(apis = main.apis ++ additional.apis)
  def sortApisByPath(obj: ApiListing) : ApiListing = obj.copy(apis = obj.apis.sortBy(api => api.path))
  def changeDescription(obj: ApiListing, desc: String) : ApiListing = obj.copy(description = Some(desc))
}
