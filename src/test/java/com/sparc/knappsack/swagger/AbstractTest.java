package com.sparc.knappsack.swagger;

import org.reflections.Reflections;
import org.springframework.stereotype.Controller;

import java.util.Set;

public abstract class AbstractTest {

    public Class getControllerClass() {
        String basePackage = "com.sparc.knappsack.swagger.testController";
        Reflections reflections = new Reflections(basePackage);
        Set<Class<?>> controllerClasses = reflections.getTypesAnnotatedWith(Controller.class);

        return controllerClasses.iterator().next();
    }
}
