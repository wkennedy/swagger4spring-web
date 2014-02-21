package com.knappsack.swagger4springweb.parser;

import com.knappsack.swagger4springweb.util.ModelUtils;
import com.wordnik.swagger.model.Model;
import org.springframework.web.bind.annotation.ResponseBody;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Map;

public class ApiModelParser {

    private final Map<String, Model> models;

    public ApiModelParser(final Map<String, Model> models) {
        this.models = models;
    }

    public void parseResponseBodyModels(Method method) {
        if (method.getAnnotation(ResponseBody.class) != null) {
            Type type = method.getGenericReturnType();

            ModelUtils.addModels(type, models);
        }
    }
}
