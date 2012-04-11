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

package com.google.code.ssm.aop;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.code.ssm.aop.support.AnnotationData;
import com.google.code.ssm.aop.support.AnnotationDataBuilder;
import com.google.code.ssm.aop.support.InvalidAnnotationException;
import com.google.code.ssm.api.InvalidateMultiCache;

/**
 * 
 * @author Nelson Carpentier
 * @author Jakub Białek
 */
@Aspect
public class InvalidateMultiCacheAdvice extends CacheAdvice {

    private static final Logger LOG = LoggerFactory.getLogger(InvalidateMultiCacheAdvice.class);

    @Pointcut("@annotation(com.google.code.ssm.api.InvalidateMultiCache)")
    public void invalidateMulti() {
    }

    @Around("invalidateMulti()")
    public Object cacheInvalidateMulti(final ProceedingJoinPoint pjp) throws Throwable {
        if (isDisabled()) {
            getLogger().info("Cache disabled");
            return pjp.proceed();
        }

        // This is injected caching. If anything goes wrong in the caching, LOG
        // the crap outta it, but do not let it surface up past the AOP injection itself.
        Collection<String> cacheKeys = null;
        final AnnotationData data;
        final Method methodToCache;
        try {
            methodToCache = getCacheBase().getMethodToCache(pjp);
            final InvalidateMultiCache annotation = methodToCache.getAnnotation(InvalidateMultiCache.class);
            data = AnnotationDataBuilder.buildAnnotationData(annotation, InvalidateMultiCache.class, methodToCache);
            if (!data.isReturnKeyIndex()) {
                cacheKeys = getCacheBase().getCacheKeyBuilder().getCacheKeys(data, pjp.getArgs(), methodToCache.toString());
            }
        } catch (Throwable ex) {
            getLogger().warn(String.format("Caching on method %s aborted due to an error.", pjp.toShortString()), ex);
            return pjp.proceed();
        }

        final Object result = pjp.proceed();

        // This is injected caching. If anything goes wrong in the caching, LOG
        // the crap outta it, but do not let it surface up past the AOP injection itself.
        try {
            // If we have a -1 key index, then build the cacheKeys now.
            if (data.isReturnKeyIndex()) {
                if (!getCacheBase().verifyTypeIsList(result.getClass())) {
                    throw new InvalidAnnotationException(String.format("The return type is not a [%s]. "
                            + "The method [%s] does not fulfill the requirements.", List.class.getName(), methodToCache.toString()));
                }

                @SuppressWarnings("unchecked")
                final List<Object> keyObjects = (List<Object>) result;
                cacheKeys = getCacheBase().getCacheKeyBuilder().getCacheKeys(keyObjects, data.getNamespace());
            }
            getCacheBase().getCache(data).delete(cacheKeys);
        } catch (Throwable ex) {
            getLogger().warn(String.format("Caching on method %s aborted due to an error.", pjp.toShortString()), ex);
        }
        return result;

    }

    @Override
    protected Logger getLogger() {
        return LOG;
    }

}
