package com.knappsack.swagger4springweb;

import com.knappsack.swagger4springweb.model.AnnotatedParameter;
import com.knappsack.swagger4springweb.testController.TestController;
import com.knappsack.swagger4springweb.util.AnnotationUtils;
import com.wordnik.swagger.annotations.ApiParam;
import org.junit.Test;
import org.springframework.web.bind.annotation.PathVariable;

import javax.servlet.http.HttpServletRequest;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.List;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

public class AnnotationUtilsTest extends AbstractTest {

    @Test
    public void testHasApiParam() {
        Class controllerClass = TestController.class;
        try {
            boolean hasApiParam = AnnotationUtils.hasApiParam(controllerClass.getMethod("getTestPojos", HttpServletRequest.class, String.class));
            assertTrue(hasApiParam);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testGetMethodRequestMappingValue() {
        Class controllerClass = TestController.class;
        try {
            String requestMappingValue = AnnotationUtils.getMethodRequestMappingValue(controllerClass.getMethod("getTestPojos", HttpServletRequest.class, String.class));
            assertEquals(requestMappingValue, "");
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testGetSwaggerParameterAnnotations() throws NoSuchMethodException {
        Class controllerClass = TestController.class;
        Method method = controllerClass.getMethod("getTestPojos", HttpServletRequest.class, String.class);
        List<AnnotatedParameter> annotatedParameters = AnnotationUtils.getAnnotatedParameters(method);
        assertTrue(annotatedParameters.size() == 1);
        for (AnnotatedParameter annotatedParameter : annotatedParameters) {
            assertTrue(listContainsType(annotatedParameter.getAnnotations(), ApiParam.class));
            assertTrue(annotatedParameter.getParameterName().equals("testVariable"));
            assertTrue(annotatedParameter.getParameterType().isAssignableFrom(String.class));
        }
    }

    @Test
    public void testGetSpringMvcParaterAnnotations() throws NoSuchMethodException {
        Class controllerClass = TestController.class;
        Method method = controllerClass.getMethod("getTestPojosNoSwaggerAnnotations", HttpServletRequest.class, String.class);
        List<AnnotatedParameter> annotatedParameters = AnnotationUtils.getAnnotatedParameters(method);
        assertTrue(annotatedParameters.size() == 1);
        for (AnnotatedParameter annotatedParameter : annotatedParameters) {
            assertTrue(listContainsType(annotatedParameter.getAnnotations(),  PathVariable.class));
            assertTrue(annotatedParameter.getParameterName().equals("testVariable"));
            assertTrue(annotatedParameter.getParameterType().isAssignableFrom(String.class));
        }
    }

    private boolean listContainsType(List<Annotation> annotations,
			Class<? extends Annotation> clazz) {
    	for (Annotation annotation : annotations) {
    		if (clazz.isAssignableFrom(annotation.getClass())) {
    			return true;
    		}
    	}
    	return false;
	}

	@Test
    public void testMethodHasNoApiParamAnnotation() throws NoSuchMethodException {
        Class controllerClass = TestController.class;
        Method method = controllerClass.getMethod("getTestPojosNoSwaggerAnnotations", HttpServletRequest.class, String.class);
        boolean hasApiParam = AnnotationUtils.hasApiParam(method);
        assertFalse(hasApiParam);
    }

}
