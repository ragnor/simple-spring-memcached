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
package com.google.code.ssm.aop;

import org.slf4j.Logger;
import org.springframework.core.Ordered;

/**
 * 
 * @author Jakub Białek
 * @since 2.0.0
 * 
 */
public abstract class CacheAdvice implements Ordered {

    public static final String DISABLE_CACHE_PROPERTY = "ssm.cache.disable";

    public static final String DISABLE_CACHE_PROPERTY_VALUE = "true";

    private CacheBase cacheBase;

    public CacheBase getCacheBase() {
        return cacheBase;
    }

    public void setCacheBase(final CacheBase cacheBase) {
        this.cacheBase = cacheBase;
    }

    @Override
    public int getOrder() {
        return cacheBase.getSettings().getOrder();
    }

    protected boolean isDisabled() {
        return DISABLE_CACHE_PROPERTY_VALUE.equals(System.getProperty(DISABLE_CACHE_PROPERTY));
    }

    protected void warn(final Throwable e, final String format, final Object... args) {
        if (getLogger().isWarnEnabled()) {
            getLogger().warn(String.format(format, args), e);
        }
    }

    protected abstract Logger getLogger();

}
