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
			verifyReturnTypeIsList(methodToCache);
			final List<Object> idObjects = verifyKeyIndexIsList(annotation.keyIndex(), pjp, methodToCache);
			final Map<Object, String> obj2Key = convertIdObjectsToKeyMap(idObjects, annotation.namespace());
			final List<String> keys = new ArrayList<String>(obj2Key.values());
			final Map<String, Object> key2Result = cache.getBulk(keys);
			if (key2Result == null) {
				throw new RuntimeException("There was an error retrieving cache values.");
			}
			final List<String> missKeys = new ArrayList<String>();
			for (final Map.Entry<String, Object> entry : key2Result.entrySet()) {
				if (entry.getValue() == null) {
					missKeys.add(entry.getKey());
				}
			}

			nooch
			/*
			HAVING TROUBLE FIGURING OUT ALL THE NEEDED MAPS...  obj2key, key2obj, key2result, obj2result, etc.
			 */

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

	protected Map<Object, String> convertIdObjectsToKeyMap(final List<Object> idObjects,
	                                              final String namespace)
			throws Exception {
		final Map<Object, String> results = new HashMap<Object, String>(idObjects.size());
		for (final Object obj : idObjects) {
			if (obj == null) {
				throw new InvalidParameterException("One of the passed in key objects is null");
			}

			if (results.get(obj) == null) {
				final Method method = getKeyMethod(obj);
				final String cacheKey = buildCacheKey(generateObjectId(method, obj), namespace);
				results.put(obj, cacheKey);
			}
		}

		return results;
	}

	protected void verifyReturnTypeIsList(final Method method) {
		if (verifyTypeIsList(method.getReturnType())) { return; }
		throw new InvalidAnnotationException(String.format(
				"The annotation [%s] is only valid on a method that returns a [%s]. " +
				"[%s] does not fulfill this requirement.",
				ReadThroughMultiCache.class.getName(),
				List.class.getName(),
				method.toString()
		));
	}

	protected List<Object> verifyKeyIndexIsList(final int keyIndex,
	                                         final JoinPoint jp,
	                                         final Method method) throws Exception {
		final Object keyObjects = getKeyObject(keyIndex, jp, method);
		if (verifyTypeIsList(keyObjects.getClass())) { return (List<Object>) keyObjects;}
		throw new InvalidAnnotationException(String.format(
				"The parameter object found at keyIndex [%s] is not a [%s]. " +
				"[%s] does not fulfill the requirements.",
				ReadThroughMultiCache.class.getName(),
				List.class.getName(),
				method.toString()
		));
	}

	protected boolean verifyTypeIsList(final Class clazz) {
		if (List.class.equals(clazz)) { return true; }
		final Type[] types = clazz.getGenericInterfaces();
		if (types != null) {
			for (final Type type : types) {
				if (type != null) {
					if (type instanceof ParameterizedType) {
						final ParameterizedType ptype = (ParameterizedType) type;
						if (List.class.equals(ptype.getRawType())) { return true; }
					} else {
						if (List.class.equals(type)) { return true; }
					}
				}
			}
		}

		return false;
	}
}
