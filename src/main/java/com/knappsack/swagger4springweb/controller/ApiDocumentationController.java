package com.knappsack.swagger4springweb.controller;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.knappsack.swagger4springweb.filter.Filter;
import com.knappsack.swagger4springweb.parser.ApiParser;
import com.knappsack.swagger4springweb.parser.ApiParserImpl;
import com.wordnik.swagger.model.ApiInfo;
import com.wordnik.swagger.model.ApiListing;
import com.wordnik.swagger.model.ResourceListing;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.HandlerMapping;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

@Controller
@RequestMapping(value = "/api")
public class ApiDocumentationController {

    public static final String REQUEST_FILTERS = ApiDocumentationController.class.getName() + ".requestFilters";

    private String baseControllerPackage = "";
    private List<String> additionalControllerPackages = new ArrayList<String>();

    /**
     * @deprecated no need in model packages
     */
    private String baseModelPackage = "";

    /**
     * @deprecated no need in model packages
     */
    private List<String> additionalModelPackages = new ArrayList<String>();
    private String basePath = "";
    private String apiVersion = "v1";
    private List<String> ignorableAnnotations = new ArrayList<String>();
    private boolean ignoreUnusedPathVariables = true;
    private boolean basePathFromReferer = false;
    private ApiInfo apiInfo;
    private List<Filter> filters;

    private final Map<Set<Filter>, Map<String, ApiListing>> documentationCache = Maps.newHashMap();
    private final Map<Set<Filter>, ResourceListing> resourceListingCache = Maps.newHashMap();

    @RequestMapping(value = "/resourceList", method = RequestMethod.GET, produces = "application/json")
    public
    @ResponseBody
    ResourceListing getResources(HttpServletRequest request) {
        return getResourceList(request);
    }

    @RequestMapping(value = "/resourceList/doc/**", method = RequestMethod.GET, produces = "application/json")
    public
    @ResponseBody
    ApiListing getDocumentation(HttpServletRequest request) {
        String handlerMappingPath = (String) request.getAttribute(
                HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        //trim the operation request mapping from the desired value
        handlerMappingPath = handlerMappingPath
                .substring(handlerMappingPath.lastIndexOf("/doc") + 4, handlerMappingPath.length());

        Map<String, ApiListing> docs = getDocs(request);
        if (docs == null) {
            //TODO throw exception
            return null;
        }

        return docs.get(handlerMappingPath);
    }

    @SuppressWarnings("unused")
    public String getBasePath() {
        if (basePath != null && !basePath.isEmpty()) {
            return basePath;
        }

        //If no base path was specified, attempt to get the base path from the request URL
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder
                .getRequestAttributes()).getRequest();
        if (request != null) {
            // requested from
            String referer = request.getHeader("Referer");

            if (basePathFromReferer && referer != null && !referer.isEmpty()) {
                return referer.substring(0, referer.lastIndexOf("/"));
            } else {
                String mapping = request.getServletPath();
                String requestURL = request.getRequestURL().toString();
                return requestURL.substring(0, requestURL.indexOf(mapping));
            }
        }
        throw new RuntimeException("Base path can't be constructed");
    }

    private Map<String, ApiListing> getDocs(HttpServletRequest request) {
        Set<Filter> fullSetOfFilters = mergeFilters(getRequestFilters(request));

        Map<String, ApiListing> documentation = documentationCache.get(fullSetOfFilters);
        if (documentation != null) {
            return documentation;
        }

        String servletPath = null;
        if (request != null) {
            servletPath = request.getServletPath();
        }
        ApiParser apiParser = new ApiParserImpl(apiInfo, getControllerPackages(), getBasePath(),
                servletPath, apiVersion, ignorableAnnotations, ignoreUnusedPathVariables, fullSetOfFilters);
        documentation = apiParser.createApiListings();
        documentationCache.put(fullSetOfFilters, documentation);

        return documentation;
    }

    private ResourceListing getResourceList(HttpServletRequest request) {
        Set<Filter> fullSetOfFilters = mergeFilters(getRequestFilters(request));

        ResourceListing resourceListing = resourceListingCache.get(fullSetOfFilters);
        if (resourceListing != null) {
            return resourceListing;
        }

        String servletPath = null;
        if (request != null) {
            servletPath = request.getServletPath();
            servletPath = servletPath.replace("/resourceList", "");
        }
        ApiParser apiParser = new ApiParserImpl(apiInfo, getControllerPackages(), getBasePath(),
                servletPath, apiVersion, ignorableAnnotations, ignoreUnusedPathVariables, fullSetOfFilters);
        resourceListing = apiParser.getResourceListing(getDocs(request));
        resourceListingCache.put(fullSetOfFilters, resourceListing);

        return resourceListing;
    }

    private List<String> getControllerPackages() {
        List<String> controllerPackages = new ArrayList<String>();
        if (baseControllerPackage != null && !baseControllerPackage.isEmpty()) {
            controllerPackages.add(baseControllerPackage);
        }

        if (additionalControllerPackages != null && !additionalControllerPackages.isEmpty()) {
            controllerPackages.addAll(additionalControllerPackages);
        }

        return controllerPackages;
    }

    private Set<Filter> mergeFilters(final Collection<Filter> requestFilters) {
        final Set<Filter> allFilters = Sets.newHashSet();
        if (filters != null) {
            allFilters.addAll(filters);
        }
        if (requestFilters != null) {
            allFilters.addAll(requestFilters);
        }
        return allFilters;
    }

    @SuppressWarnings("unused")
    public String getBaseControllerPackage() {
        return baseControllerPackage;
    }

    @SuppressWarnings("unused")
    public void setBaseControllerPackage(String baseControllerPackage) {
        this.baseControllerPackage = baseControllerPackage;
    }

    @SuppressWarnings("unused")
    public List<String> getAdditionalControllerPackages() {
        return additionalControllerPackages;
    }

    @SuppressWarnings("unused")
    public void setAdditionalControllerPackages(List<String> additionalControllerPackages) {
        this.additionalControllerPackages = additionalControllerPackages;
    }

    @SuppressWarnings("unused")
    public String getBaseModelPackage() {
        return baseModelPackage;
    }

    @SuppressWarnings("unused")
    public void setBaseModelPackage(String baseModelPackage) {
        this.baseModelPackage = baseModelPackage;
    }

    @SuppressWarnings("unused")
    public List<String> getAdditionalModelPackages() {
        return additionalModelPackages;
    }

    @SuppressWarnings("unused")
    public void setAdditionalModelPackages(List<String> additionalModelPackages) {
        this.additionalModelPackages = additionalModelPackages;
    }

    @SuppressWarnings("unused")
    public List<String> getIgnorableAnnotations() {
        return ignorableAnnotations;
    }

    @SuppressWarnings("unused")
    public void setIgnorableAnnotations(List<String> ignorableAnnotations) {
        this.ignorableAnnotations = ignorableAnnotations;
    }

    @SuppressWarnings("unused")
    public boolean isIgnoreUnusedPathVariables() {
        return ignoreUnusedPathVariables;
    }

    @SuppressWarnings("unused")
    public void setIgnoreUnusedPathVariables(final boolean ignoreUnusedPathVariables) {
        this.ignoreUnusedPathVariables = ignoreUnusedPathVariables;
    }

    @SuppressWarnings("unused")
    public void setBasePathFromReferer(final boolean basePathFromReferer) {
        this.basePathFromReferer = basePathFromReferer;
    }

    @SuppressWarnings("unused")
    public ApiInfo getApiInfo() {
        return apiInfo;
    }

    @SuppressWarnings("unused")
    public void setApiInfo(ApiInfo apiInfo) {
        this.apiInfo = apiInfo;
    }

    @SuppressWarnings("unused")
    public void setFilters(final List<Filter> filters) {
        this.filters = filters;
    }

    @SuppressWarnings("unused")
    public void setBasePath(final String basePath) {
        this.basePath = basePath;
    }

    @SuppressWarnings("unused")
    public void setApiVersion(final String apiVersion) {
        this.apiVersion = apiVersion;
    }

    public static void setRequestFilters(final HttpServletRequest request, final Collection<Filter> requestFilters) {
        request.setAttribute(REQUEST_FILTERS, requestFilters);
    }

    public static Collection<Filter> getRequestFilters(final HttpServletRequest request) {
        return (Collection<Filter>) request.getAttribute(REQUEST_FILTERS);
    }
}
