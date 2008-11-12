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
public class ReadThroughMultiCacheAdvice extends CacheBase {
	private static final Log LOG = LogFactory.getLog(ReadThroughMultiCacheAdvice.class);

	@Pointcut("@annotation(net.nelz.simplesm.annotations.ReadThroughMultiCache)")
	public void getIndividual() {}

	// TODO: This code is uuuuUUUuuugly! It needs a nice round of refactoring.
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
			final MapHolder holder = convertIdObjectsToKeyMap(idObjects, annotation.namespace());
			final List<String> keys = new ArrayList<String>(holder.getKey2Obj().keySet());
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

			// We've gotten all positive cache results back, so build up a results list and return it.
			if (missKeys.size() < 1) {
				final List<Object> results = new ArrayList<Object>();
				for (int ix = 0; ix < idObjects.size(); ix++) {
					final Object obj = idObjects.get(ix);
					final String cacheKey = holder.obj2Key.get(obj);
					if (cacheKey == null) {
						throw new RuntimeException("Problem assembling result set.");
					}
					final Object result = key2Result.get(obj);
					results.set(ix, result instanceof PertinentNegativeNull ? null : result);
				}
				return results;
			}

			final List<Object> idObjectSubset = new ArrayList<Object>(missKeys.size());
			for (final String missKey : missKeys) {
				final Object obj = holder.getKey2Obj().get(missKey);
				idObjectSubset.add(obj);
			}

			final Object [] args = pjp.getArgs();
			args[annotation.keyIndex()] = idObjectSubset;

			// TODO: Refactor this to be outside the try/catch block to allow any data problems to bubble to user
			final List results = (List) pjp.proceed(args);

			if (results.size() != missKeys.size()) {
				throw new RuntimeException("Problem retrieving data from pass-through");
			}

			final Map<String, Object> miss2Result = new HashMap<String, Object>();
			for (int ix = 0; ix < results.size(); ix++) {
				final String cacheKey = missKeys.get(ix);
				final Object cacheResult = results.get(ix);
				cache.set(cacheKey,
						annotation.expiration(),
						cacheResult == null ? new PertinentNegativeNull() : cacheResult);
				miss2Result.put(cacheKey, cacheResult);
			}

			// Now that we've gotten all the values	assemble them for return to the caller.
			final List<Object> resultObjects = new ArrayList<Object>();
			for (int ix = 0; ix <= idObjects.size(); ix++) {
				final Object idObject = idObjects.get(ix);
				final String cacheKey = holder.getObj2Key().get(idObject);
				Object resultObject = key2Result.get(cacheKey);
				if (resultObject == null) {
					resultObject = miss2Result.get(cacheKey);
				}
				resultObjects.set(ix, resultObject instanceof PertinentNegativeNull ? null : resultObject);
			}

			return resultObjects;
		} catch (Throwable ex) {
			LOG.warn("Caching on " + pjp.toShortString() + " aborted due to an error.", ex);
			return pjp.proceed();
		}
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

	protected MapHolder convertIdObjectsToKeyMap(final List<Object> idObjects,
	                                              final String namespace)
			throws Exception {
		final MapHolder holder = new MapHolder();
		for (final Object obj : idObjects) {
			if (obj == null) {
				throw new InvalidParameterException("One of the passed in key objects is null");
			}

			final Method method = getKeyMethod(obj);
			final String cacheKey = buildCacheKey(generateObjectId(method, obj), namespace);
			if (holder.getObj2Key().get(obj) == null) {
				holder.getObj2Key().put(obj, cacheKey);
			}
			if (holder.getKey2Obj().get(cacheKey) == null) {
				holder.getKey2Obj().put(cacheKey, obj);
			}
		}

		return holder;
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

	private static class MapHolder {
		final Map<String, Object> key2Obj = new HashMap<String, Object>();
		final Map<Object, String> obj2Key = new HashMap<Object, String>();

		public Map<String, Object> getKey2Obj() {
			return key2Obj;
		}

		public Map<Object, String> getObj2Key() {
			return obj2Key;
		}
	}
}
