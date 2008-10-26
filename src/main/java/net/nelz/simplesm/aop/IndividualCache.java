package net.nelz.simplesm.aop;

import net.spy.memcached.*;
import org.apache.commons.logging.*;
import org.aspectj.lang.*;
import org.aspectj.lang.annotation.*;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.*;
import org.springframework.stereotype.*;

import java.lang.annotation.*;
import java.lang.reflect.*;

/**
 * Copyright 2008 Widgetbox, Inc.
 * All rights reserved.
 * THIS PROGRAM IS CONFIDENTIAL AND AN UNPUBLISHED WORK AND TRADE
 * SECRET OF THE COPYRIGHT HOLDER, AND DISTRIBUTED ONLY UNDER RESTRICTION.
 */
@Aspect
@Component("individualCache")
public class IndividualCache {
	private static final Log LOG = LogFactory.getLog(IndividualCache.class);

	private MemcachedClientIF cache;

	public void setCache(MemcachedClientIF cache) {
		this.cache = cache;
	}

	@Pointcut("@annotation(net.nelz.simplesm.annotations.SSMIndividual)")
	public void getIndividual() {}

	@Around("getIndividual()")
	public Object cacheIndividual(final ProceedingJoinPoint pjp) /* throws Throwable*/ {
//		if (cache_is_disabled) {
//			LOG.warn("Caching is disabled.");
//			return pjp.proceed();
//		}




		return null;
	}

	protected <T> T getMethodAnnotation(final JoinPoint jp) {
		final Signature sig = jp.getSignature();
		if (!(sig instanceof MethodSignature)) {
			throw new RuntimeException("This annotation is only valid on a method.");
		}
		final Method method = ((MethodSignature)sig).getMethod();
		final Annotation[] annotations = method.getDeclaredAnnotations();
		T result = null;
		if (annotations != null) {
			for (final Annotation annotation : annotations) {
				if (annotation != null) {
					if (annotation.annotationType().equals(T))

					if (annotation instanceof T.class) {
						result = (T) annotation;
					}
				}
			}
		}
		return result;
	}


}
