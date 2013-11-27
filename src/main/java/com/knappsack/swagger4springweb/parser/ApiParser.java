package com.knappsack.swagger4springweb.parser;

import com.wordnik.swagger.model.ApiListing;
import com.wordnik.swagger.model.ResourceListing;

import java.util.Map;

public interface ApiParser {
    /**
     * @param documentationMap Map<String, ApiListing> - A map of different API declarations for which you want to
     *                         create a resource listing.
     * @return ResourceListing - This returns a resource listing which is an inventory of all APIs
     */
    ResourceListing getResourceListing(Map<String, ApiListing> documentationMap);

    /**
     * @return Map - a map of different API declarations discovered when scanning for classes
     * annotated with @Controller.  The key value is the resource path of the API.
     */
    Map<String, ApiListing> createApiListings();
}
