package com.knappsack.swagger4springweb.util

import com.wordnik.swagger.model.ApiListing

/**
 * @author Allar Tammik
 */
object ApiListingUtil {
  def combine(main: ApiListing, additional : ApiListing) : ApiListing = main.copy(apis = main.apis ++ additional.apis)
}
