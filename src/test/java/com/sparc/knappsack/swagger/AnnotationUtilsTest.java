package com.sparc.knappsack.swagger;

import com.sparc.swagger4springweb.model.AnnotatedParameter;
import com.sparc.swagger4springweb.util.AnnotationUtils;
import com.wordnik.swagger.annotations.ApiParam;
import org.junit.Test;
import org.springframework.web.bind.annotation.PathVariable;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.List;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

public class AnnotationUtilsTest extends AbstractTest {

    @Test
    public void testHasApiParam() {
        Class controllerClass = getControllerClass();
        try {
            boolean hasApiParam = AnnotationUtils.hasApiParam(controllerClass.getMethod("getTestPojos", HttpServletRequest.class, String.class));
            assertTrue(hasApiParam);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testGetMethodRequestMappingValue() {
        Class controllerClass = getControllerClass();
        try {
            String requestMappingValue = AnnotationUtils.getMethodRequestMappingValue(controllerClass.getMethod("getTestPojos", HttpServletRequest.class, String.class));
            assertEquals(requestMappingValue, "/");
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testGetSwaggerParameterAnnotations() throws NoSuchMethodException {
        Class controllerClass = getControllerClass();
        Method method = controllerClass.getMethod("getTestPojos", HttpServletRequest.class, String.class);
        List<AnnotatedParameter> annotatedParameters = AnnotationUtils.getAnnotatedParameters(method);
        assertTrue(annotatedParameters.size() == 1);
        for (AnnotatedParameter annotatedParameter : annotatedParameters) {
            assertTrue(annotatedParameter.getAnnotation() instanceof ApiParam);
            assertTrue(annotatedParameter.getParameterName().equals("testVariable"));
            assertTrue(annotatedParameter.getParameterType().isAssignableFrom(String.class));
        }
    }

    @Test
    public void testGetSpringMvcParaterAnnotations() throws NoSuchMethodException {
        Class controllerClass = getControllerClass();
        Method method = controllerClass.getMethod("getTestPojosNoSwaggerAnnotations", HttpServletRequest.class, String.class);
        List<AnnotatedParameter> annotatedParameters = AnnotationUtils.getAnnotatedParameters(method);
        assertTrue(annotatedParameters.size() == 1);
        for (AnnotatedParameter annotatedParameter : annotatedParameters) {
            assertTrue(annotatedParameter.getAnnotation() instanceof PathVariable);
            assertTrue(annotatedParameter.getParameterName().equals("testVariable"));
            assertTrue(annotatedParameter.getParameterType().isAssignableFrom(String.class));
        }
    }

}
