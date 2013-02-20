package com.knappsack.swagger4springweb.parser;


import com.knappsack.swagger4springweb.util.AnnotationUtils;
import com.wordnik.swagger.core.DocumentationEndPoint;

import java.lang.reflect.Method;

public class DocumentationEndPointParser {

    public DocumentationEndPoint getDocumentationEndPoint(Method method, String description, String resourcePath) {
        String requestMappingValue = AnnotationUtils.getMethodRequestMappingValue(method);
        DocumentationEndPoint documentationEndPoint = new DocumentationEndPoint();
        if (resourcePath != null && !resourcePath.isEmpty()) {
            documentationEndPoint.setPath(resourcePath + requestMappingValue);
        } else {
            documentationEndPoint.setPath(requestMappingValue);
        }
        documentationEndPoint.setDescription(description);

        return documentationEndPoint;
    }

}
