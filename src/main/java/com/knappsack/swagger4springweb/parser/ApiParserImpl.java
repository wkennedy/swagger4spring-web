package com.knappsack.swagger4springweb.parser;

import com.knappsack.swagger4springweb.annotation.ApiExclude;
import com.knappsack.swagger4springweb.controller.ApiDocumentationController;
import com.knappsack.swagger4springweb.util.AnnotationUtils;
import com.knappsack.swagger4springweb.util.JavaToScalaUtil;
import com.knappsack.swagger4springweb.util.ScalaToJavaUtil;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.config.SwaggerConfig;
import com.wordnik.swagger.converter.ModelConverters;
import com.wordnik.swagger.model.*;
import org.reflections.Reflections;
import org.reflections.scanners.ResourcesScanner;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import scala.Option;

import java.lang.reflect.Method;
import java.util.*;

import static org.reflections.ReflectionUtils.withAnnotation;

public class ApiParserImpl implements ApiParser {
    private static final String swaggerVersion = com.wordnik.swagger.core.SwaggerSpec.version();

    private List<String> controllerPackages = new ArrayList<String>();
    private List<String> modelPackages = new ArrayList<String>();
    private String basePath = "";
    private String servletPath = "/api";
    private String apiVersion = "v1";
    private List<String> ignorableAnnotations;
    private boolean ignoreUnusedPathVariables;
    private SwaggerConfig swaggerConfig;
    private Map<String, Model> apiListingModels;

    private final Map<String, ApiListing> apiListingMap = new HashMap<String, ApiListing>();

    public ApiParserImpl(ApiInfo apiInfo, List<String> baseControllerPackage, List<String> baseModelPackage, String basePath, String servletPath,
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
        swaggerConfig = new SwaggerConfig();
        if(apiInfo != null) {
            swaggerConfig.setApiInfo(apiInfo);
        }
        swaggerConfig.setApiPath(servletPath);
        swaggerConfig.setApiVersion(apiVersion);
        swaggerConfig.setBasePath(basePath);
        swaggerConfig.setSwaggerVersion(swaggerVersion);
        createApiListingModels();
    }

    public ResourceListing getResourceListing(Map<String, ApiListing> apiListingMap) {
        int count = 0;
        List<ApiListingReference> apiListingReferences = new ArrayList<ApiListingReference>();
        for (String key : apiListingMap.keySet()) {
            ApiListing apiListing = apiListingMap.get(key);
            String docPath = "/doc"; //servletPath + "/doc"; //"/api/doc";
            if (!key.startsWith("/")) {
                docPath = docPath + "/";
            }
            ApiListingReference apiListingReference = new ApiListingReference(docPath + key, apiListing.description(), count);

            apiListingReferences.add(apiListingReference);
            count++;
        }

       return new ResourceListing(apiVersion, swaggerVersion, JavaToScalaUtil.toScalaList(apiListingReferences), null, swaggerConfig.info());
    }

    public Map<String, ApiListing> createApiListings() {
        Set<Class<?>> controllerClasses = new HashSet<Class<?>>();
        for (String controllerPackage : controllerPackages) {
            Reflections reflections = new Reflections(controllerPackage);
            controllerClasses.addAll(reflections.getTypesAnnotatedWith(Controller.class));
        }

        return processControllers(controllerClasses);
    }

    private Map<String, ApiListing> processControllers(Set<Class<?>> controllerClasses) {
        //Loop over end points (controllers)
        for (Class<?> controllerClass : controllerClasses) {
            if (ApiDocumentationController.class.isAssignableFrom(controllerClass)) {
                continue;
            }

            if (controllerClass.isAnnotationPresent(ApiExclude.class)) {
                continue;
            }

            Set<Method> requestMappingMethods = Reflections.getAllMethods(controllerClass, withAnnotation(RequestMapping.class));
            ApiListing apiListing = processControllerApi(controllerClass);
            String description = "";
            Api controllerApi = controllerClass.getAnnotation(Api.class);
            if (controllerApi != null) {
                description = controllerApi.description();
            } else {
                if(apiListing.apis() == null) {
                    apiListing = processMethods(requestMappingMethods, apiListing, description);
                    //Loop over operations 'methods'
                    //processMethods(requestMappingMethods, apiListing, description);
                    if (modelPackages != null && !modelPackages.isEmpty()) {
                        //todo - do the models need to be added to the ApiListing.  Can this be removed?
                        createApiListingModels();
                    }
                }
            }

            // controllers without any operations are excluded from the apiListingMap list
            if (apiListing.apis() != null && !apiListing.apis().isEmpty()) {
                apiListingMap.put(apiListing.resourcePath(), apiListing);
            }
        }

        return apiListingMap;
    }

    private ApiListing processControllerApi(Class<?> controllerClass) {
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

        SpringApiReader reader = new SpringApiReader();
        Option<ApiListing> apiListingOption = reader.read(resourcePath, controllerClass, swaggerConfig);
        ApiListing apiListing = null;
        if(apiListingOption.nonEmpty()) {
            apiListing = reader.read(resourcePath, controllerClass, swaggerConfig).get();
        }

        //Allow for multiple controllers having the same resource path.
        ApiListing existingApiListing = apiListingMap.get(resourcePath);
        if (existingApiListing != null){
           return existingApiListing;
        }

        if(apiListing != null) {
            return apiListing;
        }

        return new ApiListing(apiVersion, swaggerVersion, basePath, resourcePath, null, null, null, null, null, null, null, 0);
    }

    private ApiListing processMethods(Collection<Method> methods, ApiListing apiListing, String description) {
        Map<String, ApiDescription> endPointMap = new HashMap<String, ApiDescription>();
        
        populateApiDescriptionMapForApiListing(apiListing, endPointMap);

        for (Method method : methods) {
            if (method.isAnnotationPresent(ApiExclude.class)) {
                continue;
            }

            String requestMappingValue = AnnotationUtils.getMethodRequestMappingValue(method);
            ApiDescriptionParser documentationEndPointParser = new ApiDescriptionParser();
            ApiDescription apiDescription = documentationEndPointParser.getApiDescription(method, description, apiListing.resourcePath());
            if (!endPointMap.containsKey(requestMappingValue)) {
                endPointMap.put(requestMappingValue, apiDescription);
//                documentation.apis().add(documentationEndPoint);
            }
        }

        Map<String, List<Operation>> operationMap = new HashMap<String, List<Operation>>();
        for (Method method : methods) {
            if (method.isAnnotationPresent(ApiExclude.class)) {
                continue;
            }

            String value = AnnotationUtils.getMethodRequestMappingValue(method);
            List<Operation> operations = operationMap.get(value);
            if(operations == null) {
                operations = new ArrayList<Operation>();
                operationMap.put(value, operations);
            }
//            ApiDescription documentationEndPoint = endPointMap.get(value);

            ApiOperationParser apiOperationParser = new ApiOperationParser(apiListing.resourcePath(), ignorableAnnotations, ignoreUnusedPathVariables);
            Operation operation = apiOperationParser.getDocumentationOperation(method);
            operations.add(operation);
        }

        List<ApiDescription> newApiDescriptions = new ArrayList<ApiDescription>();
        for (String key : endPointMap.keySet()) {
            ApiDescription apiDescription = endPointMap.get(key);
            ApiDescription newApiDescription = new ApiDescription(apiDescription.path(), apiDescription.description(), JavaToScalaUtil.toScalaList(operationMap.get(key)));
            newApiDescriptions.add(newApiDescription);
        }

        Map<String, Model> modelMap = new HashMap<String, Model>();
        for (Method method : methods) {
            ApiModelParser apiModelParser = new ApiModelParser();
            modelMap.putAll(apiModelParser.getResponseBodyModels(method));
//            for (String key : documentationSchemaMap.keySet()) {
//                documentation.models().add(key, documentationSchemaMap.get(key));
//            }
//
//            Map<String, Option<Model>> parameterDocumentationSchemaMap = apiModelParser.getParameterDocumentationSchema(method);
//            for (String key : parameterDocumentationSchemaMap.keySet()) {
//                documentation.models().add(key, parameterDocumentationSchemaMap.get(key));
//            }
        }

//        Map<String, Model> apiListingModels = createApiListingModels();

//        Option.<scala.collection.immutable.Map<String,Model>>empty();
//        ScalaToJavaUtil.toScalaImmutableMap(documentationSchemaMap);
        Option<scala.collection.immutable.Map<String, Model>> modelOptions = Option.apply(JavaToScalaUtil.toScalaImmutableMap(apiListingModels));

        return new ApiListing(apiListing.apiVersion(), apiListing.swaggerVersion(), apiListing.basePath(), apiListing.resourcePath(),
                apiListing.produces(), apiListing.consumes(), apiListing.protocols(), apiListing.authorizations(), JavaToScalaUtil.toScalaList(newApiDescriptions), modelOptions,
                apiListing.description(), apiListing.position());
    }

    private void populateApiDescriptionMapForApiListing(ApiListing apiListing, Map<String, ApiDescription> apiDescriptionMap){
       if (apiListing.apis() != null){

          List<ApiDescription> apiDescriptions = ScalaToJavaUtil.toJavaList(apiListing.apis());
          for (ApiDescription apiDescription : apiDescriptions){
             apiDescriptionMap.put(apiDescription.path(), apiDescription);
          }
       }
    }

    private void createApiListingModels() {
        Map<String, Model> modelMap = new HashMap<String, Model>();

        for (String modelPackage : modelPackages) {
            Reflections reflections = new Reflections(new ConfigurationBuilder()
                    .filterInputsBy(new FilterBuilder().include(FilterBuilder.prefix(modelPackage)))
                    .setUrls(ClasspathHelper.forPackage(modelPackage))
                    .setScanners(new SubTypesScanner(false), new ResourcesScanner()));
            Set<Class<? extends Object>> allModelClasses = reflections.getSubTypesOf(Object.class);
            for (Class<? extends Object> clazz : allModelClasses) {
                Model model = null;
//                ApiModelParser parser;
                String schemaName;
                if (clazz.isArray()) {
//                    parser = new ApiModelParser(clazz.getComponentType());
                    schemaName = clazz.getComponentType().getSimpleName();
                    Option<Model> modelOption = ModelConverters.read(clazz.getComponentType());
                    if(modelOption.nonEmpty()) {
                        model = modelOption.get();
                    }
                } else {
//                    parser = new ApiModelParser(clazz);
                    schemaName = clazz.getSimpleName();
                    Option<Model> modelOption = ModelConverters.read(clazz);
                    if(modelOption.nonEmpty()) {
                        model = modelOption.get();
                    }
                }
                if(model != null) {
                    modelMap.put(schemaName, model);
                }
//                documentation.addModel(schemaName, parser.parse().toDocumentationSchema());
            }
        }
        apiListingModels = modelMap;
    }
}
