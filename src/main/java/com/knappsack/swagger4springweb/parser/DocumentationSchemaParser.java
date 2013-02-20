package com.knappsack.swagger4springweb.parser;

import com.knappsack.swagger4springweb.model.AnnotatedParameter;
import com.knappsack.swagger4springweb.util.AnnotationUtils;
import com.wordnik.swagger.core.DocumentationSchema;
import com.wordnik.swagger.jsonschema.ApiModelParser;
import org.springframework.web.bind.annotation.ResponseBody;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DocumentationSchemaParser {

    public Map<String, DocumentationSchema> getResponseBodyDocumentationSchema(Method method) {
        Map<String, DocumentationSchema> documentationSchemaMap = new HashMap<String, DocumentationSchema>();
        if(method.getAnnotation(ResponseBody.class) != null) {
            Class<?> returnType = method.getReturnType();
            ApiModelParser parser;
            String schemaName;
            if(returnType.isArray()) {
                parser = new ApiModelParser(returnType.getComponentType());
                schemaName = returnType.getComponentType().getSimpleName();
            } else {
                parser = new ApiModelParser(method.getReturnType());
                schemaName = returnType.getSimpleName();
            }
            documentationSchemaMap.put(schemaName, parser.parse().toDocumentationSchema());
        }

        return documentationSchemaMap;
    }

    public Map<String, DocumentationSchema> getParameterDocumentationSchema(Method method) {
        Map<String, DocumentationSchema> documentationSchemaMap = new HashMap<String, DocumentationSchema>();

        List<AnnotatedParameter> annotatedParameters = AnnotationUtils.getAnnotatedParameters(method);
        for (AnnotatedParameter annotatedParameter : annotatedParameters) {
            Class<?> parameterType = annotatedParameter.getParameterType();
            ApiModelParser parser = new ApiModelParser(parameterType);
            documentationSchemaMap.put(parameterType.getSimpleName(), parser.parse().toDocumentationSchema());
        }

        return documentationSchemaMap;
    }
}
