package com.sparc.swagger4springweb.parser;

import com.sparc.swagger4springweb.util.AnnotationUtils;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.core.*;
import org.reflections.Reflections;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.reflections.ReflectionUtils.withAnnotation;

public class ApiParser {
    private static final String swaggerVersion = com.wordnik.swagger.core.SwaggerSpec.version();

    private String basePackage = "";
    private String basePath = "";
    private String apiVersion = "v1";

    private final Map<String, Documentation> documents = new HashMap<String, Documentation>();

    public ApiParser(String basePackage, String basePath, String apiVersion) {
        this.basePackage = basePackage;
        this.basePath = basePath;
        this.apiVersion = apiVersion;
    }

    public Documentation getResourceListing(Map<String, Documentation> documentationMap) {
        Documentation resourceList = new Documentation(apiVersion, swaggerVersion, basePath, null);
        for (String key : documentationMap.keySet()) {
            DocumentationEndPoint endPoint = new DocumentationEndPoint();
            endPoint.setPath("/api/doc" + key);
            endPoint.setDescription("");
            resourceList.addApi(endPoint);
        }
        return resourceList;
    }

    public Map<String, Documentation> createDocuments() {
        Reflections reflections = new Reflections(basePackage);
        Set<Class<?>> controllerClasses = reflections.getTypesAnnotatedWith(Controller.class);

        return processControllers(controllerClasses);
    }

    private Map<String, Documentation> processControllers(Set<Class<?>> controllerClasses) {
        //Loop over end points (controllers)
        for (Class<?> controllerClass : controllerClasses) {

            Documentation documentation = processControllerDocumentation(controllerClass);
            String description = "";
            Api controllerApi = controllerClass.getAnnotation(Api.class);
            if (controllerApi != null) {
                description = controllerApi.description();
            }

            //Loop over operations 'methods'
            Set<Method> requestMappingMethods = Reflections.getAllMethods(controllerClass, withAnnotation(RequestMapping.class));
            processMethods(requestMappingMethods, documentation, description);

            documents.put(documentation.getResourcePath(), documentation);
        }

        return documents;
    }

    private Documentation processControllerDocumentation(Class<?> controllerClass) {
        String resourcePath = "";
        Api controllerApi = controllerClass.getAnnotation(Api.class);
        if (controllerApi != null) {
            resourcePath = controllerApi.basePath();
        }

        if (controllerApi == null || resourcePath.isEmpty()) {
            RequestMapping controllerRequestMapping = controllerClass.getAnnotation(RequestMapping.class);
            if (controllerRequestMapping != null && controllerRequestMapping.value() != null && controllerRequestMapping.value().length > 0) {
                resourcePath = controllerRequestMapping.value()[0];
            } else {
                resourcePath = controllerClass.getName();
            }
        }

        return new Documentation(apiVersion, swaggerVersion, basePath, resourcePath);
    }

    private void processMethods(Set<Method> methods, Documentation documentation, String description) {
        Map<String, DocumentationEndPoint> endPointMap = new HashMap<String, DocumentationEndPoint>();
        for (Method method : methods) {
            String requestMappingValue = AnnotationUtils.getMethodRequestMappingValue(method);
            DocumentationEndPointParser documentationEndPointParser = new DocumentationEndPointParser();
            DocumentationEndPoint documentationEndPoint = documentationEndPointParser.getDocumentationEndPoint(method, description);
            if (!endPointMap.containsKey(requestMappingValue)) {
                endPointMap.put(requestMappingValue, documentationEndPoint);
                documentation.addApi(documentationEndPoint);
            }
        }

        for (Method method : methods) {
            String value = AnnotationUtils.getMethodRequestMappingValue(method);
            DocumentationEndPoint documentationEndPoint = endPointMap.get(value);

            DocumentationOperationParser documentationOperationParser = new DocumentationOperationParser();
            DocumentationOperation documentationOperation = documentationOperationParser.getDocumentationOperation(method);

            DocumentationSchemaParser documentationSchemaParser = new DocumentationSchemaParser();
            Map<String, DocumentationSchema> documentationSchemaMap = documentationSchemaParser.getResponseBodyDocumentationScehma(method);
            for (String key : documentationSchemaMap.keySet()) {
                documentation.addModel(key, documentationSchemaMap.get(key));
            }

            documentationEndPoint.addOperation(documentationOperation);

            Map<String, DocumentationSchema> parameterDocumentationSchemaMap = documentationSchemaParser.getParameterDocumentationSchema(method);
            for (String key : parameterDocumentationSchemaMap.keySet()) {
                documentation.addModel(key, parameterDocumentationSchemaMap.get(key));
            }
        }
    }
}
