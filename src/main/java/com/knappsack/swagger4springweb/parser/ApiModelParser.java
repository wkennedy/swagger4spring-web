package com.knappsack.swagger4springweb.parser;

import com.knappsack.swagger4springweb.util.ApiUtils;
import com.wordnik.swagger.model.Model;
import org.springframework.web.bind.annotation.ResponseBody;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class ApiModelParser {

    public Map<String, Model> getResponseBodyModels(Method method) {
        Map<String, Model> models = new HashMap<String, Model>();
        if (method.getAnnotation(ResponseBody.class) != null) {
            Type type = method.getGenericReturnType();

            ApiUtils.addModels(type, models);
        }

        return models;
    }
}
