package com.knappsack.swagger4springweb.controller;

import com.knappsack.swagger4springweb.AbstractTest;
import com.wordnik.swagger.core.Documentation;
import com.wordnik.swagger.core.DocumentationEndPoint;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.servlet.HandlerMapping;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class ApiDocumentationControllerTest extends AbstractTest {

    @Test
    public void getDocumentation() {
        MockHttpServletRequest servletRequest = new MockHttpServletRequest();
        servletRequest.setAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE, "/api/doc/api/v1/test");
        ApiDocumentationController apiDocumentationController = new ApiDocumentationController();
        apiDocumentationController.setApiVersion("v1");
        apiDocumentationController.setBaseControllerPackage(BASE_CONTROLLER_PACKAGE);
        apiDocumentationController.setBaseModelPackage(BASE_MODEL_PACKAGE);
        apiDocumentationController.setBasePath("http://localhost/swagger4spring-web-example");
        Documentation documentation = apiDocumentationController.getDocumentation(servletRequest);
        assertNotNull(documentation);
        assertEquals(documentation.getApiVersion(), "v1");
        assertEquals(documentation.getResourcePath(), "/api/v1/test");
    }

    @Test
    public void getResourceList() {
        MockHttpServletRequest servletRequest = new MockHttpServletRequest();
        servletRequest.setAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE, "/api/doc/api/v1/test");
        ApiDocumentationController apiDocumentationController = new ApiDocumentationController();
        apiDocumentationController.setApiVersion("v1");
        apiDocumentationController.setBaseControllerPackage(BASE_CONTROLLER_PACKAGE);
        apiDocumentationController.setBaseModelPackage(BASE_MODEL_PACKAGE);
        apiDocumentationController.setBasePath("http://localhost/swagger4spring-web-example");

        Documentation documentation = apiDocumentationController.getResources(servletRequest);

        assertNotNull(documentation);
        assertEquals("v1", documentation.apiVersion());
        assertEquals(END_POINT_PATHS.size(), documentation.getApis().size());
        assertEquals("/api/doc/api/v1/test", documentation.getApis().get(0).getPath());

        for (DocumentationEndPoint endPoint : documentation.getApis()) {
            assertTrue(END_POINT_PATHS.contains(endPoint.getPath()));
        }
    }
}
