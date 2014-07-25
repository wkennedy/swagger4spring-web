package com.knappsack.swagger4springweb;

import com.knappsack.swagger4springweb.parser.ApiParser;
import com.knappsack.swagger4springweb.parser.ApiParserImpl;
import com.knappsack.swagger4springweb.util.ScalaObjectMapper;
import com.knappsack.swagger4springweb.util.ScalaToJavaUtil;
import com.wordnik.swagger.core.SwaggerSpec;
import com.wordnik.swagger.core.util.JsonSerializer;
import com.wordnik.swagger.model.ApiDescription;
import com.wordnik.swagger.model.ApiListing;
import com.wordnik.swagger.model.ApiListingReference;
import com.wordnik.swagger.model.ResourceListing;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

public class ApiParserTest extends AbstractTest {

    @Test
    public void testParseControllerDocumentation() {
        Map<String, ApiListing> documentList = createApiParser().createApiListings();
        for (String key : documentList.keySet()) {
            ApiListing documentation = documentList.get(key);
            ScalaObjectMapper mapper = new ScalaObjectMapper();
            try {
                String documentationAsJSON = mapper.writeValueAsString(documentation);
                System.out.println(documentationAsJSON);
                ApiListing documentationDeserialized = JsonSerializer.asApiListing(documentationAsJSON);
                assertNotNull(documentationDeserialized);
                assertTrue(documentationDeserialized.swaggerVersion().equals(SwaggerSpec.version()));
                assertTrue(documentationDeserialized.apiVersion().equals("v1"));
                assertTrue(documentationDeserialized.basePath().equals("http://localhost:8080/api"));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Test
    public void testOperationApiExclude() {
        ApiParser apiParser = createApiParser(Arrays.asList(BASE_CONTROLLER_PACKAGE + ".exclude"));
        Map<String, ApiListing> documents = apiParser.createApiListings();

        assertEquals(1, documents.size()); // ExcludeClassTestController excluded completely

        // validate that we don't expose any excluded operations in the documents
        for (ApiListing documentation : documents.values()) {
            assertTrue(ScalaToJavaUtil.toJavaList(documentation.apis()).size() == 2);
            for (ApiDescription api : ScalaToJavaUtil.toJavaList(documentation.apis())) {
                assertTrue(ScalaToJavaUtil.toJavaList(api.operations()).size() == 1);
            }
        }
    }

    @Test
    public void testResourceListing() {
        ApiParser apiParser = createApiParser();
        Map<String, ApiListing> documentList = apiParser.createApiListings();
        ResourceListing resourceList = apiParser.getResourceListing(documentList);

//        assertEquals("http://localhost:8080/api", resourceList.basePath());
//        assertEquals("v1", resourceList.apiVersion());
//        assertEquals(END_POINT_PATHS.size(), resourceList.getApis().size());
//
//        for (DocumentationEndPoint endPoint : resourceList.getApis()) {
//            assertTrue("did u add a new controller without updating the endPoint paths ???", END_POINT_PATHS.contains(endPoint.getPath()));
//        }
    }

    @Test
    public void testNoClassRequestMapping() {
        ApiParser apiParser = createApiParser();
        Map<String, ApiListing> documentList = apiParser.createApiListings();
        ResourceListing resourceList = apiParser.getResourceListing(documentList);
        for (ApiListingReference api: ScalaToJavaUtil.toJavaList(resourceList.apis())) {
            assertNotNull("could not get api listing for path: " + api.path(), documentList.get(api.path().substring(4))); // each api should be accessible using the ApiListingReference minus /doc
        }
    }

    private ApiParser createApiParser() {
        return createApiParser(Arrays.asList(BASE_CONTROLLER_PACKAGE));
    }

    private ApiParser createApiParser(List<String> controllerPackages) {
        return new ApiParserImpl(API_INFO, controllerPackages, "http://localhost:8080/api", "/api", "v1",
                new ArrayList<String>(), true, null);
    }
}
