package com.knappsack.swagger4springweb.parser;

import com.knappsack.swagger4springweb.util.ModelUtils;
import com.wordnik.swagger.core.ApiValues;
import com.wordnik.swagger.model.Parameter;
import scala.Option;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ApiPathParser {

    private static final Pattern PATTERN = Pattern.compile("\\{([a-zA-Z]+)\\}");
    private static final Class<?> TYPE = String.class;

    public List<Parameter> getPathParameters(String resourcePath, String[] methodPaths) {
        Map<String, Parameter> parameters = new HashMap<String, Parameter>();

        addParameters(parameters, resourcePath);

        if (methodPaths != null) {
            for (String methodPath : methodPaths) {
                addParameters(parameters, methodPath);
            }
        }

        return new ArrayList<Parameter>(parameters.values());
    }

    private void addParameters(Map<String, Parameter> parameters, String path) {
        for (String parameter : getPathParameters(path)) {
            parameters.put(parameter, createParameter(parameter));
        }
    }

    private Parameter createParameter(String parameter) {
        Option<String> descriptionOption = Option.apply(getDescription(parameter));

        return new Parameter(parameter, descriptionOption, null, true, ModelUtils.isAllowMultiple(TYPE),
                ModelUtils.getSwaggerTypeFor(TYPE), null, ApiValues.TYPE_PATH(), null);
    }

    private String getDescription(String parameter) {
        StringBuilder builder = new StringBuilder();

        //Parameters with name length less then 3 will have description equal to their name
        if (parameter == null || parameter.length() < 3) {
            return parameter;
        }

        String tmp = parameter;

        for (int i = 0; i < tmp.length(); i++) {
            if (Character.isUpperCase(tmp.charAt(i))) {
                if (builder.length() == 0) {
                    // First letter is capital
                    builder.append(tmp.substring(0, 1).toUpperCase()).append(tmp.substring(1, i));
                } else {
                    builder.append(tmp.substring(0, i).toLowerCase());
                }
                builder.append(" ");
                tmp = tmp.substring(i);
                i = 0;
            }
        }

        return builder.append(tmp.toLowerCase()).toString();
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
