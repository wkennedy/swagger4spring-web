package com.knappsack.swagger4springweb;

import java.util.Arrays;
import java.util.List;

public abstract class AbstractTest {
    public static final String BASE_CONTROLLER_PACKAGE = "com.knappsack.swagger4springweb.testController";
    public static final String BASE_MODEL_PACKAGE = "com.knappsack.swagger4springweb.testModels";
    public static final String EXCLUDE_LABEL = "exclude";
    public static final List<String> END_POINT_PATHS = Arrays.asList("/api/doc/api/v1/partialExclude", "/api/doc/api/v1/test");
}
