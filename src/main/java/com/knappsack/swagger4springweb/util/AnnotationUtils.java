package com.knappsack.swagger4springweb.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import org.springframework.web.bind.annotation.RequestMapping;

import com.google.common.collect.Lists;
import com.knappsack.swagger4springweb.model.AnnotatedParameter;
import com.thoughtworks.paranamer.BytecodeReadingParanamer;
import com.thoughtworks.paranamer.Paranamer;
import com.wordnik.swagger.annotations.ApiParam;

public class AnnotationUtils {

    /**
     * @param method
     *            Method - check to see if this method has a parameter annotated
     *            with @ApiParam
     * @return boolean true if this method has a parameter annotated with @ApiParam
     */
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

    /**
     * @param method
     *            Method - get the request mapping for this method
     * @return String - the value of the RequestMapping annotation on this
     *         method
     */
    public static String getMethodRequestMappingValue(Method method) {
        RequestMapping requestMapping = method
                .getAnnotation(RequestMapping.class);
        String requestMappingValue = "";
        if (requestMapping != null) {
            String[] requestMappingValues = requestMapping.value();
            if (requestMappingValues != null && requestMappingValues.length > 0) {
                requestMappingValue = requestMappingValues[0];
            }
        }

        return requestMappingValue;
    }

    /**
     * @param method
     *            Method
     * @return List<AnnotatedParameter> - if the method contains parameters
     *         annotated with ApiParam, RequestMapping, PathVariable, or
     *         RequestBody, this method will return a list of AnnotatedParameter
     *         objects based on the values of the parameter annotations
     */
    public static List<AnnotatedParameter> getAnnotatedParameters(Method method) {
        List<AnnotatedParameter> annotatedParameters = new ArrayList<AnnotatedParameter>();
        Paranamer paranamer = new BytecodeReadingParanamer();
        String[] parameterNames = paranamer.lookupParameterNames(method);
        Annotation[][] parameterAnnotations = method.getParameterAnnotations();
        Class[] parameterTypes = method.getParameterTypes();
        Type[] genericParameterTypes = method.getGenericParameterTypes();

        int i = 0;
        for (Annotation[] annotations : parameterAnnotations) {
            if (annotations.length > 0) {
                AnnotatedParameter annotatedParameter = new AnnotatedParameter();
                annotatedParameter.setParameterClass(parameterTypes[i]);
                annotatedParameter.setParameterName(parameterNames[i]);
                annotatedParameter.setParameterType(genericParameterTypes[i]);
                annotatedParameter.addAnnotations(Lists.newArrayList(annotations));
                annotatedParameters.add(annotatedParameter);
            }
            i++;
        }
        return annotatedParameters;
    }


  public static <T extends Annotation> T getAnnotationAnnotation(Class<T> annotationClass, Class<?> controllerClass) {
    Annotation[] annotations = controllerClass.getAnnotations();
    for (Annotation annotation : annotations) {
      T value = getAnnotation(annotation, annotationClass);
      if (value != null) {
        return value;
      }
    }
    return null;
  }

  private static <T extends Annotation> T getAnnotation(Annotation ann, Class<T> annotationType) {
    if (annotationType.isInstance(ann)) {
      return (T) ann;
    }
    return ann.annotationType().getAnnotation(annotationType);
  }
}
