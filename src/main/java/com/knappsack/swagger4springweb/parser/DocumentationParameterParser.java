package com.knappsack.swagger4springweb.parser;

import com.knappsack.swagger4springweb.model.AnnotatedParameter;
import com.knappsack.swagger4springweb.util.AnnotationUtils;
import com.wordnik.swagger.annotations.ApiParam;
import com.wordnik.swagger.core.ApiValues;
import com.wordnik.swagger.core.DocumentationAllowableListValues;
import com.wordnik.swagger.core.DocumentationParameter;
import org.springframework.web.bind.annotation.*;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DocumentationParameterParser {

	public List<DocumentationParameter> getDocumentationParams(Method method) {
		List<DocumentationParameter> documentationParameters = new ArrayList<DocumentationParameter>();
		List<AnnotatedParameter> annotatedParameters = AnnotationUtils
				.getAnnotatedParameters(method);
		for (AnnotatedParameter annotatedParameter : annotatedParameters) {
			DocumentationParameter documentationParameter = new DocumentationParameter();
			// default values from the Method
			documentationParameter.setDataType(getSwaggerTypeFor(annotatedParameter.getParameterType()));
			documentationParameter.setName(annotatedParameter
					.getParameterName());
			documentationParameter.setValueTypeInternal(annotatedParameter.getParameterType().getName());
			// apply default values from spring annotations first
			for (Annotation annotation : annotatedParameter.getAnnotations()) {
				addSpringParams(annotation, documentationParameter);
			}
			// apply swagger annotations
			for (Annotation annotation : annotatedParameter.getAnnotations()) {
				if (annotation instanceof ApiParam) {
					addApiParams((ApiParam) annotation, documentationParameter);
				}
			}
			documentationParameters.add(documentationParameter);
		}

		return documentationParameters;
	}

	private String getSwaggerTypeFor(Class<?> parameterType) {
		// swagger types are 
		// byte             
		// boolean
		// int
		// long
		// float
		// double
		// string
		// Date
		if (String.class.isAssignableFrom(parameterType)) {
			return "string";
		} else if (Boolean.class.isAssignableFrom(parameterType)) {
			return "boolean";
		} else if(Byte.class.isAssignableFrom(parameterType)) {
            return "byte";
        }  else if(Long.class.isAssignableFrom(parameterType)) {
            return "long";
        }  else if(Integer.class.isAssignableFrom(parameterType)) {
            return "int";
        }  else if(Float.class.isAssignableFrom(parameterType)) {
            return "float";
        } else if (Number.class.isAssignableFrom(parameterType)) {
			return "double";
		}
		// others
		return parameterType.getSimpleName();
	}

    private void addSpringParams(Annotation annotation, DocumentationParameter documentationParameter) {
        if (annotation instanceof RequestParam) {
            addRequestParams((RequestParam) annotation, documentationParameter);
        }
        if (annotation instanceof RequestHeader) {
            addRequestHeader((RequestHeader) annotation, documentationParameter);
        }
        if(annotation instanceof RequestBody) {
            addRequestBody(documentationParameter);
        }
        if(annotation instanceof PathVariable) {
            addPathVariable((PathVariable) annotation, documentationParameter);
        }
    }

    private void addRequestBody(DocumentationParameter documentationParameter) {
        documentationParameter.setRequired(true);
        documentationParameter.setParamType(ApiValues.TYPE_BODY);
    }

    private void addPathVariable(PathVariable pathVariable, DocumentationParameter documentationParameter) {
        if (isSet(pathVariable.value())) {
            documentationParameter.setName(pathVariable.value());
        }

        documentationParameter.setRequired(true);
        documentationParameter.setParamType(ApiValues.TYPE_PATH);
    }

	private void addRequestParams(RequestParam requestParam,
			DocumentationParameter documentationParameter) {
		if (isSet(requestParam.value())) {
			documentationParameter.setName(requestParam.value());
		}
		if (isSet(requestParam.defaultValue())) {
			documentationParameter.setDefaultValue(requestParam.defaultValue());
		}
		documentationParameter.setRequired(requestParam.required());
		documentationParameter.setParamType(ApiValues.TYPE_QUERY);
	}

	private boolean isSet(String value) {
		return value != null && !value.trim().isEmpty() && !ValueConstants.DEFAULT_NONE.equals(value);
	}

	private void addRequestHeader(RequestHeader requestHeader,
			DocumentationParameter documentationParameter) {
		if (isSet(requestHeader.value())) {
			documentationParameter.setName(requestHeader.value());
		}
		if (isSet(requestHeader.defaultValue())) {
			documentationParameter.setDefaultValue(requestHeader.defaultValue());
		}
		documentationParameter.setRequired(requestHeader.required());
		documentationParameter.setParamType(ApiValues.TYPE_HEADER);
	}

	private void addApiParams(ApiParam apiParam,
			DocumentationParameter documentationParameter) {
		if (isSet(apiParam.allowableValues())) {
			// we use only one simple string
			documentationParameter
					.setAllowableValues(new DocumentationAllowableListValues(
							Arrays.asList(apiParam.allowableValues())));
		}
		documentationParameter.setAllowMultiple(apiParam.allowMultiple());

		if (isSet(apiParam.defaultValue())) {
			documentationParameter.setDefaultValue(apiParam.defaultValue());
		}
		documentationParameter.setDescription(apiParam.value());
		documentationParameter.setInternalDescription(apiParam
				.internalDescription());
		// overwrite default name
		if (isSet(apiParam.name())) {
			documentationParameter.setName(apiParam.name());
		}
		// documentationParameter.setNotes(apiParam.);
		documentationParameter.setParamAccess(apiParam.access());
		// required is default true in the annotation
		// so if its false, der RequestParam has set it
		if (!documentationParameter.getRequired()) {
			documentationParameter.setRequired(apiParam.required());
		}
	}
}
