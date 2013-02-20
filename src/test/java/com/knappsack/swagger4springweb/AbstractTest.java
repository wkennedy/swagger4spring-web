package com.knappsack.swagger4springweb;

import org.reflections.Reflections;
import org.springframework.stereotype.Controller;

import java.util.Set;

public abstract class AbstractTest {

    public static final String BASE_CONTROLLER_PACKAGE = "com.knappsack.swagger4springweb.testController";
    public static final String BASE_MODEL_PACKAGE = "com.knappsack.swagger4springweb.testModels";

    public Class getControllerClass() {
        Reflections reflections = new Reflections(BASE_CONTROLLER_PACKAGE);
        Set<Class<?>> controllerClasses = reflections.getTypesAnnotatedWith(Controller.class);

        return controllerClasses.iterator().next();
    }
}
