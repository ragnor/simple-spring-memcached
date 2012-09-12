/*
 * Copyright (c) 2008-2012 Nelson Carpentier, Jakub Białek
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
 * 
 */

package com.google.code.ssm.aop.support;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.google.code.ssm.api.CacheKeyMethod;

/**
 * 
 * @author Nelson Carpentier
 * @author Jakub Białek
 * 
 */
public class CacheKeyMethodStoreImpl implements CacheKeyMethodStore { // NO_UCD

    public static final String DEFAULT_KEY_METHOD_NAME = "toString";

    private final Map<Class<?>, Method> map = new ConcurrentHashMap<Class<?>, Method>();

    @Override
    public Method getKeyMethod(final Class<?> keyClass) throws NoSuchMethodException {
        final Method storedMethod = find(keyClass);
        if (storedMethod != null) {
            return storedMethod;
        }
        final Method[] methods = keyClass.getDeclaredMethods();
        Method targetMethod = null;
        for (final Method method : methods) {
            boolean isCacheKeyMethod = isCacheKeyMethod(method);
            if (isCacheKeyMethod && (targetMethod != null)) {
                throw new InvalidAnnotationException(String.format(
                        "Class [%s] should have only one method annotated with [%s]. See [%s] and [%s]", keyClass.getName(),
                        CacheKeyMethod.class.getName(), targetMethod.getName(), method.getName()));
            } else if (isCacheKeyMethod) {
                targetMethod = method;
            }
        }

        if (targetMethod == null) {
            // try to get from superclass
            Class<?> superKeyClass = keyClass.getSuperclass();
            if (superKeyClass != null) {
                targetMethod = getKeyMethod(superKeyClass);
            }

            if (targetMethod == null || DEFAULT_KEY_METHOD_NAME.equals(targetMethod.getName())) {
                targetMethod = keyClass.getMethod(DEFAULT_KEY_METHOD_NAME, (Class<?>[]) null);
            }
        }

        add(keyClass, targetMethod);

        return targetMethod;
    }

    private boolean isCacheKeyMethod(final Method method) {
        if (method != null && method.getAnnotation(CacheKeyMethod.class) != null) {
            if (method.getParameterTypes().length > 0) {
                throw new InvalidAnnotationException(String.format("Method [%s] must have 0 arguments to be annotated with [%s]",
                        method.toString(), CacheKeyMethod.class.getName()));
            }
            if (!String.class.equals(method.getReturnType())) {
                throw new InvalidAnnotationException(String.format("Method [%s] must return a String to be annotated with [%s]",
                        method.toString(), CacheKeyMethod.class.getName()));
            }

            return true;
        }

        return false;
    }

    private void add(final Class<?> key, final Method value) {
        map.put(key, value);
    }

    private Method find(final Class<?> key) {
        return map.get(key);
    }

}
