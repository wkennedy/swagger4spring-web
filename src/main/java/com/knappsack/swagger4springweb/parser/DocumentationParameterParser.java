package com.knappsack.swagger4springweb.parser;

import com.knappsack.swagger4springweb.model.AnnotatedParameter;
import com.knappsack.swagger4springweb.util.AnnotationUtils;
import com.knappsack.swagger4springweb.util.DocumentationUtils;
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

    private List<String> ignorableAnnotations;

    public DocumentationParameterParser() {
        ignorableAnnotations = new ArrayList<String>();
    }

    public DocumentationParameterParser(List<String> ignorableAnnotations) {
        this.ignorableAnnotations = ignorableAnnotations;
    }

    public List<DocumentationParameter> getDocumentationParams(Method method) {
		List<DocumentationParameter> documentationParameters = new ArrayList<DocumentationParameter>();
		List<AnnotatedParameter> annotatedParameters = AnnotationUtils
				.getAnnotatedParameters(method);
		for (AnnotatedParameter annotatedParameter : annotatedParameters) {
            if(hasIgnorableAnnotations(annotatedParameter.getAnnotations())) {
               continue;
            }
			DocumentationParameter documentationParameter = new DocumentationParameter();
			// default values from the Method
			documentationParameter.setDataType(DocumentationUtils.getSwaggerTypeFor(annotatedParameter.getParameterType()));
			documentationParameter.setName(annotatedParameter.getParameterName());
			documentationParameter.setValueTypeInternal(annotatedParameter.getParameterType().getName());
            documentationParameter.setAllowMultiple(DocumentationUtils.isAllowMultiple(annotatedParameter.getParameterType()));
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
        if (DocumentationUtils.isSet(pathVariable.value())) {
            documentationParameter.setName(pathVariable.value());
        }

        documentationParameter.setRequired(true);
        documentationParameter.setParamType(ApiValues.TYPE_PATH);
    }

	private void addRequestParams(RequestParam requestParam,
			DocumentationParameter documentationParameter) {
		if (DocumentationUtils.isSet(requestParam.value())) {
			documentationParameter.setName(requestParam.value());
		}
		if (DocumentationUtils.isSet(requestParam.defaultValue())) {
			documentationParameter.setDefaultValue(requestParam.defaultValue());
		}
		documentationParameter.setRequired(requestParam.required());
        if("file".equals(documentationParameter.getDataType())) {
            documentationParameter.setParamType(ApiValues.TYPE_FORM);
        } else {
            documentationParameter.setParamType(ApiValues.TYPE_QUERY);
        }
	}

	private void addRequestHeader(RequestHeader requestHeader,
			DocumentationParameter documentationParameter) {
		if (DocumentationUtils.isSet(requestHeader.value())) {
			documentationParameter.setName(requestHeader.value());
		}
		if (DocumentationUtils.isSet(requestHeader.defaultValue())) {
			documentationParameter.setDefaultValue(requestHeader.defaultValue());
		}
		documentationParameter.setRequired(requestHeader.required());
		documentationParameter.setParamType(ApiValues.TYPE_HEADER);
	}

	private void addApiParams(ApiParam apiParam,
			DocumentationParameter documentationParameter) {
		if (DocumentationUtils.isSet(apiParam.allowableValues())) {
			// we use only one simple string
            List<String> allowableValues = Arrays.asList(apiParam.allowableValues().split("\\s*,\\s*"));
			documentationParameter.setAllowableValues(new DocumentationAllowableListValues(allowableValues));
		}
		documentationParameter.setAllowMultiple(apiParam.allowMultiple());

		if (DocumentationUtils.isSet(apiParam.defaultValue())) {
			documentationParameter.setDefaultValue(apiParam.defaultValue());
		}
		documentationParameter.setDescription(apiParam.value());
		documentationParameter.setInternalDescription(apiParam
				.internalDescription());
		// overwrite default name
		if (DocumentationUtils.isSet(apiParam.name())) {
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

    private boolean hasIgnorableAnnotations(List<Annotation> annotations) {
        for(Annotation annotation : annotations) {
            if(ignorableAnnotations.contains(annotation.annotationType().getCanonicalName())) {
                return true;
            }
        }
        return false;
    }
}
