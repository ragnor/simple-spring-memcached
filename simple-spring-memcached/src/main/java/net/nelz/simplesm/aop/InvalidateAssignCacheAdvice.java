package net.nelz.simplesm.aop;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.ProceedingJoinPoint;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.lang.reflect.Method;
import java.security.InvalidParameterException;

import net.nelz.simplesm.annotations.InvalidateSingleCache;
import net.nelz.simplesm.annotations.InvalidateAssignCache;

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
public class InvalidateAssignCacheAdvice extends CacheBase {
    private static final Log LOG = LogFactory.getLog(InvalidateAssignCacheAdvice.class);

    @Pointcut("@annotation(net.nelz.simplesm.annotations.InvalidateAssignCache)")
    public void invalidateAssign() {}

    @Around("invalidateAssign()")
    public Object cacheInvalidateAssign(final ProceedingJoinPoint pjp) throws Throwable {
        final Object result = pjp.proceed();

        // This is injected caching.  If anything goes wrong in the caching, LOG the crap outta it,
        // but do not let it surface up past the AOP injection itself.
        try {
            final Method methodToCache = getMethodToCache(pjp);
            final InvalidateAssignCache annotation = methodToCache.getAnnotation(InvalidateAssignCache.class);
            final AnnotationData annotationData =
                    AnnotationDataBuilder.buildAnnotationData(annotation,
                            InvalidateAssignCache.class,
                            methodToCache.getName());
            
            final String cacheKey = buildCacheKey(annotationData.getAssignedKey(), annotationData);
            if (cacheKey == null || cacheKey.trim().length() == 0) {
                throw new InvalidParameterException("Unable to find a cache key");
            }
            cache.delete(cacheKey);
        } catch (Throwable ex) {
            LOG.warn("Caching on " + pjp.toShortString() + " aborted due to an error.", ex);
        }
        return result;
    }
}
