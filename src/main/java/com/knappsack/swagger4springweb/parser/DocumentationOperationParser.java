package com.knappsack.swagger4springweb.parser;

import com.wordnik.swagger.annotations.ApiError;
import com.wordnik.swagger.annotations.ApiErrors;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.core.DocumentationError;
import com.wordnik.swagger.core.DocumentationOperation;
import com.wordnik.swagger.core.DocumentationParameter;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.lang.reflect.Method;
import java.util.List;

public class DocumentationOperationParser {

    private String resourcePath;
    private List<String> ignorableAnnotations;
    private boolean ignoreUnusedPathVariables;

    public DocumentationOperationParser(String resourcePath, List<String> ignorableAnnotations, boolean ignoreUnusedPathVariables) {
        this.ignorableAnnotations = ignorableAnnotations;
        this.ignoreUnusedPathVariables = ignoreUnusedPathVariables;
        this.resourcePath = resourcePath;
    }

    public DocumentationOperation getDocumentationOperation(Method method) {

        DocumentationOperation documentationOperation = new DocumentationOperation();
        documentationOperation.setNickname(method.getName());// method name
        documentationOperation.setResponseTypeInternal(method.getReturnType().getName());
        documentationOperation.setResponseClass(method.getReturnType().getSimpleName());

        String httpMethod = "";
        RequestMapping methodRequestMapping = method
                .getAnnotation(RequestMapping.class);
        if (httpMethod.isEmpty()) {
            for (RequestMethod requestMethod : methodRequestMapping.method()) {
                httpMethod += requestMethod.name() + " ";
            }
        }
        // get ApiOperation information
        ApiOperation apiOperation = method.getAnnotation(ApiOperation.class);
        if (apiOperation != null) {
            if (!apiOperation.httpMethod().isEmpty()) {
                httpMethod = apiOperation.httpMethod();
            }
            if (!(apiOperation.responseClass().isEmpty() || apiOperation
                    .responseClass().equals("void"))) {
                documentationOperation.setResponseClass(apiOperation.responseClass());
            }
            documentationOperation.setSummary(apiOperation.value());
            documentationOperation.setNotes(apiOperation.notes());
        }
        documentationOperation.setHttpMethod(httpMethod.trim());// GET POST

        ApiError apiError = method.getAnnotation(ApiError.class);
        if (apiError != null) {
            addError(documentationOperation, apiError);
        }

        ApiErrors apiErrors = method.getAnnotation(ApiErrors.class);
        if (apiErrors != null) {
            ApiError[] errors = apiErrors.value();
            for (ApiError error : errors) {
                addError(documentationOperation, error);
            }
        }

        DocumentationParameterParser documentationParameterParser = new DocumentationParameterParser(ignorableAnnotations);
        List<DocumentationParameter> documentationParameters = documentationParameterParser
                .getDocumentationParams(method);
        documentationOperation.setParameters(documentationParameters);
        addUnusedPathVariables(documentationOperation, methodRequestMapping.value());

        return documentationOperation;
    }

    private void addError(DocumentationOperation documentationOperation, ApiError apiError) {
        DocumentationError documentationError = new DocumentationError();
        documentationError.setCode(apiError.code());
        documentationError.setReason(apiError.reason());
        documentationOperation.addErrorResponse(documentationError);
    }

    private void addUnusedPathVariables(DocumentationOperation documentationOperation, String[] methodPath) {
        if(ignoreUnusedPathVariables){
           return;
        }

        for(DocumentationParameter documentationParameter : new DocumentationPathParser().getPathParameters(resourcePath, methodPath)){
            if(!isParameterPresented(documentationOperation, documentationParameter.getName())){
                documentationOperation.addParameter(documentationParameter);
            }
        }
    }

    private boolean isParameterPresented(DocumentationOperation documentationOperation, String parameter){
        if(parameter == null || documentationOperation.getParameters() == null){
            return false;
        }
        for(DocumentationParameter documentationParameter : documentationOperation.getParameters()){
            if(parameter.equals(documentationParameter.getName())){
                return true;
            }
        }
        return false;
    }
}
