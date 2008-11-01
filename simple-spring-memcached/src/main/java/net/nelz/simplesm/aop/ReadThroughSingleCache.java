package net.nelz.simplesm.aop;

import net.nelz.simplesm.annotations.*;
import net.nelz.simplesm.exceptions.*;
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
public class ReadThroughSingleCache extends CacheBase {
	private static final Log LOG = LogFactory.getLog(ReadThroughSingleCache.class);

	@Pointcut("@annotation(net.nelz.simplesm.annotations.ReadThroughSingleCache)")
	public void getIndividual() {}

	@Around("getIndividual()")
	public Object cacheIndividual(final ProceedingJoinPoint pjp) throws Throwable {
		/*
		TODO: Cache disabling
		if (cache_is_disabled) {
			LOG.warn("Caching is disabled.");
			return pjp.proceed();
		}
		*/

		// This is injected caching.  If anything goes wrong in the caching, LOG the crap outta it,
		// but do not let it surface up past the AOP injection itself.
		final String cacheKey;
		final net.nelz.simplesm.annotations.ReadThroughSingleCache annotation;
		try {
			final Method methodToCache = getMethodToCache(pjp);
			annotation = methodToCache.getAnnotation(net.nelz.simplesm.annotations.ReadThroughSingleCache.class);
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

	protected Method getKeyMethod(final Object keyObject) throws NoSuchMethodException {
		final Method[] methods = keyObject.getClass().getDeclaredMethods();
		Method targetMethod = null;
		for (final Method method : methods) {
			if (method != null && method.getAnnotation(CacheKeyMethod.class) != null) {
				if (method.getParameterTypes().length > 0) {
					throw new InvalidAnnotationException(String.format(
							"Method [%s] must have 0 arguments to be annotated with [%s]",
							method.toString(),
							CacheKeyMethod.class.getName()));
				}
				if (!String.class.equals(method.getReturnType())) {
					throw new InvalidAnnotationException(String.format(
							"Method [%s] must return a String to be annotated with [%s]",
							method.toString(),
							CacheKeyMethod.class.getName()));
				}
				if (targetMethod != null) {
					throw new InvalidAnnotationException(String.format(
							"Class [%s] should have only one method annotated with [%s]. See [%s] and [%s]",
							keyObject.getClass().getName(),
							CacheKeyMethod.class.getName(),
							targetMethod.getName(),
							method.getName()));
				}
				targetMethod = method;
			}
		}

		if (targetMethod == null) {
			targetMethod = keyObject.getClass().getMethod("toString", null);
		}

		return targetMethod;
	}

	protected Object getKeyObject(final int keyIndex,
	                             final JoinPoint jp,
	                             final Method methodToCache) throws Exception {
		final Object[] args = jp.getArgs();
		if (args.length <= keyIndex) {
			throw new InvalidParameterException(String.format(
					"A key index of %s is too big for the number of arguments in [%s]",
					keyIndex,
					methodToCache.toString()));
		}
		final Object keyObject = args[keyIndex];
		if (keyObject == null) {
			throw new InvalidParameterException(String.format(
					"The argument passed into [%s] at index %s is null.",
					methodToCache.toString(),
					keyIndex));
		}
		return keyObject;
	}

	protected void validateAnnotation(final net.nelz.simplesm.annotations.ReadThroughSingleCache annotation, final Method method) {
		if (annotation == null) {
			throw new InvalidParameterException(String.format(
					"No annotation of type [%s] found.",
					net.nelz.simplesm.annotations.ReadThroughSingleCache.class.getName()
			));
		}
		if (annotation.keyIndex() < 0) {
			throw new InvalidParameterException(String.format(
					"KeyIndex for annotation [%s] must be 0 or greater on [%s]",
					annotation.getClass().getName(),
					method.toString()
			));
		}
		if (net.nelz.simplesm.annotations.ReadThroughSingleCache.DEFAULT_STRING.equals(annotation.namespace())
				|| annotation.namespace() == null
				|| annotation.namespace().length() < 1) {
			throw new InvalidParameterException(String.format(
					"Namespace for annotation [%s] must be defined on [%s]",
					annotation.getClass().getName(),
					method.toString()
			));
		}
		if (annotation.expiration() < 0) {
			throw new InvalidParameterException(String.format(
					"Expiration for annotation [%s] must be 0 or greater on [%s]",
					annotation.getClass().getName(),
					method.toString()
			));
		}
	}
}
