/**
 * Copyright (C) 2013 AlertMe.com Ltd
 */

package com.knappsack.swagger4springweb.filter;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;

public abstract class AnnotationFilter<T extends Annotation> implements Filter {

    protected final Class<T> annotation;

    protected AnnotationFilter(final Class<T> annotation) {
        this.annotation = annotation;
    }

    @Override
    public final boolean isApplicable(final Method method) {
        return isApplicable((AnnotatedElement) method) || isApplicable(method.getDeclaringClass());
    }

    @Override
    public final boolean ignore(final Method method) {
        final T annotation = method.getAnnotation(this.annotation);
        return ignore(annotation != null ? annotation : method.getDeclaringClass().getAnnotation(this.annotation));
    }

    public abstract boolean ignore(final T annotation);

    protected boolean isApplicable(final AnnotatedElement element) {
        return element.isAnnotationPresent(annotation);
    }
}
