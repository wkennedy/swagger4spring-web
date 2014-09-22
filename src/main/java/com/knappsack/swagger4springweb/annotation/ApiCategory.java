package com.knappsack.swagger4springweb.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An annotation which indicates if this Api should be added to a category
 *
 * This annotation is applicable to the controller class
 */
@Target(value = {ElementType.METHOD, ElementType.TYPE})
@Retention(value = RetentionPolicy.RUNTIME)
public @interface ApiCategory {
  java.lang.String value();
  java.lang.String description() default "";
}
