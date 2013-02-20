package com.knappsack.swagger4springweb.parser;

import com.wordnik.swagger.core.Documentation;

import java.util.Map;

public interface ApiParser {
    /**
     * @param documentationMap Map<String, Documentation> - A map of different API declarations for which you want to
     *                         create a resource listing.
     * @return Documentation - This returns a resource listing which is an inventory of all APIs
     */
    Documentation getResourceListing(Map<String, Documentation> documentationMap);

    /**
     * @return Map<String, Documentation> - a map of different API declarations discovered when scanning for classes
     * annotated with @Controller.  The key value is the resource path of the API.
     */
    Map<String, Documentation> createDocuments();
}
