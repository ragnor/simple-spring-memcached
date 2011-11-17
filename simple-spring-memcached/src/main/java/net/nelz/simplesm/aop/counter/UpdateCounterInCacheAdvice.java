package net.nelz.simplesm.aop.counter;

import java.lang.reflect.Method;

import net.nelz.simplesm.aop.AnnotationData;
import net.nelz.simplesm.aop.AnnotationDataBuilder;
import net.nelz.simplesm.api.counter.UpdateCounterInCache;
import net.nelz.simplesm.providers.MemcacheTranscoder;
import net.nelz.simplesm.transcoders.LongToStringTranscoder;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Copyright (c) 2010, 2011 Jakub Białek
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
 * @author Jakub Białek
 * 
 */
@Aspect
public class UpdateCounterInCacheAdvice extends CounterInCacheBase {

    private static final Logger LOG = LoggerFactory.getLogger(UpdateCounterInCacheAdvice.class);

    private final MemcacheTranscoder<Long> transcoder = new LongToStringTranscoder();

    @Pointcut("@annotation(net.nelz.simplesm.api.counter.UpdateCounterInCache)")
    public void updateCounter() {
    }

    @AfterReturning(pointcut = "updateCounter()", returning = "retVal")
    public void cacheCounterInCache(final JoinPoint jp, final Object retVal) throws Throwable {
        // For Update*Cache, an AfterReturning aspect is fine. We will only
        // apply our caching after the underlying method completes successfully, and we will have
        // the same access to the method params.
        UpdateCounterInCache annotation;
        String cacheKey;
        try {
            Method methodToCache = getMethodToCache(jp);
            annotation = methodToCache.getAnnotation(UpdateCounterInCache.class);
            AnnotationData annotationData = AnnotationDataBuilder
                    .buildAnnotationData(annotation, UpdateCounterInCache.class, methodToCache);
            String[] objectsIds = getObjectIds(annotationData.getKeysIndex(), jp, methodToCache);
            cacheKey = buildCacheKey(objectsIds, annotationData);

            Object dataObject = annotationData.isReturnDataIndex() ? retVal : getIndexObject(annotationData.getDataIndex(), jp,
                    methodToCache);
            if (checkData(dataObject, jp)) {
                long value = ((Number) dataObject).longValue();
                set(cacheKey, annotation.expiration(), value, transcoder);
            }
        } catch (Exception ex) {
            getLogger().warn(String.format("Updating counter in cache via %s aborted due to an error.", jp.toShortString()), ex);
        }
    }

    @Override
    protected Logger getLogger() {
        return LOG;
    }

}
