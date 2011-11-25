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

package com.google.code.ssm.impl;

import java.lang.reflect.Method;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.code.ssm.aop.CacheKeyBuilder;
import com.google.code.ssm.api.KeyProvider;
import com.google.code.ssm.util.Utils;

/**
 * 
 * @author Jakub Białek
 * @since 2.0.0
 * 
 */
@Service("cacheKeyBuilder")
public class CacheKeyBuilderImpl implements CacheKeyBuilder {

    private static final String SEPARATOR = ":";

    private static final String ID_SEPARATOR = "/";

    @Autowired
    protected KeyProvider defaultKeyProvider;

    public void setDefaultKeyProvider(KeyProvider defaultKeyProvider) {
        this.defaultKeyProvider = defaultKeyProvider;
    }

    public String getCacheKey(final Collection<Integer> keyIndexes, final Object[] args, final String namespace, final Method method)
            throws Exception {
        final Object[] keysObjects = Utils.getMethodArgs(keyIndexes, args, method);
        final String[] objectsIds = defaultKeyProvider.generateKeys(keysObjects);
        return getCacheKey(objectsIds, namespace);
    }

    public String getCacheKey(final Object[] keyObjects, final String namespace) {
        if (keyObjects == null || keyObjects.length < 1) {
            throw new InvalidParameterException("Ids for objects in the cache must be at least 1 character long.");
        }

        final String[] objectIds = defaultKeyProvider.generateKeys(keyObjects);
        return buildCacheKey(objectIds, namespace);
    }

    /**
     * Builds cache key from one object id.
     * 
     * @param objectId
     * @param data
     * @return
     */
    public String getCacheKey(final Object keyObject, final String namespace) {
        return namespace + SEPARATOR + defaultKeyProvider.generateKey(keyObject);
    }

    public List<String> getCacheKeys(final List<Object> keyObjects, final String namespace) throws Exception {
        final List<String> results = new ArrayList<String>();
        for (final Object object : keyObjects) {
            final String objectId = defaultKeyProvider.generateKey(object);
            results.add(getCacheKey(objectId, namespace));
        }

        return results;
    }

    /**
     * Builds cache key from one object id.
     * 
     * @param objectId
     * @param data
     * @return
     */
    public String getAssignCacheKey(final String objectId, final String namespace) {
        if (objectId == null || objectId.length() < 1) {
            throw new InvalidParameterException("Ids for objects in the cache must be at least 1 character long.");
        }
        return namespace + SEPARATOR + objectId;
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

    private void checkKeyPart(String keyPart) {
        if (keyPart == null || keyPart.length() < 1) {
            throw new InvalidParameterException("Ids for objects in the cache must be at least 1 character long.");
        }
    }

}
