package net.nelz.simplesm.aop;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.ProceedingJoinPoint;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.lang.reflect.Method;
import java.util.List;
import java.util.ArrayList;

import net.nelz.simplesm.annotations.InvalidateMultiCache;
import net.nelz.simplesm.exceptions.InvalidAnnotationException;

/**
Copyright (c) 2008  Nelson Carpentier

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
 */
@Aspect
public class InvalidateMultiCacheAdvice extends CacheBase {
    private static final Log LOG = LogFactory.getLog(InvalidateMultiCacheAdvice.class);

    @Pointcut("@annotation(net.nelz.simplesm.annotations.InvalidateMultiCache)")
    public void invalidateMulti() {}

    @Around("invalidateMulti()")
    public Object cacheInvalidateMulti(final ProceedingJoinPoint pjp) throws Throwable {
        // This is injected caching.  If anything goes wrong in the caching, LOG the crap outta it,
        // but do not let it surface up past the AOP injection itself.
        List<String> cacheKeys = null;
        final AnnotationData annotationData;
        final String methodDescription;
        try {
            final Method methodToCache = getMethodToCache(pjp);
            methodDescription = methodToCache.toString();
            final InvalidateMultiCache annotation = methodToCache.getAnnotation(InvalidateMultiCache.class);
            annotationData =
                    AnnotationDataBuilder.buildAnnotationData(annotation,
                            InvalidateMultiCache.class,
                            methodToCache.getName());
            if (annotationData.getKeyIndex() > -1) {
                final Object keyObject = getKeyObject(annotationData.getKeyIndex(), pjp, methodToCache);
                final List<Object> keyObjects = convertToKeyObjects(keyObject, annotationData.getKeyIndex(), methodDescription);
                cacheKeys = getCacheKeys(keyObjects, annotationData);
            }
        } catch (Throwable ex) {
            LOG.warn("Caching on " + pjp.toShortString() + " aborted due to an error.", ex);
            return pjp.proceed();
        }

        final Object result = pjp.proceed();

        // This is injected caching.  If anything goes wrong in the caching, LOG the crap outta it,
        // but do not let it surface up past the AOP injection itself.
        try {
            // If we have a -1 key index, then build the cacheKeys now.
            if (annotationData.getKeyIndex() == -1) {
                final List<Object> keyObjects = convertToKeyObjects(result, annotationData.getKeyIndex(), methodDescription);
                cacheKeys = getCacheKeys(keyObjects, annotationData);
            }
            if (cacheKeys != null && cacheKeys.size() > 0) {
                for (final String key : cacheKeys) {
                    if (key != null && key.trim().length() > 0) {
                        cache.delete(key);
                    }
                }
            }
        } catch (Throwable ex) {
            LOG.warn("Caching on " + pjp.toShortString() + " aborted due to an error.", ex);
        }
        return result;

    }

    protected List<Object> convertToKeyObjects(final Object keyObject,
                                               final int keyIndex,
                                               final String methodDescription) throws Exception {
        if (verifyTypeIsList(keyObject.getClass())) {
            return (List<Object>) keyObject;
        }
        throw new InvalidAnnotationException(String.format(
                "The parameter object found at keyIndex [%s] is not a [%s]. " +
                "[%s] does not fulfill the requirements.",
                keyIndex,
                List.class.getName(),
                methodDescription
        ));
    }
}
