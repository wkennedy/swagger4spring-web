package com.knappsack.swagger4springweb.parser;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ValueConstants;

import com.knappsack.swagger4springweb.model.AnnotatedParameter;
import com.knappsack.swagger4springweb.util.AnnotationUtils;
import com.wordnik.swagger.annotations.ApiParam;
import com.wordnik.swagger.core.ApiValues;
import com.wordnik.swagger.core.DocumentationAllowableListValues;
import com.wordnik.swagger.core.DocumentationParameter;

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
				if (annotation instanceof RequestParam) {
					addRequestParams((RequestParam) annotation,
							documentationParameter);
				} else if (annotation instanceof RequestHeader) {
					addRequestHeader((RequestHeader) annotation,
							documentationParameter);
				} else if (annotation instanceof RequestBody) {
					addRequestBody((RequestBody) annotation,
							documentationParameter, annotatedParameter.getParameterType());
				}
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
		} else if (Date.class.isAssignableFrom(parameterType)) {
			return "Date";
		} else if (Boolean.class.isAssignableFrom(parameterType)) {
			return "boolean";
		} else if (Number.class.isAssignableFrom(parameterType)) {
			return "double";
		}
		// others
		return "byte";
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
		documentationParameter.setParamType(ApiValues.TYPE_PATH);
	}

	private boolean isSet(String value) {
		return value != null && !value.trim().isEmpty() && !value.equals(ValueConstants.DEFAULT_NONE);
	}

	private void addRequestHeader(RequestHeader requestParam,
			DocumentationParameter documentationParameter) {
		if (isSet(requestParam.value())) {
			documentationParameter.setName(requestParam.value());
		}
		if (isSet(requestParam.defaultValue())) {
			documentationParameter.setDefaultValue(requestParam.defaultValue());
		}
		documentationParameter.setRequired(requestParam.required());
		documentationParameter.setParamType(ApiValues.TYPE_HEADER);
	}

	private void addRequestBody(RequestBody requestParam,
			DocumentationParameter documentationParameter, Class<?> returnType) {
		documentationParameter.setParamType(ApiValues.TYPE_BODY);
		//TODO check auf collection type
		documentationParameter.setDataType(returnType.getSimpleName());
	}

	private void addApiParams(ApiParam apiParam,
			DocumentationParameter documentationParameter) {
		if (isSet(apiParam.allowableValues())) {
			// we use only one simple string
			documentationParameter
					.setAllowableValues(new DocumentationAllowableListValues(
							Arrays.asList(new String[] { apiParam
									.allowableValues() })));
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
