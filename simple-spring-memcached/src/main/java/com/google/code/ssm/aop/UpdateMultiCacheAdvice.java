package com.google.code.ssm.aop;

import java.lang.reflect.Method;
import java.util.List;

import com.google.code.ssm.api.UpdateMultiCache;
import com.google.code.ssm.exceptions.InvalidAnnotationException;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
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
 * 
 */
@Aspect
public class UpdateMultiCacheAdvice extends CacheBase {
    private static final Logger LOG = LoggerFactory.getLogger(UpdateMultiCacheAdvice.class);

    @Pointcut("@annotation(com.google.code.ssm.api.UpdateMultiCache)")
    public void updateMulti() {
    }

    @SuppressWarnings("unchecked")
    @AfterReturning(pointcut = "updateMulti()", returning = "retVal")
    public void cacheUpdateSingle(final JoinPoint jp, final Object retVal) throws Throwable {
        // For Update*Cache, an AfterReturning aspect is fine. We will only
        // apply our caching after the underlying method completes successfully, and we will have
        // the same access to the method params.
        try {
            final Method methodToCache = getMethodToCache(jp);
            final UpdateMultiCache annotation = methodToCache.getAnnotation(UpdateMultiCache.class);
            final AnnotationData annotationData = AnnotationDataBuilder.buildAnnotationData(annotation, UpdateMultiCache.class,
                    methodToCache);
            final List<Object> dataList = this.<List<Object>>getUpdateData(annotationData, methodToCache, jp, retVal);
            Class<?> jsonClass = getJsonClass(methodToCache, annotationData.getDataIndex());
            // FIXME only one key index is used, should getKeyIndexes()
            final List<Object> keyObjects = getKeyObjects(annotationData.getKeyIndex(), retVal, jp, methodToCache);
            final List<String> cacheKeys = getCacheKeys(keyObjects, annotationData);
            updateCache(cacheKeys, dataList, methodToCache, annotationData, jsonClass);
        } catch (Exception ex) {
            warn("Updating caching via " + jp.toShortString() + " aborted due to an error.", ex);
        }
    }

    protected void updateCache(final List<String> cacheKeys, final List<Object> returnList, final Method methodToCache,
            final AnnotationData annotationData, Class<?> jsonClass) {
        if (returnList.size() != cacheKeys.size()) {
            throw new InvalidAnnotationException(String.format(
                    "The key generation objects, and the resulting objects do not match in size for [%s].", methodToCache.toString()));
        }

        for (int ix = 0; ix < returnList.size(); ix++) {
            final Object result = returnList.get(ix);
            final String cacheKey = cacheKeys.get(ix);
            final Object cacheObject = getSubmission(result);
            setSilently(cacheKey, annotationData.getExpiration(), cacheObject, jsonClass);
        }
    }

    @SuppressWarnings("unchecked")
    protected List<Object> getKeyObjects(final int keyIndex, final Object returnValue, final JoinPoint jp, final Method methodToCache)
            throws Exception {
        final Object keyObject = keyIndex == -1 ? validateReturnValueAsKeyObject(returnValue, methodToCache) : getIndexObject(keyIndex, jp,
                methodToCache);
        if (verifyTypeIsList(keyObject.getClass())) {
            return (List<Object>) keyObject;
        }
        throw new InvalidAnnotationException(String.format("The parameter object found at dataIndex [%s] is not a [%s]. "
                + "[%s] does not fulfill the requirements.", UpdateMultiCache.class.getName(), List.class.getName(),
                methodToCache.toString()));
    }

    @Override
    protected Logger getLogger() {
        return LOG;
    }

}
