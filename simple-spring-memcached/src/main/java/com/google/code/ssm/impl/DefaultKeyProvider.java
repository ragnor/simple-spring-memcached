/*
 * Copyright (c) 2008-2009 Nelson Carpentier
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

package com.google.code.ssm.impl;

import java.lang.reflect.Method;
import java.security.InvalidParameterException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.code.ssm.aop.CacheKeyMethodStore;
import com.google.code.ssm.api.KeyProvider;

/**
 * 
 * @author Nelson Carpentier
 * @author Jakub Białek
 * 
 */
@Service("defaultKeyProvider")
public class DefaultKeyProvider implements KeyProvider { // NO_UCD

    @Autowired
    private CacheKeyMethodStore methodStore;

    public void setMethodStore(CacheKeyMethodStore methodStore) {
        this.methodStore = methodStore;
    }

    public String generateKey(final Object keyObject) {
        if (keyObject == null) {
            throw new InvalidParameterException("keyObject must be defined");
        }
        try {
            final Method keyMethod = getKeyMethod(keyObject);
            return generateObjectId(keyMethod, keyObject);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public String[] generateKeys(final Object[] keyObjects) {
        if (keyObjects == null || keyObjects.length < 1) {
            throw new InvalidParameterException("The key objects must be defined.");
        }

        final String[] results = new String[(keyObjects.length)];
        for (int i = 0; i < keyObjects.length; i++) {
            results[i] = generateKey(keyObjects[i]);
        }

        return results;
    }

    Method getKeyMethod(final Object keyObject) throws NoSuchMethodException {
        return methodStore.getKeyMethod(keyObject.getClass());
    }

    String generateObjectId(final Method keyMethod, final Object keyObject) throws Exception {
        final String objectId = (String) keyMethod.invoke(keyObject, (Object[]) null);
        if (objectId == null || objectId.length() < 1) {
            throw new RuntimeException("Got an empty key value from " + keyMethod.getName());
        }
        return objectId;
    }

}
