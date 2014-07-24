package com.knappsack.swagger4springweb.util;

import com.google.common.collect.Lists;
import com.knappsack.swagger4springweb.model.AnnotatedParameter;
import com.thoughtworks.paranamer.BytecodeReadingParanamer;
import com.thoughtworks.paranamer.Paranamer;
import com.wordnik.swagger.annotations.ApiParam;
import org.springframework.web.bind.annotation.RequestMapping;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
     * @return List - if the method contains parameters
     *         annotated with ApiParam, RequestMapping, PathVariable, or
     *         RequestBody, this method will return a list of AnnotatedParameter
     *         objects based on the values of the parameter annotations
     */
    public static List<AnnotatedParameter> getAnnotatedParameters(Method method) {
        List<AnnotatedParameter> annotatedParameters = new ArrayList<>();
        Paranamer paranamer = new BytecodeReadingParanamer();
        String[] parameterNames;
        //Attempt to use Paranamer to look up the parameter names for those not using Java 8+.
        //This will fail if trying to evaluate a class using Lambdas, in which case fall back and look up using
        //standard java reflections.  This will provide the paramter name if using the -parameters javac argument.
        try {
            parameterNames = paranamer.lookupParameterNames(method);
        } catch(Exception e) {
            Parameter[] parameters = method.getParameters();
            parameterNames = new String[parameters.length];
            for (int i = 0; i < parameters.length; i++) {
                Parameter parameter = parameters[i];
                parameterNames[i] = parameter.getName();
            }
        }
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

    /**
     *
     * @param clazz Class - scan this class for methods with the specified annotation
     * @param annotationClass Class - return all methods with this annotation
     * @return Set - all methods of this class with the specified annotation
     */
    public static Set<Method> getAnnotatedMethods(Class<?> clazz, Class<? extends Annotation> annotationClass) {
        Method[] methods = clazz.getMethods();
        Set<Method> annotatedMethods = new HashSet<>(methods.length);
        for (Method method : methods) {
            if( method.isAnnotationPresent(annotationClass)){
                annotatedMethods.add(method);
            }
        }
        return annotatedMethods;
    }
}
