package net.nelz.simplesm.aop;

import net.nelz.simplesm.annotations.*;
import org.apache.commons.logging.*;
import org.aspectj.lang.*;
import org.aspectj.lang.annotation.*;

import java.lang.reflect.*;
import java.security.*;

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
public class ReadThroughSingleCacheAdvice extends CacheBase {
	private static final Log LOG = LogFactory.getLog(ReadThroughSingleCacheAdvice.class);

	@Pointcut("@annotation(net.nelz.simplesm.annotations.ReadThroughSingleCache)")
	public void getIndividual() {}

	@Around("getIndividual()")
	public Object cacheIndividual(final ProceedingJoinPoint pjp) throws Throwable {
		// This is injected caching.  If anything goes wrong in the caching, LOG the crap outta it,
		// but do not let it surface up past the AOP injection itself.
		final String cacheKey;
		final ReadThroughSingleCache annotation;
		try {
			final Method methodToCache = getMethodToCache(pjp);
			annotation = methodToCache.getAnnotation(ReadThroughSingleCache.class);
			validateAnnotation(annotation, methodToCache);
			final String objectId = getObjectId(annotation.keyIndex(), pjp, methodToCache);
			cacheKey = buildCacheKey(objectId, annotation.namespace());
			final Object result = cache.get(cacheKey);
			if (result != null) {
				LOG.debug("Cache hit.");
				return (result instanceof PertinentNegativeNull) ? null : result;
			}
		} catch (Throwable ex) {
			LOG.warn("Caching on " + pjp.toShortString() + " aborted due to an error.", ex);
			return pjp.proceed();
		}

		final Object result = pjp.proceed();

		// This is injected caching.  If anything goes wrong in the caching, LOG the crap outta it,
		// but do not let it surface up past the AOP injection itself.
		try {
			final Object submission = (result == null) ? new PertinentNegativeNull() : result;
			cache.set(cacheKey, annotation.expiration(), submission);
		} catch (Throwable ex) {
			LOG.warn("Caching on " + pjp.toShortString() + " aborted due to an error.", ex);
		}
		return result;
	}

	protected String getObjectId(final int keyIndex,
	                             final JoinPoint jp,
	                             final Method methodToCache) throws Exception {
		final Object keyObject = getKeyObject(keyIndex, jp, methodToCache);
		final Method keyMethod = getKeyMethod(keyObject);
		return generateObjectId(keyMethod, keyObject);
	}

	protected String generateObjectId(final Method keyMethod, final Object keyObject) throws Exception {
		final String objectId = (String) keyMethod.invoke(keyObject, null);
		if (objectId == null || objectId.length() < 1) {
			throw new RuntimeException("Got an empty key value from " + keyMethod.getName());
		}
		return objectId;
	}

	protected void validateAnnotation(final ReadThroughSingleCache annotation,
	                                  final Method method) {
		if (annotation == null) {
			throw new InvalidParameterException(String.format(
					"No annotation of type [%s] found.",
					ReadThroughSingleCache.class.getName()
			));
		}
		if (annotation.keyIndex() < 0) {
			throw new InvalidParameterException(String.format(
					"KeyIndex for annotation [%s] must be 0 or greater on [%s]",
					ReadThroughSingleCache.class.getName(),
					method.toString()
			));
		}
		if (AnnotationConstants.DEFAULT_STRING.equals(annotation.namespace())
				|| annotation.namespace() == null
				|| annotation.namespace().length() < 1) {
			throw new InvalidParameterException(String.format(
					"Namespace for annotation [%s] must be defined on [%s]",
					ReadThroughSingleCache.class.getName(),
					method.toString()
			));
		}
		if (annotation.expiration() < 0) {
			throw new InvalidParameterException(String.format(
					"Expiration for annotation [%s] must be 0 or greater on [%s]",
					ReadThroughSingleCache.class.getName(),
					method.toString()
			));
		}
	}
}
