package com.knappsack.swagger4springweb.parser;

import com.knappsack.swagger4springweb.controller.ApiDocumentationController;
import com.knappsack.swagger4springweb.filter.ApiExcludeFilter;
import com.knappsack.swagger4springweb.filter.Filter;
import com.knappsack.swagger4springweb.util.AnnotationUtils;
import com.knappsack.swagger4springweb.util.JavaToScalaUtil;
import com.knappsack.swagger4springweb.util.ScalaToJavaUtil;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.config.SwaggerConfig;
import com.wordnik.swagger.model.*;
import org.reflections.ReflectionUtils;
import org.reflections.Reflections;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import scala.Option;

import java.lang.reflect.Method;
import java.util.*;

import static org.reflections.ReflectionUtils.withAnnotation;

public class ApiParserImpl implements ApiParser {

    private static final String swaggerVersion = com.wordnik.swagger.core.SwaggerSpec.version();

    private final Map<String, ApiListing> apiListingMap = new HashMap<String, ApiListing>();

    private final List<String> controllerPackages;
    private final List<String> ignorableAnnotations;
    private final List<Filter> filters;

    private String basePath = "";
    private String apiVersion = "v1";
    private boolean ignoreUnusedPathVariables;
    private SwaggerConfig swaggerConfig;

    public ApiParserImpl(ApiInfo apiInfo, List<String> baseControllerPackage, String basePath, String servletPath,
            String apiVersion, List<String> ignorableAnnotations, boolean ignoreUnusedPathVariables,
            Collection<Filter> filters) {

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

        this.filters = new ArrayList<Filter>();
        this.filters.add(new ApiExcludeFilter()); // @ApiExclude filter
        if (filters != null) {
            this.filters.addAll(filters);
        }
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

            Set<Method> requestMappingMethods =
                    ReflectionUtils.getAllMethods(controllerClass, withAnnotation(RequestMapping.class));
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

        Map<String, ApiDescription> endpoints = new HashMap<String, ApiDescription>();
        Map<String, Model> models = new HashMap<String, Model>();
        Map<String, List<Operation>> operations = new HashMap<String, List<Operation>>();
        List<ApiDescription> descriptions = new ArrayList<ApiDescription>();

        populateApiDescriptionMapForApiListing(apiListing, endpoints);

        ApiModelParser apiModelParser = new ApiModelParser(models);

        for (Method method : methods) {
            if (ignore(method)) {
                continue;
            }

            String value = AnnotationUtils.getMethodRequestMappingValue(method);
            ApiDescriptionParser documentationEndPointParser = new ApiDescriptionParser();
            ApiDescription apiDescription = documentationEndPointParser
                    .parseApiDescription(method, description, apiListing.resourcePath());
            if (!endpoints.containsKey(value)) {
                endpoints.put(value, apiDescription);
            }

            List<Operation> ops = operations.get(value);
            if (ops == null) {
                ops = new ArrayList<Operation>();
                operations.put(value, ops);
            }

            ApiOperationParser apiOperationParser = new ApiOperationParser(apiListing.resourcePath(),
                    ignorableAnnotations, ignoreUnusedPathVariables, models);
            Operation operation = apiOperationParser.parseDocumentationOperation(method);
            ops.add(operation);

            apiModelParser.parseResponseBodyModels(method);
        }

        for (String key : endpoints.keySet()) {
            ApiDescription apiDescription = endpoints.get(key);
            ApiDescription newApiDescription = new ApiDescription(apiDescription.path(), apiDescription.description(),
                    JavaToScalaUtil.toScalaList(operations.get(key)));
            descriptions.add(newApiDescription);
        }

        Option<scala.collection.immutable.Map<String, Model>> modelOptions = Option
                .apply(JavaToScalaUtil.toScalaImmutableMap(models));

        return new ApiListing(apiListing.apiVersion(), apiListing.swaggerVersion(), apiListing.basePath(),
                apiListing.resourcePath(), apiListing.produces(), apiListing.consumes(), apiListing.protocols(),
                apiListing.authorizations(), JavaToScalaUtil.toScalaList(descriptions), modelOptions,
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

    private boolean ignore(Method method) {
        for (Filter filter : filters) {
            if (filter.isApplicable(method) && filter.ignore(method)) {
                return true;
            }
        }
        return false;
    }

}
