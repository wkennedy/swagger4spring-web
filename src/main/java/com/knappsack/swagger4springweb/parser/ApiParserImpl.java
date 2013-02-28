package com.knappsack.swagger4springweb.parser;

import com.knappsack.swagger4springweb.util.AnnotationUtils;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.core.Documentation;
import com.wordnik.swagger.core.DocumentationEndPoint;
import com.wordnik.swagger.core.DocumentationOperation;
import com.wordnik.swagger.core.DocumentationSchema;
import com.wordnik.swagger.jsonschema.ApiModelParser;
import org.reflections.Reflections;
import org.reflections.scanners.ResourcesScanner;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.reflections.ReflectionUtils.withAnnotation;

public class ApiParserImpl implements ApiParser {
    private static final String swaggerVersion = com.wordnik.swagger.core.SwaggerSpec.version();

    private String baseControllerPackage = "";
    private String baseModelPackage = "";
    private String basePath = "";
    private String apiVersion = "v1";

    private final Map<String, Documentation> documents = new HashMap<String, Documentation>();

    public ApiParserImpl(String baseControllerPackage, String baseModelPackage, String basePath, String apiVersion) {
        this.baseControllerPackage = baseControllerPackage;
        this.baseModelPackage = baseModelPackage;
        this.basePath = basePath;
        this.apiVersion = apiVersion;
    }

    public Documentation getResourceListing(Map<String, Documentation> documentationMap) {
        Documentation resourceList = new Documentation(apiVersion, swaggerVersion, basePath, null);
        for (String key : documentationMap.keySet()) {
            DocumentationEndPoint endPoint = new DocumentationEndPoint();
            String docPath = "/api/doc";
            if(!key.startsWith("/")) {
                docPath = docPath + "/";
            }
            endPoint.setPath(docPath + key);
            endPoint.setDescription("");
            resourceList.addApi(endPoint);
        }
        return resourceList;
    }

    public Map<String, Documentation> createDocuments() {
        Reflections reflections = new Reflections(baseControllerPackage);
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
            if (baseModelPackage != null && !baseModelPackage.isEmpty()) {
                createDocumentationSchemas(documentation);
            }

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
            DocumentationEndPoint documentationEndPoint = documentationEndPointParser.getDocumentationEndPoint(method, description, documentation.getResourcePath());
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
            documentationEndPoint.addOperation(documentationOperation);

            DocumentationSchemaParser documentationSchemaParser = new DocumentationSchemaParser();
            Map<String, DocumentationSchema> documentationSchemaMap = documentationSchemaParser.getResponseBodyDocumentationSchema(method);
            for (String key : documentationSchemaMap.keySet()) {
                documentation.addModel(key, documentationSchemaMap.get(key));
            }


            Map<String, DocumentationSchema> parameterDocumentationSchemaMap = documentationSchemaParser.getParameterDocumentationSchema(method);
            for (String key : parameterDocumentationSchemaMap.keySet()) {
                documentation.addModel(key, parameterDocumentationSchemaMap.get(key));
            }
        }
    }

    private void createDocumentationSchemas(Documentation documentation) {
        Reflections reflections = new Reflections(new ConfigurationBuilder()
                .filterInputsBy(new FilterBuilder().include(FilterBuilder.prefix(baseModelPackage)))
                .setUrls(ClasspathHelper.forPackage(baseModelPackage))
                .setScanners(new SubTypesScanner(false), new ResourcesScanner()));
        Set<Class<? extends Object>> allModelClasses = reflections.getSubTypesOf(Object.class);
        for (Class<? extends Object> clazz : allModelClasses) {
            ApiModelParser parser;
            String schemaName;
            if (clazz.isArray()) {
                parser = new ApiModelParser(clazz.getComponentType());
                schemaName = clazz.getComponentType().getSimpleName();
            } else {
                parser = new ApiModelParser(clazz);
                schemaName = clazz.getSimpleName();
            }
            documentation.addModel(schemaName, parser.parse().toDocumentationSchema());
        }
    }
}
