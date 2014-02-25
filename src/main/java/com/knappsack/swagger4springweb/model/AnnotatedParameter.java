package com.knappsack.swagger4springweb.model;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class AnnotatedParameter {

    private String parameterName;
    private Class<?> parameterClass;
    private Type parameterType;
    private List<Annotation> annotations = new ArrayList<Annotation>();

    public String getParameterName() {
        return parameterName;
    }

    public void setParameterName(String parameterName) {
        this.parameterName = parameterName;
    }

    public Class<?> getParameterClass() {
        return parameterClass;
    }

    public void setParameterClass(Class<?> parameterClass) {
        this.parameterClass = parameterClass;
    }

    public List<Annotation> getAnnotations() {
        return annotations;
    }

    public void addAnnotation(Annotation annotation) {
        this.annotations.add(annotation);
    }

    public void addAnnotations(List<Annotation> annotations) {
        this.annotations.addAll(annotations);
    }

    public Type getParameterType() {
        return parameterType;
    }

    public void setParameterType(final Type parameterType) {
        this.parameterType = parameterType;
    }
}
