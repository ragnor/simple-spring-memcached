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
public @interface UpdateMultiCacheOption {

    /**
     * If no one argument is annotated together by {@link ParameterDataUpdateContent} and
     * {@link ParameterValueKeyProvider} or method is not annotated by both {@link ReturnValueKeyProvider} and
     * {@link ReturnDataUpdateContent} then null values will be added under keys that occurred in list parameter
     * annotated by {@link ParameterValueKeyProvider} or {@link ReturnValueKeyProvider} but not occurred in
     * {@link ParameterDataUpdateContent} or {@link ReturnDataUpdateContent}. Example: <br/>
     * 
     * @UpdateMultiCache(namespace = "NS1", expiration = 0, option=@UpdateMultiCacheOption(addNullsToCache = true)) <br/>
     *                             public List<ApplicationUser> getUsersList(@ParameterValueKeyProvider(order = 1) int
     *                             applicationId, @ParameterValueKeyProvider(order = 0) List<Integer> userIds) <br/>
     * <br/>
     *                             invocation: getUsersList(1, Arrays.asList(1,2,3,4,5)) will return entities for userId
     *                             1,3,4 then null values will be added for userId 2 and 5.
     * 
     * @return
     */
    boolean addNullsToCache() default false;

    /**
     * If {@link #addNullsToCache()} and this property is set to true then regardless of current value in cache null
     * marked will be set to cache under missed value (set memcached command), otherwise null marked will be added only
     * if current value in cache is null (add memcached command).
     * 
     * @return
     */
    boolean overwriteNoNulls() default false;

}
