package com.knappsack.swagger4springweb.model;

import java.lang.annotation.Annotation;

public class AnnotatedParameter {

    private String parameterName;
    private Class<?> parameterType;
    private Annotation annotation;

    public String getParameterName() {
        return parameterName;
    }

    public void setParameterName(String parameterName) {
        this.parameterName = parameterName;
    }

    public Class<?> getParameterType() {
        return parameterType;
    }

    public void setParameterType(Class<?> parameterType) {
        this.parameterType = parameterType;
    }

    public Annotation getAnnotation() {
        return annotation;
    }

    public void setAnnotation(Annotation annotation) {
        this.annotation = annotation;
    }
}
