package com.knappsack.swagger4springweb.util;

import com.google.common.collect.Lists;
import com.knappsack.swagger4springweb.model.AnnotatedParameter;
import com.thoughtworks.paranamer.BytecodeReadingParanamer;
import com.thoughtworks.paranamer.Paranamer;
import com.wordnik.swagger.annotations.ApiImplicitParam;
import com.wordnik.swagger.annotations.ApiImplicitParams;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import com.wordnik.swagger.model.AllowableListValues;
import com.wordnik.swagger.model.AllowableValues;
import com.wordnik.swagger.model.Parameter;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import scala.Option;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
        final ApiOperation apiOperation = findApiOperation(Lists.newArrayList(method.getDeclaredAnnotations()));

        int i = 0;
        for (Annotation[] annotations : parameterAnnotations) {
            if (annotations.length > 0) {
                if (containsModelAttribute(annotations)
                        && !apiOperation.consumes().equals(MediaType.APPLICATION_FORM_URLENCODED_VALUE)) {
                    addModelAttributeParameters(parameterTypes[i], annotatedParameters);
                } else {
                    AnnotatedParameter annotatedParameter = createParameter(parameterNames[i], parameterTypes[i],
                            genericParameterTypes[i], Lists.newArrayList(annotations));

                    annotatedParameters.add(annotatedParameter);
                }
            }
            i++;
        }
        return annotatedParameters;
    }

    /**
     * @param method
     *            Method
     * @return List<Parameter> - if the method is annotated with ApiImplicitParams that contain parameters definitions
     */
    public static List<Parameter> getImplicitParameters(Method method) {
        final List<Parameter> parameters = Lists.newArrayList();
        final ApiImplicitParams apiImplicitParams = findApiImplicitParams(
                Lists.newArrayList(method.getDeclaredAnnotations()));
        if (apiImplicitParams != null) {
            for (final ApiImplicitParam apiImplicitParam : apiImplicitParams.value()) {
                parameters.add(new Parameter(apiImplicitParam.name(), Option.apply(apiImplicitParam.value()),
                        Option.apply(apiImplicitParam.defaultValue()),
                        apiImplicitParam.required(), apiImplicitParam.allowMultiple(),
                        getDataType(apiImplicitParam.dataType()), getAllowableValues(apiImplicitParam),
                        apiImplicitParam.paramType(), Option.apply(apiImplicitParam.access())));
            }
        }
        return parameters;
    }

    private static String getDataType(String dataType) {
        return dataType == null ? null : dataType.toLowerCase();
    }

    private static AllowableValues getAllowableValues(ApiImplicitParam apiImplicitParam) {
        if (ModelUtils.isSet(apiImplicitParam.allowableValues())) {
            List<String> allowableValues = Arrays.asList(apiImplicitParam.allowableValues().split("\\s*,\\s*"));
            return new AllowableListValues(JavaToScalaUtil.toScalaList(allowableValues), "LIST");
        }
        return null;
    }

    private static void addModelAttributeParameters(final Class type,
                                                    final List<AnnotatedParameter> annotatedParameters) {
        List<Field> fields = new ArrayList<Field>();
        fields = getAllFields(fields, type);

        for (final Field field : fields) {
            List<Annotation> annotations = new ArrayList<Annotation>(Arrays.asList(field.getDeclaredAnnotations()));
            annotations.add(createRequestParamAnnotation());
            annotatedParameters.add(createParameter(field.getName(), field.getType(),
                    field.getGenericType(), annotations));
        }
    }

    private static Annotation createRequestParamAnnotation() {
        return new RequestParam() {
            @Override
            public String value() {
                return "";
            }

            @Override
            public boolean required() {
                return false;
            }

            @Override
            public String defaultValue() {
                return "";
            }

            @Override
            public Class<? extends Annotation> annotationType() {
                return RequestParam.class;
            }
        };
    }

    private static List<Field> getAllFields(List<Field> fields, Class<?> type) {
        for (Field field : type.getDeclaredFields()) {
            fields.add(field);
        }

        if (type.getSuperclass() != null && type.getSuperclass() != Object.class) {
            fields = getAllFields(fields, type.getSuperclass());
        }

        return fields;
    }


    private static boolean containsModelAttribute(final Annotation[] annotations) {
         for (final Annotation item : annotations) {
               if (item instanceof ModelAttribute) {
                 return true;
               }
         }
        return false;
    }

    private static ApiOperation findApiOperation(final List<Annotation> annotations) {
        for (final Annotation item : annotations) {
            if (item instanceof ApiOperation) {
                return (ApiOperation) item;
            }
        }
        return null;
    }

    private static ApiImplicitParams findApiImplicitParams(final List<Annotation> annotations) {
        for (final Annotation item : annotations) {
            if (item instanceof ApiImplicitParams) {
                return (ApiImplicitParams) item;
            }
        }
        return null;
    }



    private static AnnotatedParameter createParameter(final String paramName, final Class paramType,
                                                      final Type genericParameterType,
                                                      final List<Annotation> annotations) {
        AnnotatedParameter annotatedParameter = new AnnotatedParameter();
        annotatedParameter.setParameterClass(paramType);
        annotatedParameter.setParameterName(paramName);
        annotatedParameter.setParameterType(genericParameterType);
        annotatedParameter.addAnnotations(annotations);
        return annotatedParameter;
    }
}
