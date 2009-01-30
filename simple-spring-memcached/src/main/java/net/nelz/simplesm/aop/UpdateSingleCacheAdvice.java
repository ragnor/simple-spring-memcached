package net.nelz.simplesm.aop;

import net.nelz.simplesm.annotations.*;
import org.apache.commons.logging.*;
import org.aspectj.lang.*;
import org.aspectj.lang.annotation.*;

import java.lang.reflect.*;

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
public class UpdateSingleCacheAdvice extends CacheBase {
	private static final Log LOG = LogFactory.getLog(UpdateSingleCacheAdvice.class);

	@Pointcut("@annotation(net.nelz.simplesm.annotations.UpdateSingleCache)")
	public void updateSingle() {}

	@AfterReturning(pointcut="updateSingle()", returning="retVal")
	public Object cacheUpdateSingle(final JoinPoint jp, final Object retVal) throws Throwable {
		try {
			final Method methodToCache = getMethodToCache(jp);
			final UpdateSingleCache annotation = methodToCache.getAnnotation(UpdateSingleCache.class);
			validateAnnotation(annotation, methodToCache);
            final AnnotationData annotationData =
                    AnnotationDataBuilder.buildAnnotationData(annotation,
                            UpdateSingleCache.class,
                            methodToCache.getName());
            final String objectId = getObjectId(annotationData.getKeyIndex(), retVal, jp, methodToCache);
			final String cacheKey = buildCacheKey(objectId, annotationData);
			final Object submission = (retVal == null) ? new PertinentNegativeNull() : retVal;
			cache.set(cacheKey, annotationData.getExpiration(), submission);
		} catch (Exception ex) {
			LOG.warn("Updating caching via " + jp.toShortString() + " aborted due to an error.", ex);
		}
		return retVal;
	}

	protected String getObjectId(final int keyIndex,
	                             final Object returnValue,
	                             final JoinPoint jp,
	                             final Method methodToCache) throws Exception {
		final Object keyObject = keyIndex == -1
									? validateReturnValueAsKeyObject(returnValue, methodToCache)
									: getKeyObject(keyIndex, jp, methodToCache);
		final Method keyMethod = getKeyMethod(keyObject);
		return generateObjectId(keyMethod, keyObject);
	}

	protected void validateAnnotation(final UpdateSingleCache annotation,
	                                  final Method method) {

		final Class annotationClass = UpdateSingleCache.class;
		validateAnnotationExists(annotation, annotationClass);
		validateAnnotationIndex(annotation.keyIndex(), true, annotationClass, method);
		validateAnnotationNamespace(annotation.namespace(), annotationClass, method);
		validateAnnotationExpiration(annotation.expiration(), annotationClass, method);
	}

}
