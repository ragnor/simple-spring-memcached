package net.nelz.simplesm.aop;

import static org.junit.Assert.*;

import org.junit.BeforeClass;
import org.junit.Test;

import net.nelz.simplesm.api.*;

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
 * @author Nelson Carpentier
 * 
 */
public class UpdateSingleCacheAdviceTest {
    private static UpdateSingleCacheAdvice cut;

    @BeforeClass
    public static void beforeClass() {
        cut = new UpdateSingleCacheAdvice();
        cut.setMethodStore(new CacheKeyMethodStoreImpl());
        // cut.updateSingle();
    }

    @Test
    public void testGetObjectId() throws Exception {
        final String key = "Key-" + System.currentTimeMillis();
        final KeyObject obj = new KeyObject();
        obj.setKey(key);

        final String result = cut.getObjectId(-1, obj, null, null);

        assertEquals(key, result);
    }

    @SuppressWarnings("unused")
    private static class AnnotationValidator {

        @UpdateSingleCache(namespace = "bubba")
        public String cacheMe1() {
            return null;
        }

    }

    @SuppressWarnings("unused")
    private static class KeyObject {
        private String key;

        @CacheKeyMethod
        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }
    }

}
