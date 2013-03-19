package com.knappsack.swagger4springweb.parser;

import com.knappsack.swagger4springweb.AbstractTest;
import com.wordnik.swagger.core.DocumentationParameter;
import org.junit.Test;

import java.lang.reflect.Method;
import java.util.Date;
import java.util.List;

import static junit.framework.Assert.assertTrue;

public class DocumentationParameterParserTest extends AbstractTest {

    @Test
    public void documentationParameterTest() throws NoSuchMethodException {
        Class controllerClass = getControllerClass();
        Method method = controllerClass.getMethod("getTestPojoRequestParams", String.class, Boolean.class, Byte.class, Long.class, Integer.class, Float.class, Double.class, Date.class);
        DocumentationParameterParser parameterParser = new DocumentationParameterParser();
        List<DocumentationParameter> documentationParameters = parameterParser.getDocumentationParams(method);
        assertTrue(documentationParameters.size() == 8);
        DocumentationParameter documentationParameter = documentationParameters.get(0);
        assertTrue(documentationParameter.getDataType().equals("string"));
        assertTrue(documentationParameter.getName().equals("testVariable"));
        assertTrue(documentationParameter.getValueTypeInternal().equals(String.class.getName()));
    }

}
