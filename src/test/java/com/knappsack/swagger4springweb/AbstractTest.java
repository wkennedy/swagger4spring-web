package com.knappsack.swagger4springweb;

import com.wordnik.swagger.model.ApiInfo;

import java.util.Arrays;
import java.util.List;

public abstract class AbstractTest {

    public static final String BASE_CONTROLLER_PACKAGE = "com.knappsack.swagger4springweb.testController";
    public static final String EXCLUDE_LABEL = "exclude";
    public static final List<String> END_POINT_PATHS = Arrays.asList("/doc/api/v1/partialExclude", "/doc/api/v1/test", "/doc/api/v1/exclude2", "/doc/com.knappsack.swagger4springweb.testController.NoClassLevelMappingController");
    public static final ApiInfo API_INFO = new ApiInfo("swagger4spring-web example app", "This is a basic web app for demonstrating swagger4spring-web", "http://localhost:8080/terms", "http://localhost:8080/contact", "MIT", "http://opensource.org/licenses/MIT");
}
