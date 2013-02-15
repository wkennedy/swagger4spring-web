package com.sparc.swagger4springweb.parser;

import com.sparc.swagger4springweb.model.AnnotatedParameter;
import com.sparc.swagger4springweb.util.AnnotationUtils;
import com.wordnik.swagger.annotations.ApiParam;
import com.wordnik.swagger.core.ApiValues;
import com.wordnik.swagger.core.DocumentationParameter;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class DocumentationParameterParser {

    public List<DocumentationParameter> getDocumentationParams(Method method) {
        List<DocumentationParameter> documentationParameters = new ArrayList<DocumentationParameter>();
        List<AnnotatedParameter> annotatedParameters = AnnotationUtils.getAnnotatedParameters(method);
        for (AnnotatedParameter annotatedParameter : annotatedParameters) {
            DocumentationParameter documentationParameter = new DocumentationParameter();
            if(annotatedParameter.getAnnotation() instanceof ApiParam) {
                setDocumentationParameterByApiParam((ApiParam) annotatedParameter.getAnnotation(), documentationParameter);
            } else {
                String dataType = annotatedParameter.getParameterType().getSimpleName();
                documentationParameter.setDataType(dataType);
                documentationParameter.setName(annotatedParameter.getParameterName());
                documentationParameter.setValueTypeInternal(annotatedParameter.getParameterType().getName());
            }
            documentationParameters.add(documentationParameter);
        }

        return documentationParameters;
    }

    private void setDocumentationParameterByApiParam(ApiParam apiParam, DocumentationParameter documentationParameter) {
        //documentationParameter.setAllowableValues(apiParam.allowableValues());
        documentationParameter.setAllowMultiple(apiParam.allowMultiple());
        documentationParameter.setDataType(apiParam.value());
        documentationParameter.setDefaultValue(apiParam.defaultValue());
        //documentationParameter.setDescription(apiParam.internalDescription());
        documentationParameter.setInternalDescription(apiParam.internalDescription());
        documentationParameter.setName(apiParam.name());
        //documentationParameter.setNotes(apiParam.);
        documentationParameter.setParamAccess(apiParam.access());
        documentationParameter.setParamType(ApiValues.TYPE_PATH);
        documentationParameter.setRequired(apiParam.required());
        //documentationParameter.setValueTypeInternal();
    }
}
