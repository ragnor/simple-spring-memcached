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

package com.google.code.ssm.aop.counter;

import org.aspectj.lang.JoinPoint;

import com.google.code.ssm.aop.CacheBase;

/**
 * @author Jakub Białek
 * @since 2.0.0
 * 
 */
abstract class CounterInCacheBase extends CacheBase {

    protected boolean checkData(final Object data, final JoinPoint pjp) {
        if (!isTypeSupported(data)) {
            getLogger().warn("Caching on {} aborted due to incorrect return type. Should be int, long, Integer or Long is {}",
                    new Object[] { pjp.toShortString(), (data == null) ? null : data.getClass() });
            return false;
        }

        return true;
    }

    protected boolean isReturnTypeSupported(final Class<?> clazz) {
        return int.class.equals(clazz) || Integer.class.equals(clazz) || long.class.equals(clazz) || Long.class.equals(clazz);
    }

    private boolean isTypeSupported(final Object result) {
        return result instanceof Long || result instanceof Integer;
    }

}
