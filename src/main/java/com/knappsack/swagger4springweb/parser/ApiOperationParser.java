package com.knappsack.swagger4springweb.parser;

import com.knappsack.swagger4springweb.util.JavaToScalaUtil;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;
import com.wordnik.swagger.converter.ModelConverters;
import com.wordnik.swagger.model.Model;
import com.wordnik.swagger.model.Operation;
import com.wordnik.swagger.model.Parameter;
import com.wordnik.swagger.model.ResponseMessage;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import scala.Option;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static javax.ws.rs.core.MediaType.APPLICATION_FORM_URLENCODED;

public class ApiOperationParser {

    private final Map<String, Model> models;
    private String resourcePath;
    private List<String> ignorableAnnotations;
    private boolean ignoreUnusedPathVariables;

    public ApiOperationParser(String resourcePath, List<String> ignorableAnnotations,
            boolean ignoreUnusedPathVariables, Map<String, Model> models) {
        this.ignorableAnnotations = ignorableAnnotations;
        this.ignoreUnusedPathVariables = ignoreUnusedPathVariables;
        this.resourcePath = resourcePath;
        this.models = models;
    }

    public Operation parseDocumentationOperation(Method method) {

        DocumentationOperation documentationOperation = new DocumentationOperation();
        documentationOperation.setNickname(method.getName());// method name

        Type returnType = method.getGenericReturnType();
        if (returnType instanceof ParameterizedType) {
            final ParameterizedType parameterizedType = (ParameterizedType) returnType;

            if (parameterizedType.getActualTypeArguments().length == 1) {
                final Type type = parameterizedType.getActualTypeArguments()[0];
                if (type instanceof ParameterizedType) {
                    documentationOperation.setResponseClass((Class<?>) ((ParameterizedType) type).getRawType());
                } else {
                    documentationOperation.setResponseClass((Class<?>) type);
                }
                documentationOperation
                        .setResponseContainer(((Class<?>) parameterizedType.getRawType()));
            } else {
                // TODO what to do here?
                // not supporting generic with several values
            }
        }

        if (StringUtils.isEmpty(documentationOperation.getResponseClass())) {
            Class<?> clazz = method.getReturnType();
            if (clazz.isArray()) {
                documentationOperation.setResponseClass(clazz.getComponentType());
            } else {
                documentationOperation.setResponseClass(clazz);
            }
        }

        String httpMethod = "";
        RequestMapping methodRequestMapping = method.getAnnotation(RequestMapping.class);
        for (RequestMethod requestMethod : methodRequestMapping.method()) {
            httpMethod += requestMethod.name() + " ";
        }
        documentationOperation.setHttpMethod(httpMethod.trim());
        documentationOperation.addConsumes(asList(methodRequestMapping.consumes()));
        documentationOperation.addProduces(asList(methodRequestMapping.produces()));

        // get ApiOperation information
        ApiOperation apiOperation = method.getAnnotation(ApiOperation.class);
        if (apiOperation != null) {
            documentationOperation.setHttpMethod(apiOperation.httpMethod());
            documentationOperation.setResponseClass(apiOperation.response());
            documentationOperation.setResponseContainer(apiOperation.responseContainer());
            documentationOperation.addProduces(apiOperation.produces());
            documentationOperation.addConsumes(apiOperation.consumes());
            documentationOperation.setSummary(apiOperation.value());
            documentationOperation.setNotes(apiOperation.notes());
            documentationOperation.setPosition(apiOperation.position());
            documentationOperation.addProtocols(apiOperation.protocols());
            documentationOperation.addAuthorizations(apiOperation.authorizations());
        }

        ApiResponse apiResponse = method.getAnnotation(ApiResponse.class);
        if (apiResponse != null) {
            addResponse(documentationOperation, apiResponse);
        }

        ApiResponses apiResponses = method.getAnnotation(ApiResponses.class);
        if (apiResponses != null) {
            ApiResponse[] responses = apiResponses.value();
            for (ApiResponse response : responses) {
                addResponse(documentationOperation, response);
            }
        }

        ApiParameterParser apiParameterParser = new ApiParameterParser(ignorableAnnotations, models);
        List<Parameter> documentationParameters = apiParameterParser.parseApiParametersAndArgumentModels(method);
        documentationOperation.setParameters(documentationParameters);
        addUnusedPathVariables(documentationOperation, methodRequestMapping.value());

        return documentationOperation.toScalaOperation();
    }

    private void addResponse(DocumentationOperation documentationOperation, ApiResponse apiResponse) {
        Option<String> responseOption = Option.apply(apiResponse.response().getName());
        ResponseMessage responseMessage = new ResponseMessage(apiResponse.code(), apiResponse.message(),
                responseOption);
        documentationOperation.addResponseMessage(responseMessage);
    }

    private void addUnusedPathVariables(DocumentationOperation documentationOperation, String[] methodPath) {
        if (ignoreUnusedPathVariables) {
            return;
        }

        for (Parameter documentationParameter : new ApiPathParser().getPathParameters(resourcePath, methodPath)) {
            if (!isParameterPresented(documentationOperation, documentationParameter.name())) {
                documentationOperation.addParameter(documentationParameter);
            }
        }
    }

    private boolean isParameterPresented(DocumentationOperation documentationOperation, String parameter) {
        if (documentationOperation.getParameters().isEmpty()) {
            return false;
        }
        for (Parameter documentationParameter : documentationOperation.getParameters()) {
            if (parameter.equals(documentationParameter.name()) &&
                    "path".equals(documentationParameter.paramType())) {
                return true;
            }
        }
        return false;
    }

    //This class is used as a temporary solution to create a Swagger Operation object, since the Operation is immutable
    class DocumentationOperation {

        private String nickname;
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

        Operation toScalaOperation() {
            final List<String> consumes = getConsumes();

            return new Operation(httpMethod,
                    summary,
                    notes,
                    responseClass,
                    nickname,
                    position,
                    JavaToScalaUtil.toScalaList(produces),
                    JavaToScalaUtil.toScalaList(consumes),
                    JavaToScalaUtil.toScalaList(protocols),
                    JavaToScalaUtil.toScalaList(authorizations),
                    JavaToScalaUtil.toScalaList(parameters),
                    JavaToScalaUtil.toScalaList(responseMessages),
                    null);
        }

        void setNickname(String nickname) {
            this.nickname = nickname;
        }

        void setResponseClass(Class<?> responseClass) {
            if (responseClass == null || responseClass == Void.class) {
                return;
            }

            Option<Model> model = ModelConverters.read(responseClass);
            if (model.nonEmpty()) {
                this.responseClass = model.get().name();
            } else {
                this.responseClass = responseClass.getSimpleName();
            }
        }

        void setSummary(String summary) {
            this.summary = summary;
        }

        void setNotes(String notes) {
            this.notes = notes;
        }

        void setHttpMethod(String httpMethod) {
            if (StringUtils.isEmpty(httpMethod)) {
                return;
            }
            this.httpMethod = httpMethod;
        }

        void setParameters(List<Parameter> parameters) {
            this.parameters = parameters;
        }

        void setPosition(int position) {
            this.position = position;
        }

        void addConsumes(final List<String> consumes) {
            this.consumes.addAll(consumes);
        }

        void addProduces(final List<String> produces) {
            this.produces.addAll(produces);
        }

        public void addResponseMessage(final ResponseMessage responseMessage) {
            this.responseMessages.add(responseMessage);
        }

        public List<Parameter> getParameters() {
            return parameters;
        }

        public void addParameter(final Parameter parameter) {
            this.parameters.add(parameter);
        }

        public void addAuthorizations(final String authorizations) {
            if (StringUtils.isEmpty(authorizations)) {
                return;
            }
            this.authorizations.add(authorizations);
        }

        void addProtocols(final String protocols) {
            if (StringUtils.isEmpty(protocols)) {
                return;
            }
            this.protocols.add(protocols);
        }

        public void addProduces(final String produces) {
            if (StringUtils.isEmpty(produces)) {
                return;
            }
            this.produces.add(produces);
        }

        public void addConsumes(final String consumes) {
            if (StringUtils.isEmpty(consumes)) {
                return;
            }
            this.consumes.add(consumes);
        }

        public void setResponseContainer(final String container) {
            if (StringUtils.isEmpty(container)) {
                return;
            }
            this.responseClass = format("%s[%s]", container, responseClass);
        }

        public void setResponseContainer(final Class<?> type) {
            Option<Model> model = ModelConverters.read(type);
            if (model.nonEmpty()) {
                setResponseContainer(model.get().name());
            } else {
                setResponseContainer(type.getSimpleName());
            }
        }

        public String getResponseClass() {
            return responseClass;
        }

        List<String> getConsumes() {
            return !consumes.isEmpty() || "GET".equalsIgnoreCase(httpMethod)
                    ? consumes : asList(APPLICATION_FORM_URLENCODED);
        }
    }
}
