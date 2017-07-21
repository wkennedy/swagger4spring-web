package com.knappsack.swagger4springweb.parser;

import com.knappsack.swagger4springweb.model.AnnotatedParameter;
import com.knappsack.swagger4springweb.util.AnnotationUtils;
import com.knappsack.swagger4springweb.util.ModelUtils;
import com.knappsack.swagger4springweb.util.JavaToScalaUtil;
import com.wordnik.swagger.annotations.ApiModelProperty;
import com.wordnik.swagger.annotations.ApiParam;
import com.wordnik.swagger.core.ApiValues;
import com.wordnik.swagger.model.AllowableListValues;
import com.wordnik.swagger.model.AllowableValues;
import com.wordnik.swagger.model.Model;
import com.wordnik.swagger.model.Parameter;
import org.springframework.web.bind.annotation.*;
import scala.Option;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class ApiParameterParser {

    private Map<String, Model> models;
    private List<String> ignorableAnnotations;

    public ApiParameterParser(List<String> ignorableAnnotations, final Map<String, Model> models) {
        this.ignorableAnnotations = ignorableAnnotations;
        this.models = models;
    }

    public List<Parameter> parseApiParametersAndArgumentModels(Method method) {
        List<Parameter> documentationParameters = new ArrayList<Parameter>();

        List<AnnotatedParameter> annotatedParameters = AnnotationUtils
                .getAnnotatedParameters(method);

        for (AnnotatedParameter annotatedParameter : annotatedParameters) {
            if (hasIgnorableAnnotations(annotatedParameter.getAnnotations())) {
                continue;
            }

            // Adding processed model
            ModelUtils.addModels(annotatedParameter.getParameterType(), models);

            DocumentationParameter documentationParameter = new DocumentationParameter();

            // default values from the Method
            String dataType = ModelUtils.getSwaggerTypeFor(annotatedParameter.getParameterClass());
            documentationParameter.setDataType(ModelUtils.getSwaggerTypeFor(annotatedParameter.getParameterClass()));
            documentationParameter.setName(annotatedParameter.getParameterName());
            boolean allowMultiple = ModelUtils.isAllowMultiple(annotatedParameter.getParameterClass());
            documentationParameter.setAllowMultiple(allowMultiple);
            // apply default values from spring annotations first
            for (Annotation annotation : annotatedParameter.getAnnotations()) {
                addSpringParams(annotation, documentationParameter);
            }
            // apply swagger annotations
            for (Annotation annotation : annotatedParameter.getAnnotations()) {
                if (annotation instanceof ApiParam) {
                    addApiParams((ApiParam) annotation, documentationParameter);
                }

                if (annotation instanceof ApiModelProperty) {
                    addApiModelProperty((ApiModelProperty) annotation,documentationParameter);
                }
            }

            Option<String> descriptionOption = Option.apply(documentationParameter.getDescription());
            Option<String> defaultValueOption = Option.apply(documentationParameter.getDefaultValue());
            Option<String> paramAccessOption = Option.apply(documentationParameter.getParamAccess());

            Parameter parameter =
                    new Parameter(documentationParameter.getName(), descriptionOption, defaultValueOption,
                            documentationParameter.isRequired(), documentationParameter.isAllowMultiple(), dataType,
                            documentationParameter.getAllowableValues(),
                            documentationParameter.getParamType(), paramAccessOption);

            documentationParameters.add(parameter);
        }

        documentationParameters.addAll(AnnotationUtils.getImplicitParameters(method));

        return documentationParameters;
    }

    private void addSpringParams(Annotation annotation, DocumentationParameter documentationParameter) {
        if (annotation instanceof RequestParam) {
            addRequestParams((RequestParam) annotation, documentationParameter);
        }
        if (annotation instanceof RequestHeader) {
            addRequestHeader((RequestHeader) annotation, documentationParameter);
        }
        if (annotation instanceof RequestBody) {
            addRequestBody(documentationParameter);
        }
        if (annotation instanceof PathVariable) {
            addPathVariable((PathVariable) annotation, documentationParameter);
        }
        if (annotation instanceof ModelAttribute) {
            addModelAttribute((ModelAttribute) annotation, documentationParameter);
        }
    }

    private void addModelAttribute(final ModelAttribute modelAttribute,
            final DocumentationParameter documentationParameter) {
        if (ModelUtils.isSet(modelAttribute.value())) {
            documentationParameter.setName(modelAttribute.value());
        }
        documentationParameter.setParamType(ApiValues.TYPE_BODY());
    }

    private void addRequestBody(DocumentationParameter documentationParameter) {
        documentationParameter.setRequired(true);
        documentationParameter.setParamType(ApiValues.TYPE_BODY());
    }

    private void addPathVariable(PathVariable pathVariable, DocumentationParameter documentationParameter) {
        if (ModelUtils.isSet(pathVariable.value())) {
            documentationParameter.setName(pathVariable.value());
        }

        documentationParameter.setRequired(true);
        documentationParameter.setParamType(ApiValues.TYPE_PATH());
    }

    private void addRequestParams(RequestParam requestParam,
            DocumentationParameter documentationParameter) {
        if (ModelUtils.isSet(requestParam.value())) {
            documentationParameter.setName(requestParam.value());
        }
        if (ModelUtils.isSet(requestParam.defaultValue())) {
            documentationParameter.setDefaultValue(requestParam.defaultValue());
        }
        documentationParameter.setRequired(requestParam.required());
        if ("file".equals(documentationParameter.getDataType())) {
            documentationParameter.setParamType(ApiValues.TYPE_FORM());
        } else {
            documentationParameter.setParamType(ApiValues.TYPE_QUERY());
        }
    }

    private void addRequestHeader(RequestHeader requestHeader,
            DocumentationParameter documentationParameter) {
        if (ModelUtils.isSet(requestHeader.value())) {
            documentationParameter.setName(requestHeader.value());
        }
        if (ModelUtils.isSet(requestHeader.defaultValue())) {
            documentationParameter.setDefaultValue(requestHeader.defaultValue());
        }
        documentationParameter.setRequired(requestHeader.required());
        documentationParameter.setParamType(ApiValues.TYPE_HEADER());
    }

    private void addApiParams(ApiParam apiParam, DocumentationParameter documentationParameter) {
        if (ModelUtils.isSet(apiParam.allowableValues())) {
            // we use only one simple string
            List<String> allowableValues = Arrays.asList(apiParam.allowableValues().split("\\s*,\\s*"));
            documentationParameter
                    .setAllowableValues(new AllowableListValues(JavaToScalaUtil.toScalaList(allowableValues), "LIST"));
        }
        documentationParameter.setAllowMultiple(apiParam.allowMultiple());

        if (ModelUtils.isSet(apiParam.defaultValue())) {
            documentationParameter.setDefaultValue(apiParam.defaultValue());
        }
        documentationParameter.setDescription(apiParam.value());
        // overwrite default name
        if (ModelUtils.isSet(apiParam.name())) {
            documentationParameter.setName(apiParam.name());
        }
        // documentationParameter.setNotes(apiParam.);
        documentationParameter.setParamAccess(apiParam.access());
        // required is default true in the annotation
        // so if its false, der RequestParam has set it
        if (!documentationParameter.isRequired()) {
            documentationParameter.setRequired(apiParam.required());
        }
    }

    private void addApiModelProperty(final ApiModelProperty apiModelProperty,
                                     final DocumentationParameter documentationParameter) {
        if (ModelUtils.isSet(apiModelProperty.allowableValues())) {
            List<String> allowableValues = Arrays.asList(apiModelProperty.allowableValues().split("\\s*,\\s*"));
            documentationParameter
                    .setAllowableValues(new AllowableListValues(JavaToScalaUtil.toScalaList(allowableValues), "LIST"));
        }

        if (ModelUtils.isSet(apiModelProperty.notes())) {
            documentationParameter.setDescription(apiModelProperty.notes());
        }

        if (ModelUtils.isSet(apiModelProperty.dataType())) {
            documentationParameter.setDataType(apiModelProperty.dataType());
        }

        documentationParameter.setDescription(apiModelProperty.value());

        documentationParameter.setParamAccess(apiModelProperty.access());

        if (!documentationParameter.isRequired()) {
            documentationParameter.setRequired(apiModelProperty.required());
        }



    }

    private boolean hasIgnorableAnnotations(List<Annotation> annotations) {
        for (Annotation annotation : annotations) {
            if (ignorableAnnotations.contains(annotation.annotationType().getCanonicalName())) {
                return true;
            }
        }
        return false;
    }

    class DocumentationParameter {

        private String name;
        private String description;
        private String defaultValue;
        private boolean required;
        private boolean allowMultiple;
        private String dataType;
        private String paramType;
        private String paramAccess;
        AllowableValues allowableValues;

        String getName() {
            return name;
        }

        void setName(String name) {
            this.name = name;
        }

        String getDescription() {
            return description;
        }

        void setDescription(String description) {
            this.description = description;
        }

        String getDefaultValue() {
            return defaultValue;
        }

        void setDefaultValue(String defaultValue) {
            this.defaultValue = defaultValue;
        }

        boolean isRequired() {
            return required;
        }

        void setRequired(boolean required) {
            this.required = required;
        }

        boolean isAllowMultiple() {
            return allowMultiple;
        }

        void setAllowMultiple(boolean allowMultiple) {
            this.allowMultiple = allowMultiple;
        }

        String getDataType() {
            return dataType;
        }

        void setDataType(String dataType) {
            this.dataType = dataType;
        }

        AllowableValues getAllowableValues() {
            return allowableValues;
        }

        void setAllowableValues(AllowableValues allowableValues) {
            this.allowableValues = allowableValues;
        }

        String getParamType() {
            return paramType;
        }

        void setParamType(String paramType) {
            this.paramType = paramType;
        }

        String getParamAccess() {
            return paramAccess;
        }

        void setParamAccess(String paramAccess) {
            this.paramAccess = paramAccess;
        }
    }
}
