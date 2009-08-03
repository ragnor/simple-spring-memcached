package net.nelz.simplesm.aop;

import net.nelz.simplesm.api.*;
import net.nelz.simplesm.exceptions.*;
import net.spy.memcached.MemcachedClientIF;
import org.apache.commons.lang.*;
import static org.testng.AssertJUnit.*;
import org.testng.annotations.*;
import org.easymock.EasyMock;

import java.lang.reflect.*;
import java.util.*;

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
public class UpdateMultiCacheAdviceTest {

	private UpdateMultiCacheAdvice cut;

	@BeforeClass
	public void beforeClass() {
		cut = new UpdateMultiCacheAdvice();
		cut.setMethodStore(new CacheKeyMethodStoreImpl());
        cut.updateMulti();
    }

	@Test
	public void testGetCacheKeys() throws Exception {
		final int size = 10;
		final List<Object> sources= new ArrayList<Object>();
		for (int ix = 0; ix < size; ix++) {
			sources.add(RandomStringUtils.randomAlphanumeric(3 + ix));
		}

		final String namespace = RandomStringUtils.randomAlphabetic(20);
        final AnnotationData annotationData = new AnnotationData();
        annotationData.setNamespace(namespace);
        final List<String> results = cut.getCacheKeys(sources, annotationData);

		assertEquals(size, results.size());
		for (int ix = 0; ix < size; ix++) {
			final String result = results.get(ix);
			assertTrue(result.indexOf(namespace) != -1);
			final String source = (String) sources.get(ix);
			assertTrue(result.indexOf(source) != -1);
		}
	}

	@Test
	public void testGetKeyObjects() throws Exception {
		final Method method1 = AnnotationTest.class.getMethod("cacheMe02", null);
		try {
			cut.getKeyObjects(-1, "bubba", null, method1);
		} catch (InvalidAnnotationException ex) {
			assertTrue(true);
		}

		final Method method2 = AnnotationTest.class.getMethod("cacheMe03", null);
		final ArrayList<String> results = new ArrayList<String>();
		results.add("gump");
		cut.getKeyObjects(-1, results, null, method2);
	}

    @Test
    public void testUpdateCache() throws Exception {
        final Method method = AnnotationTest.class.getMethod("cacheMe01", null);
        final UpdateMultiCache annotation = method.getAnnotation(UpdateMultiCache.class);
        final AnnotationData data = AnnotationDataBuilder.buildAnnotationData(annotation, UpdateMultiCache.class, "cacheMe01");

        final List<String> keys = new ArrayList<String>();
        final List<Object> objs = new ArrayList<Object>();
        keys.add("Key1-" + System.currentTimeMillis());
        keys.add("Key2-" + System.currentTimeMillis());

        try {
            cut.updateCache(keys, objs, method, data);
            fail("Expected Exception.");
        } catch (InvalidAnnotationException ex) {
            assertTrue(ex.getMessage().contains("do not match in size"));
        }

        final MemcachedClientIF cache = EasyMock.createMock(MemcachedClientIF.class);
        cut.setCache(cache);

        for (final String key : keys) {
            final String value = "ValueFor-" + key;
            objs.add(value);
            EasyMock.expect(cache.set(key, data.getExpiration(), value)).andReturn(null);
        }
        keys.add("BigFatNull");
        objs.add(null);
        EasyMock.expect(cache.set(keys.get(2), data.getExpiration(), new PertinentNegativeNull())).andReturn(null);

        EasyMock.replay(cache);

        cut.updateCache(keys, objs, method, data);

        EasyMock.verify(cache);

    }

    static class AnnotationTest {
		@UpdateMultiCache(namespace = "Bubba", expiration = 300, keyIndex = 0, dataIndex = 0)
		public void cacheMe01() {}

		public String cacheMe02() { return null; }

		public ArrayList<String> cacheMe03() { return null; }
	}
}
