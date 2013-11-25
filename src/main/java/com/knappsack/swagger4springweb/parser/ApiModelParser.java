package com.knappsack.swagger4springweb.parser;

import com.knappsack.swagger4springweb.model.AnnotatedParameter;
import com.knappsack.swagger4springweb.util.AnnotationUtils;
import com.wordnik.swagger.converter.SwaggerSchemaConverter;
import com.wordnik.swagger.model.Model;
import org.springframework.web.bind.annotation.ResponseBody;
import scala.Option;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ApiModelParser {

    public Map<String, Model> getResponseBodyModels(Method method) {
        Map<String, Model> documentationSchemaMap = new HashMap<String, Model>();
        if(method.getAnnotation(ResponseBody.class) != null) {
            Class<?> returnType = method.getReturnType();
            SwaggerSchemaConverter parser = new SwaggerSchemaConverter();
            String schemaName;
            if(returnType.isArray()) {
                //TODO - possibly reinvestigate what we should do in the case of an array
                //parser = new ApiModelParser(returnType.getComponentType());
                schemaName = returnType.getComponentType().getSimpleName();
            } else {
                schemaName = returnType.getSimpleName();
            }
            Option<Model> model = parser.read(returnType);
            if(model.nonEmpty()) {
                documentationSchemaMap.put(schemaName, model.get());
            }
        }

        return documentationSchemaMap;
    }

    public Map<String, Model> getParameterModels(Method method) {

        Map<String, Model> documentationSchemaMap = new HashMap<String, Model>();

        List<AnnotatedParameter> annotatedParameters = AnnotationUtils.getAnnotatedParameters(method);
        for (AnnotatedParameter annotatedParameter : annotatedParameters) {
            Class<?> parameterType = annotatedParameter.getParameterType();
            SwaggerSchemaConverter parser = new SwaggerSchemaConverter();
            Option<Model> model = parser.read(parameterType);
            if(model.nonEmpty()) {
                documentationSchemaMap.put(parameterType.getSimpleName(), model.get());
            }
        }

        return documentationSchemaMap;
    }
}
