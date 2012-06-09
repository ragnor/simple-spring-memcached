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
 * This annotation is necessary for methods that come from generic class or interface and at least one of the parameter
 * is generic. Because of type erasure in such case types of method's parameters have to be provided.
 * 
 * <pre>
 * public interface Generic&lt;K, V&gt; {
 *     void set(K key, V value);
 * 
 *     V get(K key);
 * 
 *     void put(K key, V value, boolean overwrite);
 * }
 * 
 * &#064;BridgeMethodMappings({
 *         &#064;BridgeMethodMapping(methodName = &quot;set&quot;, erasedParamTypes = { Object.class, Object.class }, targetParamTypes = { Number.class,
 *                 String.class }),
 *         &#064;BridgeMethodMapping(methodName = &quot;get&quot;, erasedParamTypes = { Object.class }, targetParamTypes = { Number.class }),
 *         &#064;BridgeMethodMapping(methodName = &quot;put&quot;, erasedParamTypes = { Object.class, Object.class, boolean.class }, targetParamTypes = {
 *                 Number.class, String.class, boolean.class }) })
 * public class SubGeneric implements Generic&lt;Number, String&gt; {
 *     public void set(Number key, String value) {
 *     ..... 
 *     }
 * 
 *     public String get(Number key) {
 *     .... 
 *     }
 * 
 *     public void put(Number key, String value, boolean overwrite) {
 *     .... 
 *     }
 * }
 * </pre>
 * 
 * @author Jakub Białek
 * @since 2.0.0
 * 
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.ANNOTATION_TYPE })
public @interface BridgeMethodMapping {

    /**
     * 
     * @return the name of the method.
     */
    String methodName();

    /**
     * 
     * @return an array of erased types of method's parameters
     */
    Class<?>[] erasedParamTypes();

    /**
     * 
     * @return an array of target types of method's parameters
     */
    Class<?>[] targetParamTypes();

}
