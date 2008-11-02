package net.nelz.simplesm.aop;

import net.nelz.simplesm.annotations.*;
import net.nelz.simplesm.exceptions.*;
import org.apache.commons.logging.*;
import org.aspectj.lang.*;
import org.aspectj.lang.annotation.*;

import java.lang.reflect.*;
import java.security.*;
import java.util.*;

/**
 * Copyright 2008 Widgetbox, Inc.
 * All rights reserved.
 * THIS PROGRAM IS CONFIDENTIAL AND AN UNPUBLISHED WORK AND TRADE
 * SECRET OF THE COPYRIGHT HOLDER, AND DISTRIBUTED ONLY UNDER RESTRICTION.
 */
public class ReadThroughMultiCacheAdvice extends CacheBase {
	private static final Log LOG = LogFactory.getLog(ReadThroughMultiCacheAdvice.class);

	@Pointcut("@annotation(net.nelz.simplesm.annotations.ReadThroughMultiCache)")
	public void getIndividual() {}

	@Around("getIndividual()")
	public Object cacheIndividual(final ProceedingJoinPoint pjp) throws Throwable {
		// This is injected caching.  If anything goes wrong in the caching, LOG the crap outta it,
		// but do not let it surface up past the AOP injection itself.
		try {
			final Method methodToCache = getMethodToCache(pjp);
			final ReadThroughMultiCache annotation = methodToCache.getAnnotation(ReadThroughMultiCache.class);
			validateAnnotation(annotation, methodToCache);
			final List<String> cacheKeys = generateCacheKeys(annotation.keyIndex(), pjp, methodToCache);


		} catch (Throwable ex) {
			LOG.warn("Caching on " + pjp.toShortString() + " aborted due to an error.", ex);
			return pjp.proceed();
		}
		return null;
	}

	protected void validateAnnotation(final ReadThroughMultiCache annotation,
	                                  final Method method) {
		if (annotation == null) {
			throw new InvalidParameterException(String.format(
					"No annotation of type [%s] found.",
					ReadThroughMultiCache.class.getName()
			));
		}
		if (annotation.keyIndex() < 0) {
			throw new InvalidParameterException(String.format(
					"KeyIndex for annotation [%s] must be 0 or greater on [%s]",
					ReadThroughMultiCache.class.getName(),
					method.toString()
			));
		}
		if (AnnotationConstants.DEFAULT_STRING.equals(annotation.namespace())
				|| annotation.namespace() == null
				|| annotation.namespace().length() < 1) {
			throw new InvalidParameterException(String.format(
					"Namespace for annotation [%s] must be defined on [%s]",
					ReadThroughMultiCache.class.getName(),
					method.toString()
			));
		}
		if (annotation.expiration() < 0) {
			throw new InvalidParameterException(String.format(
					"Expiration for annotation [%s] must be 0 or greater on [%s]",
					ReadThroughMultiCache.class.getName(),
					method.toString()
			));
		}
	}

	protected List<String> generateCacheKeys(final int keyIndex,
	                                         final JoinPoint jp,
	                                         final Method methodToCache) throws Exception {

		return null;
	}

	protected void verifyReturnType(final Method method) {
		final Class returnType = method.getReturnType();
		if (List.class.equals(returnType)) { return; }
		final Type[] types = returnType.getGenericInterfaces();
		if (types != null) {
			for (final Type type : types) {
				if (List.class.equals(type)) { return; }
			}
		}
		throw new InvalidAnnotationException(String.format(
				"The annotation [%s] is only valid on a method that returns a [%s]. " +
				"[%s] does not fulfill this requirement.",
				ReadThroughMultiCache.class.getName(),
				List.class.getName(),
				method.toString()
		));
	}
}
