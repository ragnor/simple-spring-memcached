package com.google.code.ssm.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation is necessary when you want to put simple spring memcached annotations in a class that extends a
 * generic class or interface. For example class SubGeneric<Number> that extends Generic<T>.
 * 
 * @author Jakub Białek
 * @since 2.0.0
 * 
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE })
public @interface BridgeMethodMappings {

    BridgeMethodMapping[] value();

}
