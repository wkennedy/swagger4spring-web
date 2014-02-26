package com.knappsack.swagger4springweb.filter;

import java.lang.reflect.Method;

public interface Filter {

    boolean isApplicable(Method method);

    boolean ignore(Method method);

}
