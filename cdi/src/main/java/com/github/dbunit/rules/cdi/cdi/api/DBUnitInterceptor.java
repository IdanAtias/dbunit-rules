package com.github.dbunit.rules.cdi.cdi.api;

import javax.interceptor.InterceptorBinding;
import java.lang.annotation.*;

/**
 * Created by rafael-pestano on 22/07/2015.
 */
@InterceptorBinding
@Target({ ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface DBUnitInterceptor {


}