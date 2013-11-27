package com.knappsack.swagger4springweb.parser;

import com.wordnik.swagger.core.ApiValues;
import com.wordnik.swagger.model.Parameter;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class DocumentationPathParserTest {

    @Test
    public void getPathParametersReturnsCorrectParametersList() {
        String resourcePath = "/owner/{ownerName}/pets/{someOther}";
        String[] methodPaths = { "/dogs/{dogId}", "/legacy/path/to/dogs/breed/{dogId}" };

        List<Parameter> parameters = new ApiPathParser()
                .getPathParameters(resourcePath, methodPaths);

        assertEquals(3, parameters.size());
        assertTrue(contains(parameters, "ownerName"));
        assertTrue(contains(parameters, "dogId"));
        assertTrue(contains(parameters, "someOther"));
    }

    @Test
    public void getPathParametersReturnsCorrectParameterFields() {
        String resourcePath = "/owner/{ownerName}/pets";

        List<Parameter> parameters = new ApiPathParser().getPathParameters(resourcePath, null);

        assertEquals(1, parameters.size());

        Parameter documentationParameter = parameters.get(0);

        assertEquals("ownerName", documentationParameter.name());
        assertEquals(true, documentationParameter.required());
        assertEquals(ApiValues.TYPE_PATH(), documentationParameter.paramType());
        assertEquals("string", documentationParameter.dataType());
//        assertEquals(String.class.getName(), documentationParameter.getValueTypeInternal());
        assertEquals(false, documentationParameter.allowMultiple());
        assertEquals("Owner name", documentationParameter.description().get());
    }

    @Test
    public void getPathParametersReturnsEmptyListWhenBadParametersArePassed() {
        List<Parameter> parameters = new ApiPathParser().getPathParameters(null, null);
        assertEquals(parameters.size(), 0);
    }

    private boolean contains(List<Parameter> parameters, String parameter) {
        for (Parameter documentationParameter : parameters) {
            if (parameter.equals(documentationParameter.name())) {
                return true;
            }
        }
        return false;
    }
}
