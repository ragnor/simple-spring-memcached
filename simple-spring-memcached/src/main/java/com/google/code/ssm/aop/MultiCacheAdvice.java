/*
 * Copyright (c) 2012 Jakub Białek
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
 */

package com.google.code.ssm.aop;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.code.ssm.aop.support.AnnotationData;
import com.google.code.ssm.aop.support.PertinentNegativeNull;
import com.google.code.ssm.api.ParameterValueKeyProvider;
import com.google.code.ssm.api.format.SerializationType;
import com.google.code.ssm.util.Utils;

/**
 * 
 * @author Jakub Białek
 * @since 2.0.0
 * 
 */
abstract class MultiCacheAdvice extends CacheAdvice {

    MapHolder createObjectIdCacheKeyMapping(final AnnotationData data, final Object[] args, final Method methodToCache) throws Exception {
        final MapHolder holder = new MapHolder();
        List<String> cacheKeys = getCacheBase().getCacheKeyBuilder().getCacheKeys(data, args, methodToCache.toString());

        @SuppressWarnings("unchecked")
        List<Object> listObjects = (List<Object>) Utils.getMethodArg(data.getListIndexInMethodArgs(), args, methodToCache.toString());

        Iterator<Object> listObjectsIter = listObjects.iterator();
        Iterator<String> cacheKeysIter = cacheKeys.iterator();

        while (listObjectsIter.hasNext()) {
            Object obj = listObjectsIter.next();
            String cacheKey = cacheKeysIter.next();
            if (holder.getObj2Key().get(obj) == null) {
                holder.getObj2Key().put(obj, cacheKey);
            }
            if (holder.getKey2Obj().get(cacheKey) == null) {
                holder.getKey2Obj().put(cacheKey, obj);
            }
        }

        return holder;
    }

    protected void addNullValues(final List<Object> missObjects, final MultiCacheCoordinator coord,
            final SerializationType serializationType) {
        for (Object keyObject : missObjects) {
            getCacheBase().getCache(coord.getAnnotationData()).addSilently(coord.getObj2Key().get(keyObject),
                    coord.getAnnotationData().getExpiration(), PertinentNegativeNull.NULL, serializationType);
        }
    }

    protected void setNullValues(final List<Object> missObjects, final MultiCacheCoordinator coord,
            final SerializationType serializationType) {
        for (Object keyObject : missObjects) {
            getCacheBase().getCache(coord.getAnnotationData()).setSilently(coord.getObj2Key().get(keyObject),
                    coord.getAnnotationData().getExpiration(), PertinentNegativeNull.NULL, serializationType);
        }
    }

    static class MapHolder {
        final private Map<String, Object> key2Obj = new LinkedHashMap<String, Object>();
        final private Map<Object, String> obj2Key = new LinkedHashMap<Object, String>();

        public Map<String, Object> getKey2Obj() {
            return key2Obj;
        }

        public Map<Object, String> getObj2Key() {
            return obj2Key;
        }
    }

    static class MultiCacheCoordinator {
        private final Method method;
        private final AnnotationData data;
        private final Map<String, Object> key2Obj = new LinkedHashMap<String, Object>();
        private final Map<Object, String> obj2Key = new LinkedHashMap<Object, String>();
        private final Map<String, Object> key2Result = new HashMap<String, Object>();
        private List<Object> listKeyObjects = new ArrayList<Object>();
        // list is not the best collection to store missed objects because remove operation is used in some cases,
        // set cannot be used because order of insertion is important and object can appear more than once
        private final List<Object> missedObjects = new ArrayList<Object>();
        private boolean addNullsToCache;
        private boolean generateKeysFromResult;
        private boolean skipNullsInResult;

        MultiCacheCoordinator(final Method method, final AnnotationData data) {
            this.method = method;
            this.data = data;
        }

        public Method getMethod() {
            return method;
        }

        public boolean isAddNullsToCache() {
            return addNullsToCache;
        }

        public void setAddNullsToCache(final boolean addNullsToCache) {
            this.addNullsToCache = addNullsToCache;
        }

        public void setGenerateKeysFromResult(final boolean generateKeysFromResult) {
            this.generateKeysFromResult = generateKeysFromResult;
        }

        public boolean isGenerateKeysFromResult() {
            return generateKeysFromResult;
        }

        public AnnotationData getAnnotationData() {
            return data;
        }

        public void setHolder(final MapHolder holder) {
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

        public List<Object> getListKeyObjects() {
            return listKeyObjects;
        }

        public void setListKeyObjects(final List<Object> listKeyObjects) {
            this.listKeyObjects = listKeyObjects;
        }

        public void setInitialKey2Result(final Map<String, Object> key2Result) {
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
            this.missedObjects.addAll(missObjectSet);
        }

        public List<Object> generateResultList() {
            final List<Object> results = new ArrayList<Object>();
            for (Object keyObject : listKeyObjects) {
                final String cacheKey = obj2Key.get(keyObject);
                final Object keyResult = key2Result.get(cacheKey);
                if (keyResult == null) {
                    throw new RuntimeException(String.format("Unable to fulfill data for the key item [%s] with key value of [%s].",
                            keyObject.toString(), obj2Key.get(keyObject)));
                }

                if (!isSkipNullsInResult() || !(keyResult instanceof PertinentNegativeNull)) {
                    results.add(getResult(keyResult));
                }
            }

            return results;
        }

        public List<Object> generatePartialResultList() {
            final List<Object> results = new ArrayList<Object>();
            for (Object keyObject : listKeyObjects) {
                final String cacheKey = obj2Key.get(keyObject);
                final Object keyResult = key2Result.get(cacheKey);
                if (keyResult != null && (!isSkipNullsInResult() || !(keyResult instanceof PertinentNegativeNull))) {
                    results.add(getResult(keyResult));
                }
            }

            return results;
        }

        public List<Object> getMissedObjects() {
            return missedObjects;
        }

        /**
         * Alters value of method's argument of type {@link List} annotated with {@link ParameterValueKeyProvider}. As a
         * new value of annotated list argument list of missed objects will be used.
         * 
         * @param args
         * @return
         */
        public Object[] modifyArgumentList(final Object[] args) {
            args[data.getListIndexInMethodArgs()] = this.missedObjects;
            return args;
        }

        public void setSkipNullsInResult(final boolean skipNullsInResult) {
            this.skipNullsInResult = skipNullsInResult;
        }

        public boolean isSkipNullsInResult() {
            return skipNullsInResult;
        }

        private Object getResult(final Object result) {
            return (result instanceof PertinentNegativeNull) ? null : result;
        }

    }

}
