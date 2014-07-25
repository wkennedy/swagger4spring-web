package com.knappsack.swagger4springweb.util;

import com.wordnik.swagger.converter.ModelConverters;
import com.wordnik.swagger.model.Model;
import org.springframework.web.bind.annotation.ValueConstants;
import org.springframework.web.multipart.MultipartFile;
import scala.Option;
import scala.collection.immutable.HashMap;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Date;
import java.util.Map;

public class ModelUtils {

    public static String getSwaggerTypeFor(Class<?> parameterType) {
        Class type = parameterType;
        if (parameterType.isArray()) {
            type = type.getComponentType();
        }
        // swagger types are
        // byte
        // boolean
        // int
        // long
        // float
        // double
        // string
        // Date
        if (String.class.isAssignableFrom(type)) {
            return "string";
        } else if (Boolean.class.isAssignableFrom(type)) {
            return "boolean";
        } else if (Byte.class.isAssignableFrom(type)) {
            return "byte";
        } else if (Long.class.isAssignableFrom(type)) {
            return "int64";
        } else if (Integer.class.isAssignableFrom(type)) {
            return "int32";
        } else if (Float.class.isAssignableFrom(type)) {
            return "float";
        } else if (MultipartFile.class.isAssignableFrom(type)) {
            return "file";
        } else if (Number.class.isAssignableFrom(type)) {
            return "double";
        } else if (Double.class.isAssignableFrom(type)) {
            return "double";
        } else if (Date.class.isAssignableFrom(type)) {
            return "date";
        }
        // others
        return type.getSimpleName();
    }

    public static boolean isSet(String value) {
        return value != null && !value.trim().isEmpty() && !ValueConstants.DEFAULT_NONE.equals(value);
    }

    public static boolean isAllowMultiple(Class parameterType) {
        return parameterType != null && (parameterType.isArray() || Collection.class.isAssignableFrom(parameterType));
    }

    static boolean isIgnorableModel(String name) {
        return name.equalsIgnoreCase("map") || name.equalsIgnoreCase("list") || name.equalsIgnoreCase("string")
                || name.equalsIgnoreCase("set") || name.equalsIgnoreCase("collection");
    }

    public static void addModels(final Class<?> clazz, final Map<String, Model> models) {
        Option<Model> modelOption = ModelConverters.read(clazz, new HashMap<>());
        Model model;
        if(!modelOption.isEmpty()) {
           model = modelOption.get();
            if(!isIgnorableModel(model.name())) {
                models.put(model.name(), model);
            }
        }
    }

    public static void addModels(final Type type, final Map<String, Model> models) {
        if (type instanceof ParameterizedType) {
            // Adding both part of generic type
            final ParameterizedType parameterizedType = (ParameterizedType) type;
            addModels(parameterizedType.getRawType(), models);
            for (Type t : parameterizedType.getActualTypeArguments()) {
                addModels(t, models);
            }
        } else if (type instanceof Class<?>) {
            addModels((Class<?>) type, models);
        }
    }
}
