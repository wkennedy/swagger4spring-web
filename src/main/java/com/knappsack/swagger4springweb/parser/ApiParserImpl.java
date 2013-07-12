package com.knappsack.swagger4springweb.parser;

import com.knappsack.swagger4springweb.annotation.ApiExclude;
import com.knappsack.swagger4springweb.controller.ApiDocumentationController;
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
import java.util.*;

import static org.reflections.ReflectionUtils.withAnnotation;

public class ApiParserImpl implements ApiParser {
    private static final String swaggerVersion = com.wordnik.swagger.core.SwaggerSpec.version();

    //    private String baseControllerPackage = "";
    private List<String> controllerPackages = new ArrayList<String>();
    //    private String baseModelPackage = "";
    private List<String> modelPackages = new ArrayList<String>();
    private String basePath = "";
    private String servletPath = "/api";
    private String apiVersion = "v1";
    private List<String> ignorableAnnotations;
    private boolean ignoreUnusedPathVariables;

    private final Map<String, Documentation> documents = new HashMap<String, Documentation>();

    public ApiParserImpl(List<String> baseControllerPackage, List<String> baseModelPackage, String basePath, String servletPath,
            String apiVersion, List<String> ignorableAnnotations, boolean ignoreUnusedPathVariables) {
        this.controllerPackages = baseControllerPackage;
        this.modelPackages = baseModelPackage;
        this.ignorableAnnotations = ignorableAnnotations;
        this.ignoreUnusedPathVariables = ignoreUnusedPathVariables;
        this.basePath = basePath;
        this.apiVersion = apiVersion;
        if (servletPath != null && !servletPath.isEmpty()) {
            this.servletPath = servletPath;
        }
    }

    public Documentation getResourceListing(Map<String, Documentation> documentationMap) {
        Documentation resourceList = new Documentation(apiVersion, swaggerVersion, basePath, null);
        for (String key : documentationMap.keySet()) {
            DocumentationEndPoint endPoint = new DocumentationEndPoint();
            String docPath = servletPath + "/doc"; //"/api/doc";
            if (!key.startsWith("/")) {
                docPath = docPath + "/";
            }
            endPoint.setPath(docPath + key);
            endPoint.setDescription("");
            resourceList.addApi(endPoint);
        }
        return resourceList;
    }

    public Map<String, Documentation> createDocuments() {
        Set<Class<?>> controllerClasses = new HashSet<Class<?>>();
        for (String controllerPackage : controllerPackages) {
            Reflections reflections = new Reflections(controllerPackage);
            controllerClasses.addAll(reflections.getTypesAnnotatedWith(Controller.class));

        }

        return processControllers(controllerClasses);
    }

    private Map<String, Documentation> processControllers(Set<Class<?>> controllerClasses) {
        //Loop over end points (controllers)
        for (Class<?> controllerClass : controllerClasses) {
            if (ApiDocumentationController.class.isAssignableFrom(controllerClass)) {
                continue;
            }

            if (controllerClass.isAnnotationPresent(ApiExclude.class)) {
                continue;
            }

            Documentation documentation = processControllerDocumentation(controllerClass);
            String description = "";
            Api controllerApi = controllerClass.getAnnotation(Api.class);
            if (controllerApi != null) {
                description = controllerApi.description();
            }

            //Loop over operations 'methods'
            Set<Method> requestMappingMethods = Reflections.getAllMethods(controllerClass, withAnnotation(RequestMapping.class));
            processMethods(requestMappingMethods, documentation, description);
            if (modelPackages != null && !modelPackages.isEmpty()) {
                createDocumentationSchemas(documentation);
            }

            // controllers without any operations are excluded from the documents list
            if (documentation.getApis() != null && !documentation.getApis().isEmpty()) {
                documents.put(documentation.getResourcePath(), documentation);
            }
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
        
        //Allow for multiple controllers having the same resource path.
        Documentation documentation = documents.get(resourcePath);
        if (documentation != null){
           return documentation;
        }

        return new Documentation(apiVersion, swaggerVersion, basePath, resourcePath);
    }

    private void processMethods(Set<Method> methods, Documentation documentation, String description) {
        Map<String, DocumentationEndPoint> endPointMap = new HashMap<String, DocumentationEndPoint>();
        
        populateEndpointMapForDocumentation(documentation, endPointMap);
        
        for (Method method : methods) {
            if (method.isAnnotationPresent(ApiExclude.class)) {
                continue;
            }

            String requestMappingValue = AnnotationUtils.getMethodRequestMappingValue(method);
            DocumentationEndPointParser documentationEndPointParser = new DocumentationEndPointParser();
            DocumentationEndPoint documentationEndPoint = documentationEndPointParser.getDocumentationEndPoint(method, description, documentation.getResourcePath());
            if (!endPointMap.containsKey(requestMappingValue)) {
                endPointMap.put(requestMappingValue, documentationEndPoint);
                documentation.addApi(documentationEndPoint);
            }
        }

        for (Method method : methods) {
            if (method.isAnnotationPresent(ApiExclude.class)) {
                continue;
            }

            String value = AnnotationUtils.getMethodRequestMappingValue(method);
            DocumentationEndPoint documentationEndPoint = endPointMap.get(value);

            DocumentationOperationParser documentationOperationParser = new DocumentationOperationParser(documentation.resourcePath(), ignorableAnnotations, ignoreUnusedPathVariables);
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

    private void populateEndpointMapForDocumentation(Documentation documentation, Map<String, DocumentationEndPoint> endPointMap){
       if (documentation.getApis() != null){
          for (DocumentationEndPoint endpoint : documentation.getApis()){
             endPointMap.put(endpoint.getPath(), endpoint);
          }
       }
    }

    private void createDocumentationSchemas(Documentation documentation) {
        for (String modelPackage : modelPackages) {
            Reflections reflections = new Reflections(new ConfigurationBuilder()
                    .filterInputsBy(new FilterBuilder().include(FilterBuilder.prefix(modelPackage)))
                    .setUrls(ClasspathHelper.forPackage(modelPackage))
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
}
