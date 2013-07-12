package com.knappsack.swagger4springweb.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An annotation which indicates if this Api should be excluded from the
 * automatically generated swagger documents
 *
 * This annotation is applicable to the controller class or a method inside the controller
 */
@Target(value = {ElementType.METHOD, ElementType.TYPE})
@Retention(value = RetentionPolicy.RUNTIME)
public @interface ApiExclude {
}
