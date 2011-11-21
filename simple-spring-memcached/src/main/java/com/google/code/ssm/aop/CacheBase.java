package com.google.code.ssm.aop;

import java.lang.reflect.Method;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.google.code.ssm.api.CacheKeyMethod;
import com.google.code.ssm.api.KeyProvider;
import com.google.code.ssm.api.ReadThroughMultiCache;
import com.google.code.ssm.api.format.UseJson;
import com.google.code.ssm.exceptions.InvalidAnnotationException;
import com.google.code.ssm.impl.NoClass;
import com.google.code.ssm.providers.CacheClient;
import com.google.code.ssm.providers.CacheException;
import com.google.code.ssm.providers.CacheTranscoder;
import com.google.code.ssm.transcoders.JsonTranscoders;

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
public abstract class CacheBase implements ApplicationContextAware {

    protected static final String SEPARATOR = ":";

    protected static final String ID_SEPARATOR = "/";

    protected CacheClient cache;

    @Autowired
    protected CacheKeyMethodStore methodStore;

    protected KeyProvider defaultKeyProvider;

    protected ApplicationContext applicationContext;

    @Autowired
    protected JsonTranscoders jsonTranscoders;

    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    public void setCache(CacheClient cache) {
        this.cache = cache;
    }

    public void setMethodStore(CacheKeyMethodStore methodStore) {
        this.methodStore = methodStore;
    }

    public void setDefaultKeyProvider(KeyProvider defaultKeyProvider) {
        this.defaultKeyProvider = defaultKeyProvider;
    }

    protected Method getMethodToCache(final JoinPoint jp) throws NoSuchMethodException {
        final Signature sig = jp.getSignature();
        if (!(sig instanceof MethodSignature)) {
            throw new InvalidAnnotationException("This annotation is only valid on a method.");
        }
        final MethodSignature msig = (MethodSignature) sig;
        final Object target = jp.getTarget();
        // cannot use msig.getMethod() because it can return the method where annotation was declared i.e. method in
        // interface
        return target.getClass().getMethod(msig.getName(), msig.getParameterTypes());
    }

    protected String[] getObjectIds(final Collection<Integer> keysIndexes, final JoinPoint jp, final Method methodToCache) throws Exception {
        final Object[] keysObjects = getIndexObjects(keysIndexes, jp, methodToCache);
        final Method[] keysMethods = getKeysMethods(keysObjects);
        return generateObjectIds(keysMethods, keysObjects);
    }

    protected String generateObjectId(final Method keyMethod, final Object keyObject) throws Exception {
        final String objectId = (String) keyMethod.invoke(keyObject, new Object[0]);
        if (objectId == null || objectId.length() < 1) {
            throw new RuntimeException("Got an empty key value from " + keyMethod.getName());
        }
        return objectId;
    }

    protected String[] generateObjectIds(final Method[] keysMethods, final Object[] keysObjects) throws Exception {
        if (keysMethods == null || keysObjects == null || keysMethods.length != keysObjects.length) {
            throw new RuntimeException("Got arrays of key and method with different size. Keys: "
                    + (keysObjects == null ? null : Arrays.asList(keysObjects)) + ", methods: "
                    + (keysMethods == null ? null : Arrays.asList(keysMethods)));
        }

        String[] ids = new String[keysMethods.length];
        for (int i = 0; i < keysMethods.length; i++) {
            ids[i] = generateObjectId(keysMethods[i], keysObjects[i]);
        }

        return ids;
    }

    @SuppressWarnings("unchecked")
    protected <T> T getUpdateData(AnnotationData annotationData, Method method, JoinPoint jp, Object returnValue) throws Exception {
        return annotationData.isReturnDataIndex() ? (T) returnValue : (T) getIndexObject(annotationData.getDataIndex(), jp, method);
    }

    protected String getCacheKey(AnnotationData annotationData, JoinPoint jp, Method method) throws Exception {
        final String[] objectsIds = getObjectIds(annotationData.getKeysIndex(), jp, method);
        return buildCacheKey(objectsIds, annotationData);
    }
    
    protected Object getSubmission(Object o) {
        return o == null ? PertinentNegativeNull.NULL : o;
    }

    /**
     * Builds cache key from one object id.
     * 
     * @param objectId
     * @param data
     * @return
     */
    protected String buildCacheKey(final String objectId, final AnnotationData data) {
        if (objectId == null || objectId.length() < 1) {
            throw new InvalidParameterException("Ids for objects in the cache must be at least 1 character long.");
        }
        return data.getNamespace() + SEPARATOR + objectId;
    }

    /**
     * Build cache key from more than one object id.
     * 
     * @param objectIds
     * @param namespace
     * @return
     */
    private String buildCacheKey(final String[] objectIds, final String namespace) {
        StringBuilder cacheKey = new StringBuilder(namespace);
        cacheKey.append(SEPARATOR);
        for (String id : objectIds) {
            if (id == null || id.length() < 1) {
                throw new InvalidParameterException("Ids for objects in the cache must be at least 1 character long.");
            }
            cacheKey.append(id);
            cacheKey.append(ID_SEPARATOR);
        }

        cacheKey.deleteCharAt(cacheKey.length() - 1);
        return cacheKey.toString();
    }

    protected String buildCacheKey(final String[] objectIds, final AnnotationData data) {
        if (objectIds == null || objectIds.length < 1) {
            throw new InvalidParameterException("Ids for objects in the cache must be at least 1 character long.");
        }

        if (objectIds.length == 1) {
            return buildCacheKey(objectIds[0], data);
        } else {
            return buildCacheKey(objectIds, data.getNamespace());
        }
    }

    protected Object getIndexObject(final int index, final JoinPoint jp, final Method methodToCache) throws Exception {
        if (index < 0) {
            throw new InvalidParameterException(String.format("An index of %s is invalid", index));
        }
        final Object[] args = jp.getArgs();
        if (args.length <= index) {
            throw new InvalidParameterException(String.format("An index of %s is too big for the number of arguments in [%s]", index,
                    methodToCache.toString()));
        }
        final Object indexObject = args[index];
        if (indexObject == null) {
            throw new InvalidParameterException(String.format("The argument passed into [%s] at index %s is null.",
                    methodToCache.toString(), index));
        }
        return indexObject;
    }

    protected Object[] getIndexObjects(final Collection<Integer> indexes, final JoinPoint jp, final Method methodToCache) throws Exception {
        Object[] results = new Object[indexes.size()];
        Iterator<Integer> iter = indexes.iterator();
        for (int i = 0; i < indexes.size(); i++) {
            results[i] = getIndexObject(iter.next(), jp, methodToCache);
        }

        return results;
    }

    protected Object validateReturnValueAsKeyObject(final Object returnValue, final Method methodToCache) throws Exception {
        if (returnValue == null) {
            throw new InvalidParameterException(String.format(
                    "The result of the method [%s] is null, which will not give an appropriate cache key.", methodToCache.toString()));
        }
        return returnValue;
    }

    protected Method getKeyMethod(final Object keyObject) throws NoSuchMethodException {
        final Method storedMethod = methodStore.find(keyObject.getClass());
        if (storedMethod != null) {
            return storedMethod;
        }
        final Method[] methods = keyObject.getClass().getDeclaredMethods();
        Method targetMethod = null;
        for (final Method method : methods) {
            if (method != null && method.getAnnotation(CacheKeyMethod.class) != null) {
                if (method.getParameterTypes().length > 0) {
                    throw new InvalidAnnotationException(String.format("Method [%s] must have 0 arguments to be annotated with [%s]",
                            method.toString(), CacheKeyMethod.class.getName()));
                }
                if (!String.class.equals(method.getReturnType())) {
                    throw new InvalidAnnotationException(String.format("Method [%s] must return a String to be annotated with [%s]",
                            method.toString(), CacheKeyMethod.class.getName()));
                }
                if (targetMethod != null) {
                    throw new InvalidAnnotationException(String.format(
                            "Class [%s] should have only one method annotated with [%s]. See [%s] and [%s]",
                            keyObject.getClass().getName(), CacheKeyMethod.class.getName(), targetMethod.getName(), method.getName()));
                }
                targetMethod = method;
            }
        }

        if (targetMethod == null) {
            targetMethod = keyObject.getClass().getMethod("toString", (Class<?>[]) null);
        }

        methodStore.add(keyObject.getClass(), targetMethod);

        return targetMethod;
    }

    protected Method[] getKeysMethods(final Object[] keysObjects) throws NoSuchMethodException {
        Method[] methods = new Method[keysObjects.length];
        for (int i = 0; i < keysObjects.length; i++) {
            methods[i] = getKeyMethod(keysObjects[i]);
        }

        return methods;
    }

    protected void verifyReturnTypeIsList(final Method method, final Class<?> annotationClass) {
        if (!verifyTypeIsList(method.getReturnType())) {
            throw new InvalidAnnotationException(String.format(
                    "The annotation [%s] is only valid on a method that returns a [%s] or its subclass. "
                            + "[%s] does not fulfill this requirement.", ReadThroughMultiCache.class.getName(), List.class.getName(),
                    method.toString()));
        }
    }

    protected boolean verifyTypeIsList(final Class<?> clazz) {
        return List.class.isAssignableFrom(clazz);
    }

    protected void verifyReturnTypeIsNoVoid(final Method method, final Class<?> annotationClass) {
        if (method.getReturnType().equals(void.class)) {
            throw new InvalidParameterException(String.format("Annotation [%s] is defined on void method  [%s]", annotationClass,
                    method.getName()));
        }
    }

    protected List<String> getCacheKeys(final List<Object> keyObjects, final AnnotationData annotationData) throws Exception {
        final List<String> results = new ArrayList<String>();
        for (final Object object : keyObjects) {
            final Method keyMethod = getKeyMethod(object);
            final String objectId = generateObjectId(keyMethod, object);
            results.add(buildCacheKey(objectId, annotationData));
        }

        return results;
    }

    @SuppressWarnings("unchecked")
    protected <T> T get(String cacheKey, Class<T> clazz) throws TimeoutException, CacheException {
        if (clazz != null) {
            CacheTranscoder<?> transcoder = jsonTranscoders.getTranscoder(clazz);
            if (transcoder != null) {
                return (T) get(cacheKey, transcoder);
            } else {
                warn("There's no json transcoder for class " + clazz.getName() + ", standard transcoder will be used for key " + cacheKey);
            }
        }

        return (T) cache.get(cacheKey);
    }

    protected <T> T get(String cacheKey, CacheTranscoder<T> transcoder) throws TimeoutException, CacheException {
        return cache.get(cacheKey, transcoder);
    }

    @SuppressWarnings("unchecked")
    protected <T> void set(String cacheKey, int expiration, Object value, Class<?> clazz) throws TimeoutException, CacheException {
        if (clazz != null) {
            CacheTranscoder<?> transcoder = jsonTranscoders.getTranscoder(clazz);
            if (transcoder != null) {
                set(cacheKey, expiration, (T) value, (CacheTranscoder<T>) transcoder);
                return;
            } else {
                warn("There's no json transcoder for class " + clazz.getName() + ", standard transcoder will be used for key " + cacheKey);
            }
        }

        cache.set(cacheKey, expiration, value);
    }

    protected <T> void set(String cacheKey, int expiration, T value, CacheTranscoder<T> transcoder) throws TimeoutException, CacheException {
        cache.set(cacheKey, expiration, value, transcoder);
        if (getLogger().isInfoEnabled()) {
            info("Set [json] under key: " + cacheKey + ", object: " + value + ", json: " + (value != null ? value.getClass() : null));
        }
    }

    protected <T> void setSilently(String cacheKey, int expiration, Object value, Class<T> clazz) {
        try {
            set(cacheKey, expiration, value, clazz);
        } catch (TimeoutException e) {
            warn("Cannot set on key " + cacheKey, e);
        } catch (CacheException e) {
            warn("Cannot set on key " + cacheKey, e);
        }
    }

    protected <T> void add(String cacheKey, int expiration, Object value, Class<T> clazz) throws TimeoutException, CacheException {
        if (clazz != null) {
            CacheTranscoder<?> transcoder = jsonTranscoders.getTranscoder(clazz);
            if (transcoder != null) {
                cache.add(cacheKey, expiration, value, jsonTranscoders.getTranscoder(clazz));
                if (getLogger().isInfoEnabled()) {
                    info("Add nullValue under key: " + cacheKey + ", json: " + clazz);
                }
                return;
            } else {
                warn("There's no json transcoder for class " + clazz.getName() + ", standard transcoder will be used for key " + cacheKey);
            }
        }

        cache.add(cacheKey, expiration, value);
    }

    protected <T> void addSilently(String cacheKey, int expiration, Object value, Class<?> clazz) {
        try {
            add(cacheKey, expiration, value, clazz);
        } catch (TimeoutException e) {
            warn("Cannot add to key " + cacheKey, e);
        } catch (CacheException e) {
            warn("Cannot add to key " + cacheKey, e);
        }
    }

    protected long decr(String key, int by) throws TimeoutException, CacheException {
        return cache.decr(key, by);
    }

    protected long decr(String key, int by, long def) throws TimeoutException, CacheException {
        return cache.decr(key, by, def);
    }

    protected long incr(String key, int by) throws TimeoutException, CacheException {
        return cache.incr(key, by);
    }

    protected long incr(String key, int by, long def) throws TimeoutException, CacheException {
        return cache.incr(key, by, def);
    }

    protected long incr(String key, int by, long def, int exp) throws TimeoutException, CacheException {
        return cache.incr(key, by, def, exp);
    }

    protected void delete(String key) throws TimeoutException, CacheException {
        cache.delete(key);
    }

    protected void delete(Collection<String> keys) throws TimeoutException, CacheException {
        cache.delete(keys);
    }

    protected Map<String, Object> getBulk(Collection<String> keys, Class<?> clazz) throws TimeoutException, CacheException {
        if (clazz != null) {
            CacheTranscoder<?> transcoder = jsonTranscoders.getTranscoder(clazz);
            if (transcoder != null) {
                return cache.getBulk(keys, jsonTranscoders.getTranscoder(clazz));
            } else {
                warn("There's no json transcoder for class " + clazz.getName() + ", standard transcoder will be used for keys " + keys);
            }
        }

        return cache.getBulk(keys);
    }

    protected Class<?> getJsonClass(Method methodToCache, int index) {
        UseJson useJsonAnnotation = methodToCache.getAnnotation(UseJson.class);
        if (useJsonAnnotation == null) {
            return null;
        }

        if (useJsonAnnotation.value() != NoClass.class) {
            return useJsonAnnotation.value();
        }

        if (index == AnnotationData.RETURN_INDEX) {
            return methodToCache.getReturnType();
        } else {
            return methodToCache.getParameterTypes()[index];
        }
    }

    protected void info(String msg) {
        getLogger().info(msg);
    }

    protected void warn(String msg) {
        getLogger().warn(msg);
    }

    protected void warn(String msg, Throwable t) {
        getLogger().warn(msg, t);
    }

    protected abstract Logger getLogger();
}
