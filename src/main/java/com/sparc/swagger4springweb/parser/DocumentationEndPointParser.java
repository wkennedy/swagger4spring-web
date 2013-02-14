package com.sparc.swagger4springweb.parser;


import com.sparc.swagger4springweb.util.AnnotationUtils;
import com.wordnik.swagger.core.DocumentationEndPoint;

import java.lang.reflect.Method;

public class DocumentationEndPointParser {

    public DocumentationEndPoint getDocumentationEndPoint(Method method, String description) {
        String requestMappingValue = AnnotationUtils.getMethodRequestMappingValue(method);
        DocumentationEndPoint documentationEndPoint = new DocumentationEndPoint();
        documentationEndPoint.setPath(requestMappingValue);
        documentationEndPoint.setDescription(description);

        return documentationEndPoint;
    }

}
