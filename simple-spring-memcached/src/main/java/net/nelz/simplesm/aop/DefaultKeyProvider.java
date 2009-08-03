package net.nelz.simplesm.aop;

import net.nelz.simplesm.api.KeyProvider;
import net.nelz.simplesm.api.CacheKeyMethod;
import net.nelz.simplesm.exceptions.InvalidAnnotationException;

import java.util.List;
import java.util.ArrayList;
import java.lang.reflect.Method;
import java.security.InvalidParameterException;

/**
Copyright (c) 2008, 2009  Nelson Carpentier

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
public class DefaultKeyProvider implements KeyProvider {

    private CacheKeyMethodStore methodStore;

    public void setMethodStore(CacheKeyMethodStore methodStore) {
        this.methodStore = methodStore;
    }

    public String generateKey(final Object keyObject) {
        try {
            final Method keyMethod = getKeyMethod(keyObject);
            /*
            Step 1: Underpants (Get the method that will provide our key.)
            Step 2: ? (Make it give us the key.)
            Step 3: PROFIT! (Return the key to the caller.)
             */
            return null;
        } catch (NoSuchMethodException ex) {
            throw new RuntimeException(ex);
        }
    }

    public List<String> generateKeys(final List<Object> keyObjects) {
        if (keyObjects == null || keyObjects.size() < 1) {
            throw new InvalidParameterException("The key objects must be defined.");
        }
        final List<String> results = new ArrayList<String>(keyObjects.size());
        for (final Object keyObject : keyObjects) {
            results.add(generateKey(keyObject));
        }
        return results;
    }

    Method getKeyMethod(final Object keyObject) throws NoSuchMethodException {
        final Method storedMethod = methodStore.find(keyObject.getClass());
        if (storedMethod != null) { return storedMethod; }
        final Method[] methods = keyObject.getClass().getDeclaredMethods();
        Method targetMethod = null;
        for (final Method method : methods) {
            if (method != null && method.getAnnotation(CacheKeyMethod.class) != null) {
                if (method.getParameterTypes().length > 0) {
                    throw new InvalidAnnotationException(String.format(
                            "Method [%s] must have 0 arguments to be annotated with [%s]",
                            method.toString(),
                            CacheKeyMethod.class.getName()));
                }
                if (!String.class.equals(method.getReturnType())) {
                    throw new InvalidAnnotationException(String.format(
                            "Method [%s] must return a String to be annotated with [%s]",
                            method.toString(),
                            CacheKeyMethod.class.getName()));
                }
                if (targetMethod != null) {
                    throw new InvalidAnnotationException(String.format(
                            "Class [%s] should have only one method annotated with [%s]. See [%s] and [%s]",
                            keyObject.getClass().getName(),
                            CacheKeyMethod.class.getName(),
                            targetMethod.getName(),
                            method.getName()));
                }
                targetMethod = method;
            }
        }

        if (targetMethod == null) {
            targetMethod = keyObject.getClass().getMethod("toString", null);
        }

        methodStore.add(keyObject.getClass(), targetMethod);

        return targetMethod;
    }

}
