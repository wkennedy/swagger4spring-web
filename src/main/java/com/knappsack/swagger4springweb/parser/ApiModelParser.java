package com.knappsack.swagger4springweb.parser;

import com.knappsack.swagger4springweb.util.ModelUtils;
import com.wordnik.swagger.model.Model;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Map;

public class ApiModelParser {

    private final Map<String, Model> models;

    public ApiModelParser(final Map<String, Model> models) {
        this.models = models;
    }

    /**
     * @param method Method for which to evaluate the return type
     */
    public void parseResponseBodyModels(Method method) {
        //In Spring 4, the RestController annotation specifies that the class is a Controller and the return type
        //is automatically assumed to be the ResponseBody, therefore no ResponseBody annotation is needed when marked
        //as a RestController
        boolean isRestController = method.getDeclaringClass().getAnnotation(RestController.class) != null;
        if (method.getAnnotation(ResponseBody.class) != null || isRestController) {
            Type type = method.getGenericReturnType();

            ModelUtils.addModels(type, models);
        }
    }
}
