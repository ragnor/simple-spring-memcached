/*
 * Copyright (c) 2012-2014 Jakub Białek
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

package com.google.code.ssm.providers;

import java.util.Collection;
import java.util.concurrent.TimeoutException;

/**
 * 
 * @author Jakub Białek
 * @since 2.0.0
 * 
 */
public abstract class AbstractMemcacheClientWrapper implements CacheClient {

    @Override
    public void delete(final Collection<String> keys) throws TimeoutException, CacheException {
        if (keys == null || keys.isEmpty()) {
            return;
        }

        for (final String key : keys) {
            if (key != null) {
                delete(key);
            }
        }

    }

}
