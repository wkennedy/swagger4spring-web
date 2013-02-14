package com.sparc.swagger4springweb.parser;

import com.sparc.swagger4springweb.model.AnnotatedParameter;
import com.sparc.swagger4springweb.util.AnnotationUtils;
import com.wordnik.swagger.core.DocumentationSchema;
import com.wordnik.swagger.jsonschema.ApiModelParser;
import org.springframework.web.bind.annotation.ResponseBody;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DocumentationSchemaParser {

    public Map<String, DocumentationSchema> getResponseBodyDocumentationScehma(Method method) {
        Map<String, DocumentationSchema> documentationSchemaMap = new HashMap<String, DocumentationSchema>();
        if(method.getAnnotation(ResponseBody.class) != null) {
            ApiModelParser parser = new ApiModelParser(method.getReturnType());
            documentationSchemaMap.put(method.getReturnType().getName(), parser.parse().toDocumentationSchema());
        }

        return documentationSchemaMap;
    }

    public Map<String, DocumentationSchema> getParameterDocumentationSchema(Method method) {
        Map<String, DocumentationSchema> documentationSchemaMap = new HashMap<String, DocumentationSchema>();

        List<AnnotatedParameter> annotatedParameters = AnnotationUtils.getAnnotatedParameters(method);
        for (AnnotatedParameter annotatedParameter : annotatedParameters) {
            Class<?> parameterType = annotatedParameter.getParameterType();
            ApiModelParser parser = new ApiModelParser(parameterType);
            documentationSchemaMap.put(parameterType.getName(), parser.parse().toDocumentationSchema());
        }

        return documentationSchemaMap;
    }
}
