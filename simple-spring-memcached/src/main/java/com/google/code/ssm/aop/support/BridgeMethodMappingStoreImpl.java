/*
 * Copyright (c) 2012 Jakub Białek
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the
 * Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.google.code.ssm.aop.support;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.google.code.ssm.api.BridgeMethodMapping;
import com.google.code.ssm.api.BridgeMethodMappings;

/**
 * 
 * @author Jakub Białek
 * @since 2.0.0
 * 
 */
public class BridgeMethodMappingStoreImpl implements BridgeMethodMappingStore {

    // class -> methodName -> erasedParamTypes -> targetParamTypes
    private final Map<Class<?>, Map<String, Map<String, Class<?>[]>>> map = new ConcurrentHashMap<Class<?>, Map<String, Map<String, Class<?>[]>>>();

    @Override
    public Class<?>[] getTargetParamsTypes(final Class<?> clazz, final String methodName, final Class<?>[] erasedParamTypes) {
        if (!map.containsKey(clazz)) {
            // initialize all class' bridge method mappings
            processBridgeMethodMappingsAnnotation(clazz);
        }

        Map<String, Map<String, Class<?>[]>> methodNameMap = map.get(clazz);
        Map<String, Class<?>[]> erasedParamTypesMap = null;
        Class<?>[] targetParamTypes = null;
        if (methodNameMap != null) {
            erasedParamTypesMap = methodNameMap.get(methodName);
        }

        String key = Arrays.toString(erasedParamTypes);
        if (erasedParamTypesMap != null) {
            targetParamTypes = erasedParamTypesMap.get(key);
        }

        if (targetParamTypes == null) {
            throw new RuntimeException(String.format(
                    "Annotation [%s] must be defined on [%s] for bridge method [%s] with erased param types [%s]",
                    BridgeMethodMappings.class.getName(), clazz.getName(), methodName, key));
        }

        return targetParamTypes;
    }

    private void processBridgeMethodMappingsAnnotation(final Class<?> clazz) {
        BridgeMethodMappings bridgeMethodMappings = clazz.getAnnotation(BridgeMethodMappings.class);
        Map<String, Map<String, Class<?>[]>> methodNameMap = new ConcurrentHashMap<String, Map<String, Class<?>[]>>();
        if (bridgeMethodMappings != null) {
            for (BridgeMethodMapping methodMapping : bridgeMethodMappings.value()) {
                addMethodMapping(clazz, methodNameMap, methodMapping.methodName(), methodMapping.erasedParamTypes(),
                        methodMapping.targetParamTypes());
            }
        }

        map.put(clazz, methodNameMap);
    }

    private void addMethodMapping(final Class<?> clazz, final Map<String, Map<String, Class<?>[]>> methodNameMap, final String methodName,
            final Class<?>[] erasedParamTypes, final Class<?>[] targetParamTypes) {
        Map<String, Class<?>[]> erasedParamTypesMap = methodNameMap.get(methodName);
        String erasedParamTypesKey = Arrays.toString(erasedParamTypes);
        if (erasedParamTypesMap == null) {
            if (erasedParamTypes.length != targetParamTypes.length) {
                throw new InvalidAnnotationException(
                        String.format(
                                "Annotation [%s] defined on class [%s] for method [%s] must have the same number of types in erasedParamTypes and targetParamTypes",
                                BridgeMethodMapping.class.getName(), clazz.getName(), methodName));
            }
            erasedParamTypesMap = new ConcurrentHashMap<String, Class<?>[]>();
            methodNameMap.put(methodName, erasedParamTypesMap);
        } else if (erasedParamTypesMap.containsKey(erasedParamTypesKey)) {
            throw new InvalidAnnotationException(
                    String.format(
                            "@BridgeMethodMappings annotation on class [%s] defines two or more mappings for the same bridge method, method name [%s], erased param types: [%s]",
                            clazz, methodName, erasedParamTypesKey));
        }
        erasedParamTypesMap.put(erasedParamTypesKey, targetParamTypes);
    }
}
