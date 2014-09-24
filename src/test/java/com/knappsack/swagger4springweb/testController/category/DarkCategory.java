package com.knappsack.swagger4springweb.testController.category;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.knappsack.swagger4springweb.annotation.ApiCategory;

/**
 * @author Allar Tammik
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@ApiCategory(value = "dark", description = "This is so dark")
public @interface DarkCategory {
}
