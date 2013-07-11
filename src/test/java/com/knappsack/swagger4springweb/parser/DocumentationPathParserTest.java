package com.knappsack.swagger4springweb.parser;

import com.wordnik.swagger.core.ApiValues;
import com.wordnik.swagger.core.DocumentationParameter;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class DocumentationPathParserTest {

    @Test
    public void getPathParametersReturnsCorrectParametersList() {
        String resourcePath = "/owner/{ownerName}/pets/{someOther}";
        String[] methodPaths = { "/dogs/{dogId}", "/legacy/path/to/dogs/breed/{dogId}" };

        List<DocumentationParameter> parameters = new DocumentationPathParser()
                .getPathParameters(resourcePath, methodPaths);

        assertEquals(3, parameters.size());
        assertTrue(contains(parameters, "ownerName"));
        assertTrue(contains(parameters, "dogId"));
        assertTrue(contains(parameters, "someOther"));
    }

    @Test
    public void getPathParametersReturnsCorrectParameterFields() {
        String resourcePath = "/owner/{ownerName}/pets";

        List<DocumentationParameter> parameters = new DocumentationPathParser().getPathParameters(resourcePath, null);

        assertEquals(1, parameters.size());

        DocumentationParameter documentationParameter = parameters.get(0);

        assertEquals("ownerName", documentationParameter.getName());
        assertEquals(true, documentationParameter.getRequired());
        assertEquals(ApiValues.TYPE_PATH, documentationParameter.getParamType());
        assertEquals("string", documentationParameter.getDataType());
        assertEquals(String.class.getName(), documentationParameter.getValueTypeInternal());
        assertEquals(false, documentationParameter.getAllowMultiple());
        assertEquals("Owner name", documentationParameter.getDescription());
    }

    @Test
    public void getPathParametersReturnsEmptyListWhenBadParametersArePassed() {
        List<DocumentationParameter> parameters = new DocumentationPathParser().getPathParameters(null, null);
        assertEquals(parameters.size(), 0);
    }

    private boolean contains(List<DocumentationParameter> parameters, String parameter) {
        for (DocumentationParameter documentationParameter : parameters) {
            if (parameter.equals(documentationParameter.getName())) {
                return true;
            }
        }
        return false;
    }
}
