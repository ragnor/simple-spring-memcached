/*
 * Copyright (c) 2011 Jakub Białek
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

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.aspectj.lang.JoinPoint;

import com.google.code.ssm.exceptions.InvalidAnnotationException;
import com.google.code.ssm.impl.PertinentNegativeNull;
import com.google.code.ssm.util.Utils;

/**
 * 
 * @author Jakub Białek
 * @since 2.0.0
 * 
 */
public abstract class MultiCacheAdvice extends CacheBase {

    static final Integer[] INTEGER_ARRAY = new Integer[0];

    MapHolder convertIdObjectsToKeyMap(final List<Object> idObjects, final Object[] keys, final int listIndex, final AnnotationData data)
            throws Exception {
        final MapHolder holder = new MapHolder();

        for (final Object obj : idObjects) {
            if (obj == null) {
                throw new InvalidParameterException("One of the passed in key objects is null");
            }

            keys[listIndex] = obj;
            String cacheKey = cacheKeyBuilder.getCacheKey(keys, data.getNamespace());
            if (holder.getObj2Key().get(obj) == null) {
                holder.getObj2Key().put(obj, cacheKey);
            }
            if (holder.getKey2Obj().get(cacheKey) == null) {
                holder.getKey2Obj().put(cacheKey, obj);
            }
        }

        return holder;
    }

    @SuppressWarnings("unchecked")
    Object[] getKeyObjects(final Collection<Integer> keysIndex, final JoinPoint jp, final Method method, MultiCacheCoordinator coord,
            Annotation annotation) throws Exception {
        Object[] results = Utils.getMethodArgs(keysIndex, jp.getArgs(), method);
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

    protected void addNullValues(List<Object> missObjects, MultiCacheCoordinator coord, Class<?> jsonClass) {
        for (Object keyObject : missObjects) {
            addSilently(coord.getObj2Key().get(keyObject), coord.getAnnotationData().getExpiration(), PertinentNegativeNull.NULL, jsonClass);
        }
    }

    protected void setNullValues(List<Object> missObjects, MultiCacheCoordinator coord, Class<?> jsonClass) {
        for (Object keyObject : missObjects) {
            setSilently(coord.getObj2Key().get(keyObject), coord.getAnnotationData().getExpiration(), PertinentNegativeNull.NULL, jsonClass);
        }
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
                    results.add(getResult(keyResult));
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
                    results.add(getResult(keyResult));
                }
            }

            return results;
        }

        protected Object getResult(Object result) {
            return (result instanceof PertinentNegativeNull) ? null : result;
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
