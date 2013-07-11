package com.knappsack.swagger4springweb.parser;

import com.knappsack.swagger4springweb.util.DocumentationUtils;
import com.wordnik.swagger.core.ApiValues;
import com.wordnik.swagger.core.DocumentationParameter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DocumentationPathParser {

    private static final Pattern PATTERN = Pattern.compile("\\{([a-zA-Z]+)\\}");
    private static final Class<?> TYPE = String.class;

    public List<DocumentationParameter> getPathParameters(String resourcePath, String[] methodPaths) {
        Map<String, DocumentationParameter> parameters = new HashMap<String, DocumentationParameter>();

        addParameters(parameters, resourcePath);

        if (methodPaths != null) {
            for (String methodPath : methodPaths) {
                addParameters(parameters, methodPath);
            }
        }

        return new ArrayList<DocumentationParameter>(parameters.values());
    }

    private void addParameters(Map<String, DocumentationParameter> parameters, String path) {
        for (String parameter : getPathParameters(path)) {
            parameters.put(parameter, createParameter(parameter));
        }
    }

    private DocumentationParameter createParameter(String parameter) {
        DocumentationParameter documentationParameter = new DocumentationParameter();
        documentationParameter.setName(parameter);
        documentationParameter.setRequired(true);
        documentationParameter.setDataType(DocumentationUtils.getSwaggerTypeFor(TYPE));
        documentationParameter.setValueTypeInternal(TYPE.getName());
        documentationParameter.setAllowMultiple(DocumentationUtils.isAllowMultiple(TYPE));
        documentationParameter.setParamType(ApiValues.TYPE_PATH);
        documentationParameter.setDescription(getDescription(parameter));

        return documentationParameter;
    }

    private String getDescription(String parameter) {
        StringBuffer buffer = new StringBuffer();

        //Parameters with name length less then 3 will have description equal to their name
        if (parameter == null || parameter.length() < 3) {
            return parameter;
        }

        String tmp = parameter;

        for (int i = 0; i < tmp.length(); i++) {
            if (Character.isUpperCase(tmp.charAt(i))) {
                if (buffer.length() == 0) {
                    // First letter is capital
                    buffer.append(tmp.substring(0, 1).toUpperCase()).append(tmp.substring(1, i));
                } else {
                    buffer.append(tmp.substring(0, i).toLowerCase());
                }
                buffer.append(" ");
                tmp = tmp.substring(i);
                i = 0;
            }
        }

        return buffer.append(tmp.toLowerCase()).toString();
    }

    private List<String> getPathParameters(String path) {
        List<String> parameters = new ArrayList<String>();

        if (path == null || path.trim().length() == 0) {
            return Collections.emptyList();
        }

        Matcher matcher = PATTERN.matcher(path);

        while (matcher.find()) {
            parameters.add(matcher.group(1));
        }

        return parameters;
    }
}
