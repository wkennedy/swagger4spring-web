package com.knappsack.swagger4springweb;

import com.knappsack.swagger4springweb.parser.ApiParser;
import com.knappsack.swagger4springweb.parser.ApiParserImpl;
import com.wordnik.swagger.model.ApiDescription;
import com.wordnik.swagger.model.ApiListing;
import junit.framework.*;
import org.junit.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import scala.Function1;
import scala.collection.Iterator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


public class ApiCategoryTest extends AbstractTest {

  @Test
  public void category() {

    ApiParser apiParser = createApiParser();
    Map<String, ApiListing> documents = apiParser.createApiListings();

    assertEquals("api keys expected", 2, documents.size());
    assertTrue(documents.containsKey("dark"));
    assertTrue(documents.containsKey("light"));

    scala.collection.immutable.List<ApiDescription> dark = documents.get("dark").apis();
//    assertEquals("dark colors expected", 2, dark.size());

    boolean foundBlack, foundBlue;
    for (Iterator<ApiDescription> it = dark.iterator(); it.hasNext();) {
      ApiDescription description = it.next();
      description.path().equals("sky");
      System.out.println(description.path());

    }

  }


  private ApiParser createApiParser() {
   return createApiParser(Arrays.asList(BASE_CONTROLLER_PACKAGE + ".category"));
  }

  private ApiParser createApiParser(List<String> controllerPackages) {
    return new ApiParserImpl(API_INFO, controllerPackages, "http://localhost:8080/api", "/api", "v1",
      new ArrayList<String>(), true, null);
  }

}
