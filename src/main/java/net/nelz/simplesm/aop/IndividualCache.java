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
			final SSMIndividual annotation = getMethodAnnotation(SSMIndividual.class, pjp);
			validateAnnotation(annotation, pjp);

			final String cacheKey = getCacheKey(annotation.keyIndex(), pjp);
		} catch (Throwable ex) {
			LOG.warn("Caching on " + pjp.toShortString() + " aborted due to an error.", ex);
			return pjp.proceed();
		}

		return null;
	}

	protected String getCacheKey(final int keyIndex, final JoinPoint jp) throws Exception {
		String objectKey = "";

		final Object keyObject = jp.getArgs()[keyIndex];
		if (keyObject == null) {
			throw new InvalidParameterException("The argument at index " + keyIndex + " is null.");
		}

		final Method[] methods = keyObject.getClass().getDeclaredMethods();
		Method targetMethod = null;
		for (final Method method : methods) {
			if (method.getAnnotation(SSMCacheKeyMethod.class) != null) {
				// TODO: Add description of what class we're talking about? (keyObject's class)
				if (method.getParameterTypes().length > 0) {
					throw new InvalidAnnotationException("A method must have 0 arguments to be annotated with "
							+ SSMCacheKeyMethod.class.getName());
				}
				if (!String.class.equals(method.getReturnType())) {
					throw new InvalidAnnotationException("A method must return a String to be annotated with "
							+ SSMCacheKeyMethod.class.getName());
				}
				if (targetMethod != null) {
					throw new InvalidAnnotationException("A class must have only one annotation of  "
							+ SSMCacheKeyMethod.class.getName());
				}
				targetMethod = method;
			}
		}

		if (targetMethod == null) {
			targetMethod = keyObject.getClass().getMethod("toString", null);
		}

		objectKey = (String) targetMethod.invoke(keyObject, null);
		if (objectKey == null || objectKey.length() > 1) {
			throw new RuntimeException("Got an empty key value from " + targetMethod.getName());
		}

		return null;
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
