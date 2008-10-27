package net.nelz.simplesm.aop;

import net.nelz.simplesm.annotations.*;
import net.nelz.simplesm.exceptions.*;
import org.apache.commons.logging.*;
import org.aspectj.lang.*;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.*;

import java.lang.reflect.*;
import java.security.*;

/**
 * Copyright 2008 Widgetbox, Inc.
 * All rights reserved.
 * THIS PROGRAM IS CONFIDENTIAL AND AN UNPUBLISHED WORK AND TRADE
 * SECRET OF THE COPYRIGHT HOLDER, AND DISTRIBUTED ONLY UNDER RESTRICTION.
 */
@Aspect
@Component("individualCache")
public class IndividualCache extends CacheBase {
	private static final Log LOG = LogFactory.getLog(IndividualCache.class);


	@Pointcut("@annotation(net.nelz.simplesm.annotations.SSMIndividual)")
	public void getIndividual() {}

	@Around("getIndividual()")
	public Object cacheIndividual(final ProceedingJoinPoint pjp) throws Throwable {
// TODO: Cache disabling
//		if (cache_is_disabled) {
//			LOG.warn("Caching is disabled.");
//			return pjp.proceed();
//		}
		// This is injected caching.  If anything goes wrong in the caching, LOG the crap outta it,
		// but do not let it surface up past the AOP injection itself.
		try {
			final Method methodToCache = getMethodToCache(pjp);
			final SSMIndividual annotation = methodToCache.getAnnotation(SSMIndividual.class);
			validateAnnotation(annotation, pjp);

			final String cacheKey = getCacheKey(annotation.keyIndex(), pjp, methodToCache);
		} catch (Throwable ex) {
			LOG.warn("Caching on " + pjp.toShortString() + " aborted due to an error.", ex);
			return pjp.proceed();
		}

		return null;
	}

	protected String getCacheKey(final int keyIndex,
	                             final JoinPoint jp,
	                             final Method methodToCache) throws Exception {
		final Object keyObject = getKeyObject(keyIndex, jp, methodToCache);
		final Method keyMethod = getKeyMethod(keyObject);
		return generateCacheKey(keyMethod, keyObject);
	}

	protected String generateCacheKey(final Method keyMethod, final Object keyObject) throws Exception {
		final String cacheKey = (String) keyMethod.invoke(keyObject, null);
		if (cacheKey == null || cacheKey.length() < 1) {
			throw new RuntimeException("Got an empty key value from " + keyMethod.getName());
		}
		return cacheKey;
	}

	protected Method getKeyMethod(final Object keyObject) throws NoSuchMethodException {
		final Method[] methods = keyObject.getClass().getDeclaredMethods();
		Method targetMethod = null;
		for (final Method method : methods) {
			if (method != null && method.getAnnotation(SSMCacheKeyMethod.class) != null) {
				if (method.getParameterTypes().length > 0) {
					throw new InvalidAnnotationException(String.format(
							"Method [%s] must have 0 arguments to be annotated with [%s]",
							method.toString(),
							SSMCacheKeyMethod.class.getName()));
				}
				if (!String.class.equals(method.getReturnType())) {
					throw new InvalidAnnotationException(String.format(
							"Method [%s] must return a String to be annotated with [%s]",
							method.toString(),
							SSMCacheKeyMethod.class.getName()));
				}
				if (targetMethod != null) {
					throw new InvalidAnnotationException(String.format(
							"Class [%s] should have only one method annotated with [%s]. See [%s] and [%s]",
							keyObject.getClass().getName(),
							SSMCacheKeyMethod.class.getName(),
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

	protected void validateAnnotation(final SSMIndividual annotation, final JoinPoint jp) {
		if (annotation.keyIndex() < 0) {
			throw new InvalidParameterException(String.format(
					"KeyIndex for annotation %s must be 0 or greater on %s",
					annotation.getClass().getName(),
					jp.toLongString()
			));
		}
		if (SSMIndividual.DEFAULT_STRING.equals(annotation.namespace())
				|| annotation.namespace() == null
				|| annotation.namespace().length() < 1) {
			throw new InvalidParameterException(String.format(
					"Namespace for annotation %s must be defined on %s",
					annotation.getClass().getName(),
					jp.toLongString()
			));
		}
	}
}
