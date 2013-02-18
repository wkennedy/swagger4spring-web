package com.sparc.swagger4springweb.util;

import com.sparc.swagger4springweb.model.AnnotatedParameter;
import com.thoughtworks.paranamer.BytecodeReadingParanamer;
import com.thoughtworks.paranamer.Paranamer;
import com.wordnik.swagger.annotations.ApiParam;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class AnnotationUtils {

    public static boolean hasApiParam(Method method) {
        Annotation[][] parameterAnnotations = method.getParameterAnnotations();
        for (Annotation[] parameterAnnotation : parameterAnnotations) {
            for (Annotation annotation : parameterAnnotation) {
                if (annotation instanceof ApiParam) {
                    return true;

                }
            }
        }
        return false;
    }

    public static boolean hasApiParam(Annotation[] annotations) {
        for (Annotation annotation : annotations) {
            if (annotation instanceof ApiParam) {
                return true;
            }
        }
        return false;
    }

    public static String getMethodRequestMappingValue(Method method) {
        RequestMapping requestMapping = method.getAnnotation(RequestMapping.class);
        String requestMappingValue = "/";
        String[] requestMappingValues = requestMapping.value();
        if (requestMappingValues != null && requestMappingValues.length > 0) {
            requestMappingValue = requestMappingValues[0];
        }

        return requestMappingValue;
    }

    public static List<AnnotatedParameter> getAnnotatedParameters(Method method) {
        List<AnnotatedParameter> annotatedParameters = new ArrayList<AnnotatedParameter>();
        Paranamer paranamer = new BytecodeReadingParanamer();
        String[] parameterNames = paranamer.lookupParameterNames(method);
        Annotation[][] parameterAnnotations = method.getParameterAnnotations();
        Class[] parameterTypes = method.getParameterTypes();

        int i = 0;
        for (Annotation[] annotations : parameterAnnotations) {
            Class parameterType = parameterTypes[i];
            String parameterName = parameterNames[i];
            i++;
            for (Annotation annotation : annotations) {
                if (annotation instanceof ApiParam) {
                    AnnotatedParameter annotatedParameter = new AnnotatedParameter();
                    annotatedParameter.setAnnotation(annotation);
                    annotatedParameter.setParameterType(parameterType);
                    annotatedParameter.setParameterName(parameterName);
                    annotatedParameters.add(annotatedParameter);
                    break;
                } else if (!hasApiParam(annotations) &&
                        (annotation instanceof RequestMapping ||
                        annotation instanceof PathVariable ||
                        annotation instanceof RequestBody)) {
                    AnnotatedParameter annotatedParameter = new AnnotatedParameter();
                    annotatedParameter.setAnnotation(annotation);
                    annotatedParameter.setParameterType(parameterType);
                    annotatedParameter.setParameterName(parameterName);
                    annotatedParameters.add(annotatedParameter);
                    break;
                }
            }
        }
        return annotatedParameters;
    }
}
