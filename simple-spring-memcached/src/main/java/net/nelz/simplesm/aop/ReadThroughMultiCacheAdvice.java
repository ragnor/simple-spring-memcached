package net.nelz.simplesm.aop;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.nelz.simplesm.api.ReadThroughMultiCache;
import net.nelz.simplesm.api.ReadThroughMultiCacheOptions;
import net.nelz.simplesm.exceptions.InvalidAnnotationException;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Copyright (c) 2008, 2009 Nelson Carpentier
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the
 * Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 * 
 * @author Nelson Carpentier, Jakub Białek
 * 
 */
@Aspect
public class ReadThroughMultiCacheAdvice extends CacheBase {
    private static final Logger LOG = LoggerFactory.getLogger(ReadThroughMultiCacheAdvice.class);

    private static final Integer[] INTEGER_ARRAY = new Integer[0];

    @Pointcut("@annotation(net.nelz.simplesm.api.ReadThroughMultiCache)")
    public void getMulti() {
    }

    @Around("getMulti()")
    @SuppressWarnings("unchecked")
    public Object cacheMulti(final ProceedingJoinPoint pjp) throws Throwable {
        // This is injected caching. If anything goes wrong in the caching, LOG
        // the crap outta it, but do not let it surface up past the AOP injection itself.
        final MultiCacheCoordinator coord = new MultiCacheCoordinator();
        Class<?> jsonClass = null;

        Object[] args = pjp.getArgs();
        try {
            // Get the target method being invoked, and make sure it returns the correct info.
            coord.setMethod(getMethodToCache(pjp));
            verifyReturnTypeIsList(coord.getMethod(), ReadThroughMultiCache.class);

            // Get the annotation associated with this method, and make sure the values are valid.
            final ReadThroughMultiCache annotation = coord.getMethod().getAnnotation(ReadThroughMultiCache.class);
            jsonClass = getJsonClass(coord.getMethod(), -1);

            coord.setAnnotationData(AnnotationDataBuilder.buildAnnotationData(annotation, ReadThroughMultiCache.class, coord.getMethod()));
            ReadThroughMultiCacheOptions options = annotation.options();
          
            coord.setGenerateKeysFromResult(options.generateKeysFromResult());
            coord.setAddNullsToCache(options.addNullsToCache());
            coord.setSkipNullsInResult(options.skipNullsInResult());

            // Get the list of objects that will provide the keys to all the cache values.
            coord.setKeyObjects(getKeyObjects(coord.getAnnotationData().getKeysIndex(), pjp, coord.getMethod(), coord, annotation));

            // Create key->object and object->key mappings.
            coord.setHolder(convertIdObjectsToKeyMap(coord.getListObjects(), coord.getKeyObjects(), coord.getListIndexInKeys(),
                    coord.getAnnotationData()));

            if (options.readDirectlyFromDB()) {
                coord.setInitialKey2Result(Collections.EMPTY_MAP);
            } else {
                // Get the full list of cache keys and ask the cache for the corresponding values.
                coord.setInitialKey2Result(getBulk(coord.getKey2Obj().keySet(), jsonClass));
            }

            // We've gotten all positive cache results back, so build up a results list and return it.
            if (coord.getMissObjects().size() < 1) {
                return coord.generateResultList();
            }

            // Create the new list of arguments with a subset of the key objects that aren't in the cache.
            args = coord.modifyArgumentList(args);
        } catch (Throwable ex) {
            warn("Caching on " + pjp.toShortString() + " aborted due to an error.", ex);
            return pjp.proceed();
        }

        /*
         * Call the target method with the new subset of arguments. We are calling this outside of the try/catch block
         * in case there are some 'not our fault' problems with the target method. (Connection issues, etc...) Though,
         * this decision could go either way, really.
         */
        final List<Object> results = (List<Object>) pjp.proceed(args);

        try {
            // there are no results
            if (results == null || results.isEmpty()) {
                if (coord.isAddNullsToCache()) {
                    addNullsValuesForMissedObjects(coord.getMissObjects(), coord, jsonClass);
                }
                return coord.generatePartialResultList();
            }

            if (coord.isGenerateKeysFromResult()) {
                return generateByKeysFromResult(results, coord, jsonClass);
            } else {
                return generateByKeysProviders(results, coord, jsonClass);
            }
        } catch (Throwable ex) {
            warn("Caching on " + pjp.toShortString() + " aborted due to an error. The underlying method will be called twice.", ex);
            return pjp.proceed();
        }
    }

    private List<?> generateByKeysFromResult(List<Object> results, MultiCacheCoordinator coord, Class<?> jsonClass) {
        if (results == null) {
            results = new ArrayList<Object>();
        } else if (!results.isEmpty()) {
            List<String> keys = defaultKeyProvider.generateKeys(results);
            String cacheKey;

            int ix = 0;
            for (Object resultObject : results) {
                cacheKey = buildCacheKey(keys.get(ix), coord.getAnnotationData());
                setSilently(cacheKey, coord.getAnnotationData().getExpiration(), resultObject, jsonClass);
                coord.getMissObjects().remove(coord.getKey2Obj().get(cacheKey));
                ix++;
            }
        }

        if (coord.isAddNullsToCache()) {
            addNullsValuesForMissedObjects(coord.getMissObjects(), coord, jsonClass);
        }

        results.addAll(coord.generatePartialResultList());
        return results;
    }

    private List<?> generateByKeysProviders(List<Object> results, MultiCacheCoordinator coord, Class<?> jsonClass) {
        if (results.size() != coord.getMissObjects().size()) {
            getLogger().info("Did not receive a correlated amount of data from the target method.");
            results.addAll(coord.generatePartialResultList());
            return results;
        }

        int ix = 0;
        for (Object resultObject : results) {
            if (resultObject == null) {
                resultObject = PertinentNegativeNull.NULL;
            }
            Object keyObject = coord.getMissObjects().get(ix);
            String cacheKey = coord.getObj2Key().get(keyObject);
            setSilently(cacheKey, coord.getAnnotationData().getExpiration(), resultObject, jsonClass);
            coord.getKey2Result().put(cacheKey, resultObject);
            ix++;
        }

        return coord.generateResultList();
    }

    @Override
    protected Logger getLogger() {
        return LOG;
    }

    private void addNullsValuesForMissedObjects(List<Object> missObjects, MultiCacheCoordinator coord, Class<?> jsonClass) {
        for (Object keyObject : missObjects) {
            addSilently(coord.getObj2Key().get(keyObject), coord.getAnnotationData().getExpiration(), PertinentNegativeNull.NULL, jsonClass);
        }
    }

    @SuppressWarnings("unchecked")
    private Object[] getKeyObjects(final Collection<Integer> keysIndex, final ProceedingJoinPoint pjp, final Method method,
            MultiCacheCoordinator coord, Annotation annotation) throws Exception {
        Object[] results = getIndexObjects(keysIndex, pjp, method);
        Integer[] keyIndexArray = keysIndex.toArray(INTEGER_ARRAY);

        boolean listOccured = false;
        for (int i = 0; i < results.length; i++) {
            if (verifyTypeIsList(results[i].getClass())) {
                if (listOccured) {
                    throw new InvalidAnnotationException(
                            "There are more than one parameter annotated by @ParameterValueKeyProvider that are list in method "
                                    + method.toString());
                }
                listOccured = true;
                coord.setListIndexInKeys(i);
                coord.setListIndexInMethodArgs(keyIndexArray[i]);
                coord.setListObjects((List<Object>) results[i]);
            }
        }

        if (listOccured) {
            return results;
        }

        throw new InvalidAnnotationException(String.format("The parameter object found at dataIndex [%s] is not a [%s]. "
                + "[%s] does not fulfill the requirements.", annotation.getClass().getName(), List.class.getName(), method.toString()));
    }

    MapHolder convertIdObjectsToKeyMap(final List<Object> idObjects, final Object[] keys, final int listIndex, final AnnotationData data)
            throws Exception {
        final MapHolder holder = new MapHolder();

        // initialize method that will be used to generate keys from not collection method's arguments
        final Method[] methods = new Method[keys.length];
        for (int i = 0; i < keys.length; i++) {
            if (i != listIndex) {
                methods[i] = getKeyMethod(keys[i]);
            }
        }

        for (final Object obj : idObjects) {
            if (obj == null) {
                throw new InvalidParameterException("One of the passed in key objects is null");
            }

            keys[listIndex] = obj;
            methods[listIndex] = getKeyMethod(keys[listIndex]);
            String cacheKey = buildCacheKey(generateObjectIds(methods, keys), data);
            if (holder.getObj2Key().get(obj) == null) {
                holder.getObj2Key().put(obj, cacheKey);
            }
            if (holder.getKey2Obj().get(cacheKey) == null) {
                holder.getKey2Obj().put(cacheKey, obj);
            }
        }

        return holder;
    }

    static class MapHolder {
        final Map<String, Object> key2Obj = new LinkedHashMap<String, Object>();
        final Map<Object, String> obj2Key = new LinkedHashMap<Object, String>();

        public Map<String, Object> getKey2Obj() {
            return key2Obj;
        }

        public Map<Object, String> getObj2Key() {
            return obj2Key;
        }
    }

    static class MultiCacheCoordinator {
        private Method method;
        private AnnotationData annotationData;
        private Object[] keyObjects = new Object[0];
        private int listIndexInKeys = -1;
        private int listIndexInMethodArgs = -1;
        private List<Object> listObjects = new ArrayList<Object>();
        private Map<String, Object> key2Obj = new LinkedHashMap<String, Object>();
        private Map<Object, String> obj2Key = new LinkedHashMap<Object, String>();
        private Map<String, Object> key2Result = new HashMap<String, Object>();
        private List<Object> missObjects = new ArrayList<Object>();
        private boolean addNullsToCache;
        private boolean generateKeysFromResult;
        private boolean skipNullsInResult;

        public Method getMethod() {
            return method;
        }

        public boolean isAddNullsToCache() {
            return addNullsToCache;
        }

        public void setAddNullsToCache(boolean addNullsToCache) {
            this.addNullsToCache = addNullsToCache;
        }

        public void setGenerateKeysFromResult(boolean generateKeysFromResult) {
            this.generateKeysFromResult = generateKeysFromResult;
        }

        public boolean isGenerateKeysFromResult() {
            return generateKeysFromResult;
        }

        public void setListIndexInKeys(int listIndexInKeys) {
            this.listIndexInKeys = listIndexInKeys;
        }

        public int getListIndexInKeys() {
            return listIndexInKeys;
        }

        public void setMethod(Method method) {
            this.method = method;
        }

        public AnnotationData getAnnotationData() {
            return annotationData;
        }

        public void setAnnotationData(AnnotationData annotationData) {
            this.annotationData = annotationData;
        }

        public Object[] getKeyObjects() {
            return keyObjects;
        }

        public void setKeyObjects(Object[] keyObjects) {
            this.keyObjects = keyObjects;
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

            final Set<Object> missObjectSet = new LinkedHashSet<Object>();
            for (final String key : this.key2Obj.keySet()) {
                if (this.key2Result.get(key) == null) {
                    missObjectSet.add(key2Obj.get(key));
                }
            }
            this.missObjects.addAll(missObjectSet);
        }

        public List<Object> generateResultList() {
            final List<Object> results = new ArrayList<Object>();
            for (Object keyObject : listObjects) {
                final String cacheKey = obj2Key.get(keyObject);
                final Object keyResult = key2Result.get(cacheKey);
                if (keyResult == null) {
                    throw new RuntimeException(String.format("Unable to fulfill data for the key item [%s] with key value of [%s].",
                            keyObject.toString(), obj2Key.get(keyObject)));
                }

                if (!isSkipNullsInResult() || !(keyResult instanceof PertinentNegativeNull)) {
                    results.add(keyResult instanceof PertinentNegativeNull ? null : keyResult);
                }
            }

            return results;
        }

        public List<Object> generatePartialResultList() {
            final List<Object> results = new ArrayList<Object>();
            for (Object keyObject : listObjects) {
                final String cacheKey = obj2Key.get(keyObject);
                final Object keyResult = key2Result.get(cacheKey);
                if (keyResult != null && (!isSkipNullsInResult() || !(keyResult instanceof PertinentNegativeNull))) {
                    results.add(keyResult instanceof PertinentNegativeNull ? null : keyResult);
                }
            }

            return results;
        }

        public List<Object> getMissObjects() {
            return missObjects;
        }

        public Object[] modifyArgumentList(final Object[] args) {
            args[listIndexInMethodArgs] = this.missObjects;
            return args;
        }

        public void setListObjects(List<Object> listObjects) {
            this.listObjects = listObjects;
        }

        public List<Object> getListObjects() {
            return listObjects;
        }

        public void setListIndexInMethodArgs(int listIndexInMethodArgs) {
            this.listIndexInMethodArgs = listIndexInMethodArgs;
        }

        public void setSkipNullsInResult(boolean skipNullsInResult) {
            this.skipNullsInResult = skipNullsInResult;
        }

        public boolean isSkipNullsInResult() {
            return skipNullsInResult;
        }
    }

}
