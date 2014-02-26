/**
 * Copyright (C) 2013 AlertMe.com Ltd
 */

package com.knappsack.swagger4springweb.filter;

import com.knappsack.swagger4springweb.annotation.ApiExclude;

public class ApiExcludeFilter extends AnnotationFilter<ApiExclude> {

    public ApiExcludeFilter() {
        super(ApiExclude.class);
    }

    @Override
    public boolean ignore(final ApiExclude annotation) {
        return true;
    }
}
