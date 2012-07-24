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
 */

package com.google.code.ssm.aop;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.RandomStringUtils;
import org.easymock.EasyMock;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.code.ssm.Cache;
import com.google.code.ssm.aop.support.AnnotationData;
import com.google.code.ssm.aop.support.AnnotationDataBuilder;
import com.google.code.ssm.aop.support.InvalidAnnotationException;
import com.google.code.ssm.aop.support.PertinentNegativeNull;
import com.google.code.ssm.api.AnnotationConstants;
import com.google.code.ssm.api.ParameterDataUpdateContent;
import com.google.code.ssm.api.ParameterValueKeyProvider;
import com.google.code.ssm.api.UpdateMultiCache;

/**
 * 
 * @author Nelson Carpentier
 * 
 */
public class UpdateMultiCacheAdviceTest {

    private static UpdateMultiCacheAdvice cut;

    @BeforeClass
    public static void beforeClass() {
        cut = new UpdateMultiCacheAdvice();
        cut.setCacheBase(new CacheBase());
    }

    @Test
    public void testGetCacheKeys() throws Exception {
        final int size = 10;
        final List<Object> sources = new ArrayList<Object>();
        for (int ix = 0; ix < size; ix++) {
            sources.add(RandomStringUtils.randomAlphanumeric(3 + ix));
        }

        final String namespace = RandomStringUtils.randomAlphabetic(20);
        final AnnotationData annotationData = new AnnotationData();
        annotationData.setNamespace(namespace);
        final List<String> results = cut.getCacheBase().getCacheKeyBuilder().getCacheKeys(sources, annotationData.getNamespace());

        assertEquals(size, results.size());
        for (int ix = 0; ix < size; ix++) {
            final String result = results.get(ix);
            assertTrue(result.indexOf(namespace) != -1);
            final String source = (String) sources.get(ix);
            assertTrue(result.indexOf(source) != -1);
        }
    }

    @Test
    public void testUpdateCache() throws Exception {
        final Method method = AnnotationTest.class.getMethod("cacheMe01", List.class);
        final UpdateMultiCache annotation = method.getAnnotation(UpdateMultiCache.class);
        final AnnotationData data = AnnotationDataBuilder.buildAnnotationData(annotation, UpdateMultiCache.class, method);

        final List<String> keys = new ArrayList<String>();
        final List<Object> objs = new ArrayList<Object>();
        keys.add("Key1-" + System.currentTimeMillis());
        keys.add("Key2-" + System.currentTimeMillis());

        try {
            cut.updateCache(keys, objs, method, data, null);
            fail("Expected Exception.");
        } catch (InvalidAnnotationException ex) {
            assertTrue(ex.getMessage().contains("do not match in size"));
        }

        final Cache cache = EasyMock.createMock(Cache.class);
        EasyMock.expect(cache.getName()).andReturn(AnnotationConstants.DEFAULT_CACHE_NAME);
        EasyMock.expect(cache.getAliases()).andReturn(Collections.<String> emptyList()).anyTimes();
        for (final String key : keys) {
            final String value = "ValueFor-" + key;
            objs.add(value);
            cache.setSilently(key, data.getExpiration(), value, null);
            EasyMock.expectLastCall();
        }
        keys.add("BigFatNull");
        objs.add(null);
        cache.setSilently(keys.get(2), data.getExpiration(), new PertinentNegativeNull(), null);
        EasyMock.expectLastCall();

        EasyMock.replay(cache);

        cut.getCacheBase().addCache(cache);
        cut.updateCache(keys, objs, method, data, null);

        EasyMock.verify(cache);
    }

    static class AnnotationTest {

        @UpdateMultiCache(namespace = "Bubba", expiration = 300)
        public void cacheMe01(@ParameterDataUpdateContent @ParameterValueKeyProvider final List<String> nada) {
        }

        public String cacheMe02() {
            return null;
        }

        public ArrayList<String> cacheMe03() {
            return null;
        }

    }

}
