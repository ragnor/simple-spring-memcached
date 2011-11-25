/*
 * Copyright (c) 2008-2009 Nelson Carpentier
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

package com.google.code.ssm.impl;

import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.*;

import org.springframework.stereotype.Service;

import com.google.code.ssm.aop.CacheKeyMethodStore;
import com.google.code.ssm.api.CacheKeyMethod;
import com.google.code.ssm.exceptions.InvalidAnnotationException;

/**
 * 
 * @author Nelson Carpentier
 * 
 */
@Service("methodStore")
public class CacheKeyMethodStoreImpl implements CacheKeyMethodStore {

    final Map<Class<?>, Method> map = new ConcurrentHashMap<Class<?>, Method>();

    public Method getKeyMethod(final Class<?> keyClass) throws NoSuchMethodException {
        final Method storedMethod = find(keyClass);
        if (storedMethod != null) {
            return storedMethod;
        }
        final Method[] methods = keyClass.getDeclaredMethods();
        Method targetMethod = null;
        for (final Method method : methods) {
            if (method != null && method.getAnnotation(CacheKeyMethod.class) != null) {
                if (method.getParameterTypes().length > 0) {
                    throw new InvalidAnnotationException(String.format("Method [%s] must have 0 arguments to be annotated with [%s]",
                            method.toString(), CacheKeyMethod.class.getName()));
                }
                if (!String.class.equals(method.getReturnType())) {
                    throw new InvalidAnnotationException(String.format("Method [%s] must return a String to be annotated with [%s]",
                            method.toString(), CacheKeyMethod.class.getName()));
                }
                if (targetMethod != null) {
                    throw new InvalidAnnotationException(String.format(
                            "Class [%s] should have only one method annotated with [%s]. See [%s] and [%s]", keyClass.getName(),
                            CacheKeyMethod.class.getName(), targetMethod.getName(), method.getName()));
                }
                targetMethod = method;
            }
        }

        if (targetMethod == null) {
            targetMethod = keyClass.getMethod("toString", (Class<?>[]) null);
        }

        add(keyClass, targetMethod);

        return targetMethod;
    }

    protected void add(final Class<?> key, final Method value) {
        map.put(key, value);
    }

    protected Method find(final Class<?> key) {
        return map.get(key);
    }

}
