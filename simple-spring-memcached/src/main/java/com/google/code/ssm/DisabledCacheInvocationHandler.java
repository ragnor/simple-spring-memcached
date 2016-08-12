/*
 * Copyright (c) 2014-2016 Jakub Białek
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

package com.google.code.ssm;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Collection;

/**
 * 
 * Handle all invocations to disabled {@link Cache}. All methods except {@link Cache#getName()},
 * {@link Cache#getAliases()}, {@link Cache#isEnabled()}, {@link Cache#getProperties()}, {@link Cache#shutdown()} will
 * throw {@link IllegalStateException}.
 * 
 * @author Jakub Białek
 * @since 3.5.0
 * 
 */
public class DisabledCacheInvocationHandler implements InvocationHandler {

    private final String cacheName;

    private final Collection<String> cacheAliases;
    
    private final CacheProperties cacheProperties = new CacheProperties();

    public DisabledCacheInvocationHandler(String cacheName, Collection<String> cacheAliases) {
        this.cacheName = cacheName;
        this.cacheAliases = cacheAliases;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        final String methodName = method.getName();

        if ("getName".equals(methodName)) {
            return cacheName;
        } else if ("getAliases".equals(methodName)) {
            return cacheAliases;
        } else if ("isEnabled".equals(methodName)) {
            return false;
        } else if ("shutdown".equals(methodName)) {
            return null;
        } else if ("getProperties".equals(methodName)) {
            return cacheProperties;
        }

        throw new IllegalStateException(String.format("Cache with name %s and aliases %s is disabled for method %s", cacheName,
                cacheAliases, methodName));
    }

}
