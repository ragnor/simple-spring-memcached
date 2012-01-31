package com.google.code.ssm.aop.support;

/**
 * 
 * @author Jakub Białek
 * @since 2.0.0
 * 
 */
public interface BridgeMethodMappingStore {

    Class<?>[] getTargetParamsTypes(final Class<?> clazz, final String methodName, final Class<?>[] erasedParamTypes);

}
