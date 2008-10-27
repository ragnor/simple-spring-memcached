package net.nelz.simplesm.aop;

import net.nelz.simplesm.exceptions.*;
import net.spy.memcached.*;
import org.aspectj.lang.*;
import org.aspectj.lang.reflect.*;

import java.lang.reflect.*;

/**
 * Copyright 2008 Widgetbox, Inc.
 * All rights reserved.
 * THIS PROGRAM IS CONFIDENTIAL AND AN UNPUBLISHED WORK AND TRADE
 * SECRET OF THE COPYRIGHT HOLDER, AND DISTRIBUTED ONLY UNDER RESTRICTION.
 */
public class CacheBase {
	protected MemcachedClientIF cache;

	public void setCache(MemcachedClientIF cache) {
		this.cache = cache;
	}

	protected Method getMethodToCache(final JoinPoint jp) {
		final Signature sig = jp.getSignature();
		if (!(sig instanceof MethodSignature)) {
			throw new InvalidAnnotationException("This annotation is only valid on a method.");
		}
		return((MethodSignature)sig).getMethod();
	}

}
