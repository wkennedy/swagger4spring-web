package com.knappsack.swagger4springweb.parser;


import com.knappsack.swagger4springweb.util.AnnotationUtils;
import com.wordnik.swagger.model.ApiDescription;
import scala.Option;

import java.lang.reflect.Method;

public class ApiDescriptionParser {

    /**
     * @param method Method - Controller method to investigate
     * @param description String - Description of this API
     * @param resourcePath String - the path of this API. For Spring MVC this would be the value of the RequestMapping
     * @return ApiDescription
     */
    public ApiDescription parseApiDescription(Method method, String description, String resourcePath) {
        String requestMappingValue = AnnotationUtils.getMethodRequestMappingValue(method);
        String path;
        if (resourcePath != null && !resourcePath.isEmpty()) {
            path = resourcePath + requestMappingValue;
        } else {
            path = requestMappingValue;
        }

        Option<String> descriptionOption = Option.apply(description);

        return new ApiDescription(path, descriptionOption, null);
    }
}
