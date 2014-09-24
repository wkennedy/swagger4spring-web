package com.knappsack.swagger4springweb;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.knappsack.swagger4springweb.parser.ApiParser;
import com.knappsack.swagger4springweb.parser.ApiParserImpl;
import com.wordnik.swagger.model.ApiDescription;
import com.wordnik.swagger.model.ApiListing;
import com.wordnik.swagger.model.ApiListingReference;
import com.wordnik.swagger.model.ResourceListing;

import scala.collection.Iterator;


public class ApiCategoryTest extends AbstractTest {

  @Test
  public void category() {

    ApiParser apiParser = createApiParser();
    Map<String, ApiListing> documents = apiParser.createApiListings();

    assertEquals("api keys expected", 3, documents.size());
    assertTrue(documents.containsKey("/dark"));
    assertTrue(documents.containsKey("/light"));

    ApiListing darkApiListing = documents.get("/dark");

    scala.collection.immutable.List<ApiDescription> dark = darkApiListing.apis();
    assertEquals("dark colors expected", 3, dark.size());

    // Validate alphabetic ordering
    assertTrue("earth should be first", dark.apply(0).path().equals("/api/v1/black/earth"));
    assertTrue("x-planet should be second", dark.apply(1).path().equals("/api/v1/black/x-planet"));
    assertTrue("sky should be third", dark.apply(2).path().equals("/api/v1/blue/sky"));

    assertEquals("This is so dark", darkApiListing.description().get());

  }

  @Test
  public void resourceListing() {
    ApiParser apiParser = createApiParser();
    ResourceListing resourceListing = apiParser.getResourceListing(apiParser.createApiListings());
    assertEquals(3, resourceListing.apis().size());
    scala.collection.immutable.List<ApiListingReference> apis = resourceListing.apis();


    boolean foundDark = false;
    boolean foundLight = false;
    boolean foundNoColor = false;
    for (Iterator<ApiListingReference> it = apis.iterator(); it.hasNext(); ) {

      ApiListingReference apiListingReference = it.next();
      if (apiListingReference.path().equals("/doc/dark")) foundDark = true;
      if (apiListingReference.path().equals("/doc/light")) foundLight = true;
      if (apiListingReference.path().equals("/doc/api/v1/nocolor")) foundNoColor = true;
    }
    assertTrue("found dark category", foundDark);
    assertTrue("found light category", foundLight);
    assertTrue("found no color category", foundNoColor);
  }


  private ApiParser createApiParser() {
    return createApiParser(Arrays.asList(BASE_CONTROLLER_PACKAGE + ".category"));
  }

  private ApiParser createApiParser(List<String> controllerPackages) {
    return new ApiParserImpl(API_INFO, controllerPackages, "http://localhost:8080/api", "/api", "v1",
        new ArrayList<String>(), true, null);
  }

}
