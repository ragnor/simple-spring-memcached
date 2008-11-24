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
@Aspect
public class ReadThroughMultiCacheAdvice extends CacheBase {
	private static final Log LOG = LogFactory.getLog(ReadThroughMultiCacheAdvice.class);

	@Pointcut("@annotation(net.nelz.simplesm.annotations.ReadThroughMultiCache)")
	public void getMulti() {}

	@Around("getMulti()")
	public Object cacheMulti(final ProceedingJoinPoint pjp) throws Throwable {
		// This is injected caching.  If anything goes wrong in the caching, LOG the crap outta it,
		// but do not let it surface up past the AOP injection itself.
		final MultiCacheCoordinator coord = new MultiCacheCoordinator();
		Object [] args = pjp.getArgs();
		try {
			// Get the target method being invoked, and make sure it returns the correct info.
			coord.setMethod(getMethodToCache(pjp));
			verifyReturnTypeIsList(coord.getMethod(), ReadThroughMultiCache.class);

			// Get the annotation associated with this method, and make sure the values are valid.
			coord.setAnnotation(coord.getMethod().getAnnotation(ReadThroughMultiCache.class));
			validateAnnotation(coord.getAnnotation(), coord.getMethod());

			// Get the list of objects that will provide the keys to all the cache values.
			coord.setKeyObjects(getKeyObjectList(coord.getAnnotation().keyIndex(), pjp, coord.getMethod()));

			// Create key->object and object->key mappings.
			coord.setHolder(convertIdObjectsToKeyMap(coord.getKeyObjects(), coord.getAnnotation().namespace()));

			// Get the full list of cache keys and ask the cache for the corresponding values.
			coord.setInitialKey2Result(cache.getBulk(coord.getKey2Obj().keySet()));

			// We've gotten all positive cache results back, so build up a results list and return it.
			if (coord.getMissObjects().size() < 1) {
				return coord.generateResultList();
			}

			// Create the new list of arguments with a subset of the key objects that aren't in the cache.
			args = coord.modifyArgumentList(args);
		} catch (Throwable ex) {
			LOG.warn("Caching on " + pjp.toShortString() + " aborted due to an error.", ex);
			return pjp.proceed();
		}

		/*
		Call the target method with the new subset of arguments.
		We are calling this outside of the try/catch block in case there are some
		'not our fault' problems with the target method. (Connection issues, etc...)
		Though, this decision could go either way, really.
		 */
		final List results = (List) pjp.proceed(args);

		try {

			if (results.size() != coord.getMissObjects().size()) {
				throw new RuntimeException("Did not receive a correlated amount of data from the target method.");
			}

			for (int ix = 0; ix < results.size(); ix++) {
				final Object keyObject = coord.getMissObjects().get(ix);
				final Object resultObject = results.get(ix) == null ? new PertinentNegativeNull() : results.get(ix);
				final String cacheKey = coord.obj2Key.get(keyObject);
				cache.set(cacheKey,
						coord.getAnnotation().expiration(),
						resultObject);
				coord.getKey2Result().put(cacheKey, resultObject);
			}

			return coord.generateResultList();
		} catch (Throwable ex) {
			LOG.warn("Caching on " + pjp.toShortString()
					+ " aborted due to an error. The underlying method will be called twice.", ex);
			return pjp.proceed();
		}
	}

	protected void validateAnnotation(final ReadThroughMultiCache annotation,
	                                  final Method method) {
		final Class annotationClass = ReadThroughMultiCache.class;
		validateAnnotationExists(annotation, annotationClass);
		validateAnnotationIndex(annotation.keyIndex(), false, annotationClass, method);
		validateAnnotationNamespace(annotation.namespace(), annotationClass, method);
		validateAnnotationExpiration(annotation.expiration(), annotationClass, method);
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

	protected List<Object> getKeyObjectList(final int keyIndex,
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

	static class MapHolder {
		final Map<String, Object> key2Obj = new HashMap<String, Object>();
		final Map<Object, String> obj2Key = new HashMap<Object, String>();

		public Map<String, Object> getKey2Obj() {
			return key2Obj;
		}

		public Map<Object, String> getObj2Key() {
			return obj2Key;
		}
	}

	static class MultiCacheCoordinator {
		private Method method;
		private ReadThroughMultiCache annotation;
		private List<Object> keyObjects = new ArrayList<Object>();
		private Map<String, Object> key2Obj = new HashMap<String, Object>();
		private Map<Object, String> obj2Key = new HashMap<Object, String>();
		private Map<String, Object> key2Result = new HashMap<String, Object>();
		private List<Object> missObjects = new ArrayList<Object>();

		public Method getMethod() {
			return method;
		}

		public void setMethod(Method method) {
			this.method = method;
		}

		public ReadThroughMultiCache getAnnotation() {
			return annotation;
		}

		public void setAnnotation(ReadThroughMultiCache annotation) {
			this.annotation = annotation;
		}

		public List<Object> getKeyObjects() {
			return keyObjects;
		}

		public void setKeyObjects(List<Object> keyObjects) {
			this.keyObjects.addAll(keyObjects);
		}

		public void setHolder(MapHolder holder) {
			key2Obj.putAll(holder.getKey2Obj());
			obj2Key.putAll(holder.getObj2Key());
		}

		public Map<String, Object> getKey2Obj() {
			return key2Obj;
		}

		public Map<Object, String> getObj2Key() {
			return obj2Key;
		}

		public Map<String, Object> getKey2Result() {
			return key2Result;
		}

		public void setInitialKey2Result(Map<String, Object> key2Result) {
			if (key2Result == null) {
				throw new RuntimeException("There was an error retrieving cache values.");
			}
			this.key2Result.putAll(key2Result);

			final Set<Object> missObjectSet = new HashSet<Object>();
			for (final String key : this.key2Obj.keySet()) {
				if (this.key2Result.get(key) == null) {
					missObjectSet.add(key2Obj.get(key));
				}
			}
			this.missObjects.addAll(missObjectSet);
		}

		public List<Object> generateResultList() {
			final List<Object> results = new ArrayList<Object>();
			for (int ix = 0; ix < keyObjects.size(); ix++) {
				final Object keyObject = keyObjects.get(ix);
				final String cacheKey = obj2Key.get(keyObject);
				final Object keyResult = key2Result.get(cacheKey);
				if (keyResult == null) {
					throw new RuntimeException(String.format(
							"Unable to fulfill data for the key item [%s] with key value of [%s].",
							keyObject.toString(),
							obj2Key.get(keyObject)));
				}
				results.add(keyResult instanceof PertinentNegativeNull ? null : keyResult);
			}

			return results;
		}

		public List<Object> getMissObjects() {
			return missObjects;
		}

		public Object[] modifyArgumentList(final Object[] args) {
			args[annotation.keyIndex()] = this.missObjects;
			return args;
		}
	}
}
