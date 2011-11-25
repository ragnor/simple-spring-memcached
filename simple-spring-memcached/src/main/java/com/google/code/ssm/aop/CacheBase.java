/*
 * Copyright (c) 2008-2011 Nelson Carpentier, Jakub Białek
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
 */

package com.google.code.ssm.aop;

import java.lang.reflect.Method;
import java.security.InvalidParameterException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.code.ssm.api.ReadThroughMultiCache;
import com.google.code.ssm.api.format.UseJson;
import com.google.code.ssm.exceptions.InvalidAnnotationException;
import com.google.code.ssm.impl.NoClass;
import com.google.code.ssm.impl.PertinentNegativeNull;
import com.google.code.ssm.providers.CacheClient;
import com.google.code.ssm.providers.CacheException;
import com.google.code.ssm.providers.CacheTranscoder;
import com.google.code.ssm.transcoders.JsonTranscoders;
import com.google.code.ssm.util.Utils;

/**
 * 
 * @author Nelson Carpentier, Jakub Białek
 * 
 */
public abstract class CacheBase {

    protected static final String SEPARATOR = ":";

    protected static final String ID_SEPARATOR = "/";

    protected CacheClient cache;

    @Autowired
    protected CacheKeyBuilder cacheKeyBuilder;

    @Autowired
    protected JsonTranscoders jsonTranscoders;

    public void setCache(CacheClient cache) {
        this.cache = cache;
    }

    public void setCacheKeyBuilder(CacheKeyBuilder cacheKeyBuilder) {
        this.cacheKeyBuilder = cacheKeyBuilder;
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

    @SuppressWarnings("unchecked")
    protected <T> T getUpdateData(final AnnotationData annotationData, final Method method, final JoinPoint jp, final Object returnValue)
            throws Exception {
        return annotationData.isReturnDataIndex() ? (T) returnValue : (T) getIndexObject(annotationData.getDataIndex(), jp, method);
    }

    protected String getCacheKey(final AnnotationData annotationData, final JoinPoint jp, final Method method) throws Exception {
        return cacheKeyBuilder.getCacheKey(annotationData.getKeyIndexes(), jp.getArgs(), annotationData.getNamespace(), method);
    }

    protected Object getSubmission(Object o) {
        return o == null ? PertinentNegativeNull.NULL : o;
    }

    protected Object getResult(Object result) {
        return (result instanceof PertinentNegativeNull) ? null : result;
    }
    
    protected String getAssignCacheKey(final AnnotationData data) {
        return cacheKeyBuilder.getAssignCacheKey(data.getAssignedKey(), data.getNamespace());
    }

    protected Object getIndexObject(final int index, final JoinPoint jp, final Method methodToCache) throws Exception {
        return Utils.getMethodArg(index, jp.getArgs(), methodToCache);
    }

    protected Object validateReturnValueAsKeyObject(final Object returnValue, final Method methodToCache) throws Exception {
        if (returnValue == null) {
            throw new InvalidParameterException(String.format(
                    "The result of the method [%s] is null, which will not give an appropriate cache key.", methodToCache.toString()));
        }
        return returnValue;
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
        return cacheKeyBuilder.getCacheKeys(keyObjects, annotationData.getNamespace());
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

    protected Class<?> getParameterJsonClass(Method method, int index) {
        UseJson useJsonAnnotation = method.getAnnotation(UseJson.class);
        if (useJsonAnnotation == null) {
            return null;
        }

        if (useJsonAnnotation.value() != NoClass.class) {
            return useJsonAnnotation.value();
        }

        if (index < 0) {
            throw new IllegalArgumentException("Parameter index is below 0");
        }

        return method.getParameterTypes()[index];
    }

    protected Class<?> getReturnJsonClass(Method method) {
        UseJson useJsonAnnotation = method.getAnnotation(UseJson.class);
        if (useJsonAnnotation == null) {
            return null;
        }

        if (useJsonAnnotation.value() != NoClass.class) {
            return useJsonAnnotation.value();
        }

        return method.getReturnType();
    }

    protected Class<?> getDataJsonClass(Method method, AnnotationData annotationData) {
        if (annotationData.isReturnDataIndex()) {
            return getReturnJsonClass(method);
        } else {
            return getParameterJsonClass(method, annotationData.getDataIndex());
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
