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

    public List<DocumentationParameter> getPathParameters(String resourcePath, String[] methodPaths){
        Map<String, DocumentationParameter> parameters = new HashMap<String, DocumentationParameter>();

        addParameters(parameters, resourcePath);

        if(methodPaths != null){
            for(String methodPath : methodPaths){
                addParameters(parameters, methodPath);
            }
        }

        return new ArrayList<DocumentationParameter>(parameters.values());
    }

    private void addParameters(Map<String, DocumentationParameter> parameters, String path){
        for(String parameter : getPathParameters(path)){
            parameters.put(parameter, createParameter(parameter));
        }
    }

    private DocumentationParameter createParameter(String parameter){
        DocumentationParameter documentationParameter = new DocumentationParameter();
        documentationParameter.setName(parameter);
        documentationParameter.setRequired(true);
        documentationParameter.setDataType(DocumentationUtils.getSwaggerTypeFor(TYPE));
        documentationParameter.setValueTypeInternal(TYPE.getName());
        documentationParameter.setAllowMultiple(DocumentationUtils.isAllowMultiple(TYPE));
        documentationParameter.setParamType(ApiValues.TYPE_PATH);

        return documentationParameter;
    }

    private List<String> getPathParameters(String path){
        List<String> parameters = new ArrayList<String>();

        if(path == null || path.trim().length() == 0){
            return Collections.emptyList();
        }

        Matcher matcher = PATTERN.matcher(path);

        if(matcher.find()){
            for(int i = 0; i < matcher.groupCount(); i++){
                parameters.add(matcher.group(i + 1));
            }
        }

        return parameters;
    }
}
