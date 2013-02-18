package com.sparc.swagger4springweb.controller;

import com.sparc.swagger4springweb.parser.ApiParser;
import com.sparc.swagger4springweb.parser.ApiParserImpl;
import com.wordnik.swagger.core.Documentation;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.HandlerMapping;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@Controller
@RequestMapping(value = "/api")
public class ApiDocumentationController {

    public static final String CONTROLLER_MAPPING = "/api";

    private String baseControllerPackage = "";
    private String baseModelPackage = "";
    private String basePath = "";
    private String apiVersion = "v1";
    private Documentation resourceList;
    private Map<String, Documentation> documentation;

    @RequestMapping(value = "/resourceList", method = RequestMethod.GET, produces = "application/json")
    public
    @ResponseBody
    Documentation getResources() {
        return getResourceList();
    }

    @RequestMapping(value = "/doc/**", method = RequestMethod.GET, produces = "application/json")
    public
    @ResponseBody
    Documentation getDocumentation(HttpServletRequest request) {
        String handlerMappingPath = (String) request.getAttribute(
                HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        //trim the operation request mapping from the desired value
        String pathVariable = handlerMappingPath.replaceFirst("/api/doc", "");
        if (getDocs() == null) {
            return new Documentation();
        }

        return getDocs().get(pathVariable);
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
    public String getBaseModelPackage() {
        return baseModelPackage;
    }

    @SuppressWarnings("unused")
    public void setBaseModelPackage(String baseModelPackage) {
        this.baseModelPackage = baseModelPackage;
    }

    @SuppressWarnings("unused")
    public Map<String, Documentation> getDocumentation() {
        return documentation;
    }

    @SuppressWarnings("unused")
    public void setDocumentation(Map<String, Documentation> documentation) {
        this.documentation = documentation;
    }

    @SuppressWarnings("unused")
    public String getBasePath() {
        if (basePath == null || basePath.isEmpty()) {
            //If no base path was specified, attempt to get the base path from the request URL
            HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder
                    .getRequestAttributes()).getRequest();
            if(request != null) {
                basePath = request.getRequestURL().toString();
                basePath = basePath.substring(0, basePath.indexOf(CONTROLLER_MAPPING));
            }
        }
        return basePath;
    }

    @SuppressWarnings("unused")
    public void setBasePath(String basePath) {
        this.basePath = basePath;
    }

    @SuppressWarnings("unused")
    public String getApiVersion() {
        return apiVersion;
    }

    @SuppressWarnings("unused")
    public void setApiVersion(String apiVersion) {
        this.apiVersion = apiVersion;
    }

    private Map<String, Documentation> getDocs() {
        if (this.documentation == null) {
            ApiParser apiParser = new ApiParserImpl(baseControllerPackage, baseModelPackage, getBasePath(), apiVersion);
            this.documentation = apiParser.createDocuments();
        }
        return documentation;
    }

    private Documentation getResourceList() {
        if (this.resourceList == null) {
            ApiParser apiParser = new ApiParserImpl(baseControllerPackage, baseModelPackage, getBasePath(), apiVersion);
            this.resourceList = apiParser.getResourceListing(getDocs());
        }
        return resourceList;
    }

    @SuppressWarnings("unused")
    public void setResourceList(Documentation resourceList) {
        this.resourceList = resourceList;
    }
}
