package com.knappsack.swagger4springweb.parser;


import com.knappsack.swagger4springweb.util.AnnotationUtils;
//import com.wordnik.swagger.core.DocumentationEndPoint;
import com.wordnik.swagger.model.ApiDescription;
import com.wordnik.swagger.model.ApiListingReference;
import scala.Option;

import java.lang.reflect.Method;

public class ApiDescriptionParser {

//    public ApiListingReference getDocumentationEndPoint(Method method, String description, String resourcePath, int position) {
//        String requestMappingValue = AnnotationUtils.getMethodRequestMappingValue(method);
//        String path = "";
//        if (resourcePath != null && !resourcePath.isEmpty()) {
//            path = resourcePath + requestMappingValue;
//        } else {
//            path = requestMappingValue;
////            documentationEndPoint.setPath(requestMappingValue);
//        }
////        documentationEndPoint.setDescription(description);
//
//        ApiListingReference documentationEndPoint = new ApiListingReference(path, Option.apply(description), position);
//
//        return documentationEndPoint;
//    }


    public ApiDescription getApiDescription(Method method, String description, String resourcePath) {
        String requestMappingValue = AnnotationUtils.getMethodRequestMappingValue(method);
        String path = "";
        if (resourcePath != null && !resourcePath.isEmpty()) {
            path = resourcePath + requestMappingValue;
        } else {
            path = requestMappingValue;
//            documentationEndPoint.setPath(requestMappingValue);
        }
//        documentationEndPoint.setDescription(description);


        Option<String> descriptionOption = Option.apply(description);
        ApiDescription apiDescription = new ApiDescription(path, descriptionOption, null);

        return apiDescription;
    }
}
