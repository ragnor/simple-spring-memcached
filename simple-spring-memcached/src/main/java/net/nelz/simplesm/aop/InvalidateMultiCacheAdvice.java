package net.nelz.simplesm.aop;

import java.lang.reflect.Method;
import java.util.List;

import net.nelz.simplesm.api.InvalidateMultiCache;
import net.nelz.simplesm.exceptions.InvalidAnnotationException;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Copyright (c) 2008, 2009 Nelson Carpentier
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
 * @author Nelson Carpentier
 */
@Aspect
public class InvalidateMultiCacheAdvice extends CacheBase {
    private static final Logger LOG = LoggerFactory.getLogger(InvalidateMultiCacheAdvice.class);

    @Pointcut("@annotation(net.nelz.simplesm.api.InvalidateMultiCache)")
    public void invalidateMulti() {
    }

    @Around("invalidateMulti()")
    public Object cacheInvalidateMulti(final ProceedingJoinPoint pjp) throws Throwable {
        // This is injected caching. If anything goes wrong in the caching, LOG
        // the crap outta it, but do not let it surface up past the AOP injection itself.
        List<String> cacheKeys = null;
        final AnnotationData annotationData;
        final String methodDescription;
        try {
            final Method methodToCache = getMethodToCache(pjp);
            methodDescription = methodToCache.toString();
            final InvalidateMultiCache annotation = methodToCache.getAnnotation(InvalidateMultiCache.class);
            annotationData = AnnotationDataBuilder.buildAnnotationData(annotation, InvalidateMultiCache.class, methodToCache);
            if (!annotationData.isReturnKeyIndex()) {
                // FIXME only one key index is used, should getKeyIndexes()
                final Object keyObject = getIndexObject(annotationData.getKeyIndex(), pjp, methodToCache);
                // FIXME only one key index is used, should getKeyIndexes()
                final List<Object> keyObjects = convertToKeyObjects(keyObject, annotationData.getKeyIndex(), methodDescription);
                cacheKeys = getCacheKeys(keyObjects, annotationData);
            }
        } catch (Throwable ex) {
            warn("Caching on " + pjp.toShortString() + " aborted due to an error.", ex);
            return pjp.proceed();
        }

        final Object result = pjp.proceed();

        // This is injected caching. If anything goes wrong in the caching, LOG
        // the crap outta it, but do not let it surface up past the AOP injection itself.
        try {
            // If we have a -1 key index, then build the cacheKeys now.
            if (annotationData.isReturnKeyIndex()) {
                // FIXME only one key index is used, should getKeyIndexes()
                final List<Object> keyObjects = convertToKeyObjects(result, annotationData.getKeyIndex(), methodDescription);
                cacheKeys = getCacheKeys(keyObjects, annotationData);
            }
            if (cacheKeys != null && cacheKeys.size() > 0) {
                for (final String key : cacheKeys) {
                    if (key != null && key.trim().length() > 0) {
                        delete(key);
                    }
                }
            }
        } catch (Throwable ex) {
            warn("Caching on " + pjp.toShortString() + " aborted due to an error.", ex);
        }
        return result;

    }

    @SuppressWarnings("unchecked")
    protected List<Object> convertToKeyObjects(final Object keyObject, final int keyIndex, final String methodDescription) throws Exception {
        if (verifyTypeIsList(keyObject.getClass())) {
            return (List<Object>) keyObject;
        }
        throw new InvalidAnnotationException(String.format("The parameter object found at dataIndex [%s] is not a [%s]. "
                + "[%s] does not fulfill the requirements.", keyIndex, List.class.getName(), methodDescription));
    }

    @Override
    protected Logger getLogger() {
        return LOG;
    }

}
