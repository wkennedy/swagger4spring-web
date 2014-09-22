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

import scala.collection.Iterator;


public class ApiCategoryTest extends AbstractTest {

  @Test
  public void category() {

    ApiParser apiParser = createApiParser();
    Map<String, ApiListing> documents = apiParser.createApiListings();

    assertEquals("api keys expected", 2, documents.size());
    assertTrue(documents.containsKey("dark"));
    assertTrue(documents.containsKey("light"));

    scala.collection.immutable.List<ApiDescription> dark = documents.get("dark").apis();
    assertEquals("dark colors expected", 2, dark.size());

    boolean foundBlack = false, foundBlue = false;
    for (Iterator<ApiDescription> it = dark.iterator(); it.hasNext();) {
      ApiDescription description = it.next();

      if (description.operations().iterator().next().nickname().equals("sky")) {
        foundBlue = true;
      }
      if (description.operations().iterator().next().nickname().equals("earth")) {
        foundBlack = true;
      }
    }

    assertTrue("found black", foundBlack);
    assertTrue("found blue", foundBlue);

  }


  private ApiParser createApiParser() {
   return createApiParser(Arrays.asList(BASE_CONTROLLER_PACKAGE + ".category"));
  }

  private ApiParser createApiParser(List<String> controllerPackages) {
    return new ApiParserImpl(API_INFO, controllerPackages, "http://localhost:8080/api", "/api", "v1",
      new ArrayList<String>(), true, null);
  }

}
