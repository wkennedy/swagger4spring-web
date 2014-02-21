package com.knappsack.swagger4springweb.parser;

import com.knappsack.swagger4springweb.annotation.ApiExclude;
import com.knappsack.swagger4springweb.controller.ApiDocumentationController;
import com.knappsack.swagger4springweb.util.AnnotationUtils;
import com.knappsack.swagger4springweb.util.JavaToScalaUtil;
import com.knappsack.swagger4springweb.util.ScalaToJavaUtil;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.config.SwaggerConfig;
import com.wordnik.swagger.model.*;
import org.reflections.Reflections;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import scala.Option;

import java.lang.reflect.Method;
import java.util.*;

import static org.reflections.ReflectionUtils.withAnnotation;

public class ApiParserImpl implements ApiParser {

    private static final String swaggerVersion = com.wordnik.swagger.core.SwaggerSpec.version();

    private List<String> controllerPackages = new ArrayList<String>();
    private String basePath = "";
    private String apiVersion = "v1";
    private List<String> ignorableAnnotations;
    private boolean ignoreUnusedPathVariables;
    private SwaggerConfig swaggerConfig;

    private final Map<String, ApiListing> apiListingMap = new HashMap<String, ApiListing>();

    public ApiParserImpl(ApiInfo apiInfo, List<String> baseControllerPackage, String basePath, String servletPath,
            String apiVersion, List<String> ignorableAnnotations, boolean ignoreUnusedPathVariables) {
        this.controllerPackages = baseControllerPackage;
        this.ignorableAnnotations = ignorableAnnotations;
        this.ignoreUnusedPathVariables = ignoreUnusedPathVariables;
        this.basePath = basePath;
        this.apiVersion = apiVersion;
        swaggerConfig = new SwaggerConfig();
        if (apiInfo != null) {
            swaggerConfig.setApiInfo(apiInfo);
        }
        swaggerConfig.setApiPath(servletPath);
        swaggerConfig.setApiVersion(apiVersion);
        swaggerConfig.setBasePath(basePath);
        swaggerConfig.setSwaggerVersion(swaggerVersion);
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
            ApiListingReference apiListingReference = new ApiListingReference(docPath + key, apiListing.description(),
                    count);

            apiListingReferences.add(apiListingReference);
            count++;
        }

        return new ResourceListing(apiVersion, swaggerVersion, JavaToScalaUtil.toScalaList(apiListingReferences), null,
                swaggerConfig.info());
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

            Set<Method> requestMappingMethods = Reflections
                    .getAllMethods(controllerClass, withAnnotation(RequestMapping.class));
            ApiListing apiListing = processControllerApi(controllerClass);
            String description = "";
            Api controllerApi = controllerClass.getAnnotation(Api.class);
            if (controllerApi != null) {
                description = controllerApi.description();
            }

            if (apiListing.apis() == null) {
                apiListing = processMethods(requestMappingMethods, apiListing, description);
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
            if (controllerRequestMapping != null && controllerRequestMapping.value() != null &&
                    controllerRequestMapping.value().length > 0) {
                resourcePath = controllerRequestMapping.value()[0];
            } else {
                resourcePath = controllerClass.getName();
            }
        }

        SpringApiReader reader = new SpringApiReader();
        Option<ApiListing> apiListingOption = reader.read(resourcePath, controllerClass, swaggerConfig);
        ApiListing apiListing = null;
        if (apiListingOption.nonEmpty()) {
            apiListing = apiListingOption.get();
        }

        //Allow for multiple controllers having the same resource path.
        ApiListing existingApiListing = apiListingMap.get(resourcePath);
        if (existingApiListing != null) {
            return existingApiListing;
        }

        if (apiListing != null) {
            return apiListing;
        }

        return new ApiListing(apiVersion, swaggerVersion, basePath, resourcePath, null, null, null, null, null, null,
                null, 0);
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
            ApiDescription apiDescription = documentationEndPointParser
                    .parseApiDescription(method, description, apiListing.resourcePath());
            if (!endPointMap.containsKey(requestMappingValue)) {
                endPointMap.put(requestMappingValue, apiDescription);
            }
        }

        Map<String, Model> models = new HashMap<String, Model>();

        Map<String, List<Operation>> operationMap = new HashMap<String, List<Operation>>();
        for (Method method : methods) {
            if (method.isAnnotationPresent(ApiExclude.class)) {
                continue;
            }

            String value = AnnotationUtils.getMethodRequestMappingValue(method);
            List<Operation> operations = operationMap.get(value);
            if (operations == null) {
                operations = new ArrayList<Operation>();
                operationMap.put(value, operations);
            }

            ApiOperationParser apiOperationParser = new ApiOperationParser(apiListing.resourcePath(),
                    ignorableAnnotations, ignoreUnusedPathVariables, models);
            Operation operation = apiOperationParser.parseDocumentationOperation(method);
            operations.add(operation);
        }

        List<ApiDescription> newApiDescriptions = new ArrayList<ApiDescription>();
        for (String key : endPointMap.keySet()) {
            ApiDescription apiDescription = endPointMap.get(key);
            ApiDescription newApiDescription = new ApiDescription(apiDescription.path(), apiDescription.description(),
                    JavaToScalaUtil.toScalaList(operationMap.get(key)));
            newApiDescriptions.add(newApiDescription);
        }

        ApiModelParser apiModelParser = new ApiModelParser(models);
        for (Method method : methods) {
            apiModelParser.parseResponseBodyModels(method);
        }

        Option<scala.collection.immutable.Map<String, Model>> modelOptions = Option
                .apply(JavaToScalaUtil.toScalaImmutableMap(models));

        return new ApiListing(apiListing.apiVersion(), apiListing.swaggerVersion(), apiListing.basePath(),
                apiListing.resourcePath(), apiListing.produces(), apiListing.consumes(), apiListing.protocols(),
                apiListing.authorizations(), JavaToScalaUtil.toScalaList(newApiDescriptions), modelOptions,
                apiListing.description(), apiListing.position());
    }

    private void populateApiDescriptionMapForApiListing(ApiListing apiListing,
            Map<String, ApiDescription> apiDescriptionMap) {
        if (apiListing.apis() != null) {

            List<ApiDescription> apiDescriptions = ScalaToJavaUtil.toJavaList(apiListing.apis());
            for (ApiDescription apiDescription : apiDescriptions) {
                apiDescriptionMap.put(apiDescription.path(), apiDescription);
            }
        }
    }
}
