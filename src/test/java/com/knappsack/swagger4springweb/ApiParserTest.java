package com.knappsack.swagger4springweb;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.knappsack.swagger4springweb.parser.ApiParser;
import com.knappsack.swagger4springweb.parser.ApiParserImpl;
import com.wordnik.swagger.core.Documentation;
import com.wordnik.swagger.core.DocumentationEndPoint;
import com.wordnik.swagger.core.DocumentationOperation;
import com.wordnik.swagger.core.SwaggerSpec;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class ApiParserTest extends AbstractTest {
    @Test
    public void testParseControllerDocumentation() {
        Map<String, Documentation> documentList = createApiParser().createDocuments();
        for (String key : documentList.keySet()) {
            Documentation documentation = documentList.get(key);
            ObjectMapper mapper = new ObjectMapper();
            try {
                String documentationAsJSON = mapper.writeValueAsString(documentation);
                System.out.println(documentationAsJSON);
                Documentation documentationDeserialized = mapper.readValue(documentationAsJSON, Documentation.class);
                assertNotNull(documentationDeserialized);
                assertTrue(documentationDeserialized.swaggerVersion().equals(SwaggerSpec.version()));
                assertTrue(documentationDeserialized.getApiVersion().equals("v1"));
                assertTrue(documentationDeserialized.getBasePath().equals("http://localhost:8080/api"));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Test
    public void testOperationApiExclude() {
        ApiParser apiParser = createApiParser();
        Map<String, Documentation> documents = apiParser.createDocuments();

        // validate that we don't expose any excluded operations in the documents
        for (Documentation documentation : documents.values()) {
            for (DocumentationEndPoint api : documentation.getApis()) {
                for (DocumentationOperation op : api.getOperations()) {
                    assertFalse("The operation " + op.getNickname() + " should be excluded",
                            "excluded".equals(op.getSummary()));
                }
            }
        }
    }

    @Test
    public void testResourceListing() {
        ApiParser apiParser = createApiParser();
        Map<String, Documentation> documentList = apiParser.createDocuments();
        Documentation resourceList = apiParser.getResourceListing(documentList);

        assertEquals("http://localhost:8080/api", resourceList.basePath());
        assertEquals("v1", resourceList.apiVersion());
        assertEquals(END_POINT_PATHS.size(), resourceList.getApis().size());

        for (DocumentationEndPoint endPoint : resourceList.getApis()) {
            assertTrue("did u add a new controller without updating the endPoint paths ???", END_POINT_PATHS.contains(endPoint.getPath()));
        }
    }

    private ApiParser createApiParser() {
        return createApiParser(Arrays.asList(BASE_CONTROLLER_PACKAGE), Arrays.asList(BASE_MODEL_PACKAGE));
    }

    private ApiParser createApiParser(List<String> controllerPackages, List<String> modelPackages) {
        return new ApiParserImpl(controllerPackages, modelPackages, "http://localhost:8080/api", "/api", "v1", new ArrayList<String>(), true);
    }
}
