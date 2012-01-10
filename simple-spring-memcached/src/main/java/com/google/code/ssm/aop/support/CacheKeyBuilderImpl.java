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

package com.google.code.ssm.aop.support;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;

import com.google.code.ssm.util.Utils;

/**
 * 
 * @author Jakub Białek
 * @since 2.0.0
 * 
 */
public class CacheKeyBuilderImpl implements CacheKeyBuilder { // NO_UCD

    private static final String SEPARATOR = ":";

    private static final String ID_SEPARATOR = "/";

    private KeyProvider defaultKeyProvider = new DefaultKeyProvider();

    public void setDefaultKeyProvider(final KeyProvider defaultKeyProvider) {
        this.defaultKeyProvider = defaultKeyProvider;
    }

    public KeyProvider getDefaultKeyProvider() {
        return this.defaultKeyProvider;
    }

    @Override
    public String getCacheKey(final AnnotationData data, final Object[] args, final String methodDesc) throws Exception {
        final Object[] keysObjects = Utils.getMethodArgs(data.getKeyIndexes(), args, methodDesc);
        return getCacheKey(keysObjects, data.getNamespace());
    }

    /**
     * Builds cache key from one key object.
     * 
     * @param keyObject
     * @param namespace
     * @return
     */
    @Override
    public String getCacheKey(final Object keyObject, final String namespace) {
        return namespace + SEPARATOR + defaultKeyProvider.generateKey(keyObject);
    }

    @Override
    public List<String> getCacheKeys(final List<Object> keyObjects, final String namespace) throws Exception {
        final List<String> results = new ArrayList<String>();
        for (final Object object : keyObjects) {
            final String objectId = defaultKeyProvider.generateKey(object);
            results.add(getCacheKey(objectId, namespace));
        }

        return results;
    }

    @Override
    public List<String> getCacheKeys(final AnnotationData data, final Object[] args, final String methodDesc) {
        @SuppressWarnings("unchecked")
        final List<Object> listObjects = (List<Object>) args[data.getListIndexInMethodArgs()];
        final List<String> cacheKeys = new ArrayList<String>(listObjects.size());
        final Object[] keyObjects = Utils.getMethodArgs(data.getKeyIndexes(), args, methodDesc);

        Object[] keys = new Object[keyObjects.length];
        System.arraycopy(keyObjects, 0, keys, 0, keys.length);

        for (final Object obj : listObjects) {
            if (obj == null) {
                throw new InvalidParameterException("One of the passed in key objects is null");
            }

            keys[data.getListIndexInKeys()] = obj;
            cacheKeys.add(getCacheKey(keys, data.getNamespace()));
        }

        return cacheKeys;
    }

    @Override
    public String getAssignCacheKey(final AnnotationData data) {
        if (data == null || data.getAssignedKey() == null || data.getAssignedKey().length() < 1) {
            throw new InvalidParameterException("Ids for objects in the cache must be at least 1 character long.");
        }
        return data.getNamespace() + SEPARATOR + data.getAssignedKey();
    }

    private String getCacheKey(final Object[] keyObjects, final String namespace) {
        if (keyObjects == null || keyObjects.length < 1) {
            throw new InvalidParameterException("Ids for objects in the cache must be at least 1 character long.");
        }

        final String[] objectIds = defaultKeyProvider.generateKeys(keyObjects);
        return buildCacheKey(objectIds, namespace);
    }

    /**
     * Build cache key.
     * 
     * @param objectIds
     * @param namespace
     * @return
     */
    private String buildCacheKey(final String[] objectIds, final String namespace) {
        if (objectIds.length == 1) {
            checkKeyPart(objectIds[0]);
            return namespace + SEPARATOR + objectIds[0];
        }

        StringBuilder cacheKey = new StringBuilder(namespace);
        cacheKey.append(SEPARATOR);
        for (String id : objectIds) {
            checkKeyPart(id);
            cacheKey.append(id);
            cacheKey.append(ID_SEPARATOR);
        }

        cacheKey.deleteCharAt(cacheKey.length() - 1);
        return cacheKey.toString();
    }

    private void checkKeyPart(final String keyPart) {
        if (keyPart == null || keyPart.length() < 1) {
            throw new InvalidParameterException("Ids for objects in the cache must be at least 1 character long.");
        }
    }

}
