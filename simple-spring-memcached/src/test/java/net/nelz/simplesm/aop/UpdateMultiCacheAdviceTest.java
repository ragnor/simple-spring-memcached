package net.nelz.simplesm.aop;

import net.nelz.simplesm.annotations.*;
import net.nelz.simplesm.exceptions.*;
import org.apache.commons.lang.*;
import static org.testng.AssertJUnit.*;
import org.testng.annotations.*;

import java.lang.reflect.*;
import java.util.*;

/**
 * Copyright 2008 Widgetbox, Inc.
 * All rights reserved.
 * THIS PROGRAM IS CONFIDENTIAL AND AN UNPUBLISHED WORK AND TRADE
 * SECRET OF THE COPYRIGHT HOLDER, AND DISTRIBUTED ONLY UNDER RESTRICTION.
 */
public class UpdateMultiCacheAdviceTest {

	private UpdateMultiCacheAdvice cut;

	@BeforeClass
	public void beforeClass() {
		cut = new UpdateMultiCacheAdvice();
		cut.setMethodStore(new CacheKeyMethodStoreImpl());
	}

	@Test
	public void testAnnotation() throws Exception {
		final Method method = AnnotationTest.class.getMethod("cacheMe01", null);
		final UpdateMultiCache annotation = method.getAnnotation(UpdateMultiCache.class);
		cut.validateAnnotation(annotation, method);
	}

	@Test
	public void testGetCacheKeys() throws Exception {
		final int size = 10;
		final List<Object> sources= new ArrayList<Object>();
		for (int ix = 0; ix < size; ix++) {
			sources.add(RandomStringUtils.randomAlphanumeric(3 + ix));
		}

		final String namespace = RandomStringUtils.randomAlphabetic(20);

		final List<String> results = cut.getCacheKeys(sources, namespace);

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

	static class AnnotationTest {
		@UpdateMultiCache(namespace = "Bubba", expiration = 300, keyIndex = 0)
		public void cacheMe01() {}

		public String cacheMe02() { return null; }

		public ArrayList<String> cacheMe03() { return null; }
	}
}
