package com.knappsack.swagger4springweb.util;

import org.springframework.web.bind.annotation.ValueConstants;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collection;

/**
 * Author: andrey.antonov
 * Date: 7/3/13
 */
public class DocumentationUtils {

    public static String getSwaggerTypeFor(Class<?> parameterType) {
        Class type = parameterType;
        if(parameterType.isArray()) {
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
        } else if(Byte.class.isAssignableFrom(type)) {
            return "byte";
        }  else if(Long.class.isAssignableFrom(type)) {
            return "long";
        }  else if(Integer.class.isAssignableFrom(type)) {
            return "int";
        }  else if(Float.class.isAssignableFrom(type)) {
            return "float";
        } else if(MultipartFile.class.isAssignableFrom(type)) {
            return "file";
        } else if (Number.class.isAssignableFrom(type)) {
            return "double";
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
}
