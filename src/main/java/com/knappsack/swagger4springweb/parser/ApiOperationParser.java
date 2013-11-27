package com.knappsack.swagger4springweb.parser;

import com.knappsack.swagger4springweb.util.JavaToScalaUtil;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;
import com.wordnik.swagger.model.Operation;
import com.wordnik.swagger.model.Parameter;
import com.wordnik.swagger.model.ResponseMessage;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import scala.Option;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ApiOperationParser {

    private String resourcePath;
    private List<String> ignorableAnnotations;
    private boolean ignoreUnusedPathVariables;

    public ApiOperationParser(String resourcePath, List<String> ignorableAnnotations, boolean ignoreUnusedPathVariables) {
        this.ignorableAnnotations = ignorableAnnotations;
        this.ignoreUnusedPathVariables = ignoreUnusedPathVariables;
        this.resourcePath = resourcePath;
    }

    public Operation getDocumentationOperation(Method method) {

        DocumentationOperation documentationOperation = new DocumentationOperation();
        documentationOperation.setName(method.getName());
        documentationOperation.setNickname(method.getName());// method name
        documentationOperation.setResponseTypeInternal(method.getReturnType().getName());
        String responseClass;
        Class<?> returnType = method.getReturnType();
        if(returnType.isArray()) {
            responseClass = returnType.getComponentType().getSimpleName();
        } else {
            responseClass = method.getReturnType().getSimpleName();
        }
        documentationOperation.setResponseClass(responseClass);

        String httpMethod = "";
        RequestMapping methodRequestMapping = method
                .getAnnotation(RequestMapping.class);
        if (httpMethod.isEmpty()) {
            for (RequestMethod requestMethod : methodRequestMapping.method()) {
                httpMethod += requestMethod.name() + " ";
            }
        }
        documentationOperation.getConsumes().addAll(Arrays.asList(methodRequestMapping.consumes()));
        documentationOperation.setHttpMethod(httpMethod.trim());
        documentationOperation.getProduces().addAll(Arrays.asList(methodRequestMapping.produces()));

        // get ApiOperation information
        ApiOperation apiOperation = method.getAnnotation(ApiOperation.class);
        if (apiOperation != null) {
            if (!apiOperation.httpMethod().isEmpty()) {
                httpMethod = apiOperation.httpMethod();
            }
            if (!(apiOperation.response() == null)) {
                documentationOperation.setResponseClass(apiOperation.response().getName());
            }
            documentationOperation.setSummary(apiOperation.value());
            documentationOperation.setNotes(apiOperation.notes());
            documentationOperation.setPosition(apiOperation.position());
            documentationOperation.getProduces().add(apiOperation.produces());
            documentationOperation.getConsumes().add(apiOperation.consumes());
            documentationOperation.getProtocols().add(apiOperation.protocols());
            documentationOperation.getAuthorizations().add(apiOperation.authorizations());
            documentationOperation.setHttpMethod(httpMethod);
        }

        ApiResponse apiError = method.getAnnotation(ApiResponse.class);
        if (apiError != null) {
            addError(documentationOperation, apiError);
        }

        ApiResponses apiErrors = method.getAnnotation(ApiResponses.class);
        if (apiErrors != null) {
            ApiResponse[] errors = apiErrors.value();
            for (ApiResponse error : errors) {
                addError(documentationOperation, error);
            }
        }

        ApiParameterParser apiParameterParser = new ApiParameterParser(ignorableAnnotations);
        List<Parameter> documentationParameters = apiParameterParser
                .getApiParameters(method);
        documentationOperation.setParameters(documentationParameters);
        addUnusedPathVariables(documentationOperation, methodRequestMapping.value());


        return new Operation(documentationOperation.getHttpMethod(),
                documentationOperation.getSummary(),
                documentationOperation.getNotes(),
                documentationOperation.getResponseClass(),
                documentationOperation.getNickname(),
                documentationOperation.getPosition(),
                JavaToScalaUtil.toScalaList(documentationOperation.getProduces()),
                JavaToScalaUtil.toScalaList(documentationOperation.getConsumes()),
                JavaToScalaUtil.toScalaList(documentationOperation.getProtocols()),
                JavaToScalaUtil.toScalaList(documentationOperation.getAuthorizations()),
                JavaToScalaUtil.toScalaList(documentationOperation.getParameters()) ,
                JavaToScalaUtil.toScalaList(documentationOperation.getResponseMessages()),
                null);
    }

    private void addError(DocumentationOperation documentationOperation, ApiResponse apiError) {
        Option<String> responseOption = Option.apply(apiError.response().getName());
        ResponseMessage responseMessage = new ResponseMessage(apiError.code(), apiError.message(),responseOption);
        documentationOperation.getResponseMessages().add(responseMessage);
    }

    private void addUnusedPathVariables(DocumentationOperation documentationOperation, String[] methodPath) {
        if(ignoreUnusedPathVariables){
           return;
        }

        for(Parameter documentationParameter : new ApiPathParser().getPathParameters(resourcePath, methodPath)){
            if(!isParameterPresented(documentationOperation, documentationParameter.name())){
                documentationOperation.getParameters().add(documentationParameter);
            }
        }
    }

    private boolean isParameterPresented(DocumentationOperation documentationOperation, String parameter){
        if(parameter == null || documentationOperation.getParameters() == null){
            return false;
        }
        for(Parameter documentationParameter : documentationOperation.getParameters()){
            if(parameter.equals(documentationParameter.name())){
                return true;
            }
        }
        return false;
    }

    //This class is used as a temporary solution to create a Swagger Operation object, since the Operation is immutable
    class DocumentationOperation {
        private String name;
        private String nickname;
        private String responseTypeInternal;
        private String responseClass;
        private String summary;
        private String notes;
        private String httpMethod;
        private List<Parameter> parameters = new ArrayList<Parameter>();
        private List<ResponseMessage> responseMessages = new ArrayList<ResponseMessage>();
        private int position;
        private List<String> produces = new ArrayList<String>();
        private List<String> consumes = new ArrayList<String>();
        private List<String> protocols = new ArrayList<String>();
        private List<String> authorizations = new ArrayList<String>();

        String getName() {
            return name;
        }

        void setName(String name) {
            this.name = name;
        }

        String getNickname() {
            return nickname;
        }

        void setNickname(String nickname) {
            this.nickname = nickname;
        }

        String getResponseTypeInternal() {
            return responseTypeInternal;
        }

        void setResponseTypeInternal(String responseTypeInternal) {
            this.responseTypeInternal = responseTypeInternal;
        }

        String getResponseClass() {
            return responseClass;
        }

        void setResponseClass(String responseClass) {
            this.responseClass = responseClass;
        }

        String getSummary() {
            return summary;
        }

        void setSummary(String summary) {
            this.summary = summary;
        }

        String getNotes() {
            return notes;
        }

        void setNotes(String notes) {
            this.notes = notes;
        }

        String getHttpMethod() {
            return httpMethod;
        }

        void setHttpMethod(String httpMethod) {
            this.httpMethod = httpMethod;
        }

        List<Parameter> getParameters() {
            return parameters;
        }

        void setParameters(List<Parameter> parameters) {
            this.parameters = parameters;
        }

        List<ResponseMessage> getResponseMessages() {
            return responseMessages;
        }

        void setResponseMessages(List<ResponseMessage> responseMessages) {
            this.responseMessages = responseMessages;
        }

        int getPosition() {
            return position;
        }

        void setPosition(int position) {
            this.position = position;
        }

        List<String> getProduces() {
            return produces;
        }

        void setProduces(List<String> produces) {
            this.produces = produces;
        }

        List<String> getConsumes() {
            return consumes;
        }

        void setConsumes(List<String> consumes) {
            this.consumes = consumes;
        }

        List<String> getProtocols() {
            return protocols;
        }

        void setProtocols(List<String> protocols) {
            this.protocols = protocols;
        }

        List<String> getAuthorizations() {
            return authorizations;
        }

        void setAuthorizations(List<String> authorizations) {
            this.authorizations = authorizations;
        }
    }
}
