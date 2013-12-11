/*
 * Copyright (c) 2012-2013 Jakub Białek
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

package com.google.code.ssm.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 
 * @author Jakub Białek
 * @since 2.0.0
 * 
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.ANNOTATION_TYPE)
public @interface ReadThroughMultiCacheOption {

    /**
     * If true then objects from result will be added to cache not by concatenating method arguments annotated by
     * {@link ParameterValueKeyProvider} but invoking {@link CacheKeyMethod} or toString() on each object in result
     * list. So order of the IDs on collection argument don't have to correspond the order of elements in result list.
     * 
     * It can increase performance increasing amount of objects added to cache if number of IDs in collection method
     * argument is different than number of objects in result list returned by intercepted method.
     * 
     * If the {@link #generateKeysFromResult()} is true it is incorrect to set {@link #addNullsToCache()} to true and
     * {@link #skipNullsInResult()} to false because subsequence invocations of an annotated method can return different
     * results.
     * 
     */
    boolean generateKeysFromResult() default false;

    /**
     * 
     * If true and {@link #generateKeysFromResult()} is true then null value will be added to cache under keys that
     * don't occurred in result list. If true and result from annotated method is null then null will be add to all keys
     * regardless of {@link #generateKeysFromResult()} value.
     * 
     */
    boolean addNullsToCache() default false;

    /**
     * If true then null values from cache are not added to result list.
     * 
     */
    boolean skipNullsInResult() default false;

}
