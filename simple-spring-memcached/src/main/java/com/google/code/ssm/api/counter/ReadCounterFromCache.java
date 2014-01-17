/*
 * Copyright (c) 2010-2014 Jakub Białek
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

package com.google.code.ssm.api.counter;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.google.code.ssm.api.AnnotationConstants;

/**
 * 
 * Reads counter value from cache. If counter doesn't exist intercepted method is executed and return value is used to
 * initialize counter. Annotated method must return one of int, {@link Integer}, long or {@link Long}.
 * 
 * @author Jakub Białek
 * @since 2.0.0
 * 
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ReadCounterFromCache {

    /**
     * A namespace that is added to the key as it is stored in the distributed cache. This allows differing object that
     * may have the same ID to coexist. This value must be assigned.
     * 
     * @return the namespace for the objects cached in the given method.
     */
    String namespace() default AnnotationConstants.DEFAULT_STRING;

    /**
     * The exp value is passed along to cache exactly as given, and will be processed per the memcached protocol
     * specification:
     * 
     * The actual value sent may either be Unix time (number of seconds since January 1, 1970, as a 32-bit value), or a
     * number of seconds starting from current time. In the latter case, this number of seconds may not exceed
     * 60*60*24*30 (number of seconds in 30 days); if the number sent by a client is larger than that, the server will
     * consider it to be real Unix time value rather than an offset from current time.
     * 
     * (Also note: a value of 0 means the given value should never expire. The value is still susceptible to purging by
     * memcached for space and LRU (least recently used) considerations.)
     * 
     * @return expiration of counter
     */
    int expiration() default 0;

}
