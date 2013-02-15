package com.sparc.swagger4springweb.parser;

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

    public DocumentationOperation getDocumentationOperation(Method method) {
        String httpMethod = "";
        String nickname = "";
        String responseClass = "";
        //get ApiOperation information
        ApiOperation apiOperation = method.getAnnotation(ApiOperation.class);
        if (apiOperation != null) {
            //apiOperationValue = apiOperation.value();
            httpMethod = apiOperation.httpMethod();
            //nickname = apiOperation.value();
            responseClass = apiOperation.responseClass();
        }

        RequestMapping methodRequestMapping = method.getAnnotation(RequestMapping.class);
        if (httpMethod.isEmpty()) {
            for (RequestMethod requestMethod : methodRequestMapping.method()) {
                httpMethod += requestMethod.name() + " ";
            }
        }
        if (nickname.isEmpty()) {
            nickname = method.getName();
        }
        if (responseClass.isEmpty()) {
            responseClass = method.getReturnType().getSimpleName();
        }

        DocumentationOperation documentationOperation = new DocumentationOperation();
        documentationOperation.setHttpMethod(httpMethod);//GET POST
        documentationOperation.setNickname(nickname);//method name
        documentationOperation.setResponseClass(responseClass);//return type ex: CategoryModel

        ApiError apiError = method.getAnnotation(ApiError.class);
        if(apiError != null) {
            DocumentationError documentationError = new DocumentationError();
            documentationError.setCode(apiError.code());
            documentationError.setReason(apiError.reason());
            documentationOperation.addErrorResponse(documentationError);
        }

        ApiErrors apiErrors = method.getAnnotation(ApiErrors.class);
        if(apiErrors != null) {
            ApiError[] errors = apiErrors.value();
            for (ApiError error : errors) {
                DocumentationError documentationError = new DocumentationError();
                documentationError.setCode(error.code());
                documentationError.setReason(error.reason());
                documentationOperation.addErrorResponse(documentationError);
            }
        }

        DocumentationParameterParser documentationParameterParser = new DocumentationParameterParser();
        List<DocumentationParameter> documentationParameters = documentationParameterParser.getDocumentationParams(method);
        documentationOperation.setParameters(documentationParameters);

        return documentationOperation;
    }
}
