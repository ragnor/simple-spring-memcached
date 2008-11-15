package net.nelz.simplesm.aop;

import net.nelz.simplesm.annotations.*;
import net.nelz.simplesm.exceptions.*;
import org.apache.commons.lang.*;
import org.apache.commons.lang.math.*;
import static org.testng.AssertJUnit.*;
import org.testng.annotations.*;

import java.lang.reflect.*;
import java.security.*;
import java.util.*;

/**
Copyright (c) 2008  Nelson Carpentier

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
public class ReadThroughMultiCacheTest {
	private ReadThroughMultiCacheAdvice cut;
	private ReadThroughMultiCacheAdvice.MultiCacheCoordinator coord;


	@BeforeClass
	public void beforeClass() {
		cut = new ReadThroughMultiCacheAdvice();
	}

	@BeforeMethod
	public void beforeMethod() {
		coord = new ReadThroughMultiCacheAdvice.MultiCacheCoordinator();
	}

	@Test
	public void testAnnotationValidation() throws Exception {
		final AnnotationValidator testClass = new AnnotationValidator();
		Method method = null;
		ReadThroughMultiCache annotation = null;

		method = testClass.getClass().getMethod("cacheMe1",null);
		annotation = method.getAnnotation(ReadThroughMultiCache.class);
		try {
			cut.validateAnnotation(annotation, method);
			fail("Expected Exception.");
		} catch (InvalidParameterException ex) {
			System.out.println(ex.getMessage());
			assertTrue(ex.getMessage().indexOf("KeyIndex") != -1);
		}

		method = testClass.getClass().getMethod("cacheMe2",null);
		annotation = method.getAnnotation(ReadThroughMultiCache.class);
		try {
			cut.validateAnnotation(annotation, method);
			fail("Expected Exception.");
		} catch (InvalidParameterException ex) {
			System.out.println(ex.getMessage());
			assertTrue(ex.getMessage().indexOf("Namespace") != -1);
		}

		method = testClass.getClass().getMethod("cacheMe3",null);
		annotation = method.getAnnotation(ReadThroughMultiCache.class);
		try {
			cut.validateAnnotation(annotation, method);
			fail("Expected Exception.");
		} catch (InvalidParameterException ex) {
			System.out.println(ex.getMessage());
			assertTrue(ex.getMessage().indexOf("Namespace") != -1);
		}


		method = testClass.getClass().getMethod("cacheMe4",null);
		annotation = method.getAnnotation(ReadThroughMultiCache.class);
		try {
			cut.validateAnnotation(annotation, method);
			fail("Expected Exception.");
		} catch (InvalidParameterException ex) {
			System.out.println(ex.getMessage());
			assertTrue(ex.getMessage().indexOf("Expiration") != -1);
		}
	}

	@Test
	public void testReturnTypeChecking() throws Exception {
		Method method = null;

		method = ReturnTypeCheck.class.getMethod("checkA", null);
		cut.verifyReturnTypeIsList(method);

		method = ReturnTypeCheck.class.getMethod("checkB", null);
		cut.verifyReturnTypeIsList(method);

		method = ReturnTypeCheck.class.getMethod("checkC", null);
		cut.verifyReturnTypeIsList(method);

		method = ReturnTypeCheck.class.getMethod("checkD", null);
		cut.verifyReturnTypeIsList(method);

		try {
			method = ReturnTypeCheck.class.getMethod("checkE", null);
			cut.verifyReturnTypeIsList(method);
			fail("Expected Exception.");
		} catch (InvalidAnnotationException ex) {
			assertTrue(ex.getMessage().indexOf("requirement") != -1);
		}
	}

	@Test
	public void testConvertIdObject() throws Exception {
		final String namespace = RandomStringUtils.randomAlphanumeric(6);
		final Map<String, Object> expectedString2Object = new HashMap<String, Object>();
		final Map<Object, String> expectedObject2String = new HashMap<Object, String>();
		final List<Object> idObjects = new ArrayList<Object>();
		final int length = 10;
		for (int ix = 0; ix < length; ix++) {
			final String object = RandomStringUtils.randomAlphanumeric(2 + ix);
			final String key = cut.buildCacheKey(object, namespace);
			idObjects.add(object);
			expectedObject2String.put(object, key);
			expectedString2Object.put(key, object);
		}

		cut.setMethodStore(new CacheKeyMethodStoreImpl());
		final List<Object> exceptionObjects = new ArrayList<Object>(idObjects);
		exceptionObjects.add(null);
		try {
			cut.convertIdObjectsToKeyMap(exceptionObjects, namespace);
			fail("Expected Exception");
		} catch (InvalidParameterException ex) { }

		for (int ix = 0; ix < length; ix++) {
			if (ix % 2 == 0) {
				idObjects.add(idObjects.get(ix));
			}
		}
		assertTrue(idObjects.size() > length);

		final ReadThroughMultiCacheAdvice.MapHolder holder = cut.convertIdObjectsToKeyMap(idObjects, namespace);

		assertEquals(length, holder.getKey2Obj().size());
		assertEquals(length, holder.getObj2Key().size());

		assertEquals(expectedObject2String, holder.getObj2Key());
		assertEquals(expectedString2Object, holder.getKey2Obj());

		coord.setHolder(holder);

		assertEquals(expectedObject2String, coord.getObj2Key());
		assertEquals(expectedString2Object, coord.getKey2Obj());

	}

	@Test
	public void testInitialKey2Result() {
		final String namespace = RandomStringUtils.randomAlphanumeric(6);
		final Map<String, Object> expectedString2Object = new HashMap<String, Object>();
		final Map<String, Object> key2Result = new HashMap<String, Object>();
		final Set<Object> missObjects = new HashSet<Object>();
		final int length = 15;
		for (int ix = 0; ix < length; ix++) {
			final String object = RandomStringUtils.randomAlphanumeric(2 + ix);
			final String key = cut.buildCacheKey(object, namespace);
			expectedString2Object.put(key, object);

			// There are 3 possible outcomes when fetching by key from memcached:
			// 0) You hit, and the key & result are in the map
			// 1) You hit, but the result is null, which counts as a miss.
			// 2) You miss, and the key doesn't even get into the result map.
			final int option = RandomUtils.nextInt(3);
			if (option == 0) {
				key2Result.put(key, key + RandomStringUtils.randomNumeric(5));
			}
			if (option == 1) {
				key2Result.put(key, null);
				missObjects.add(object);
			}
			if (option == 2) {
				missObjects.add(object);
			}
		}

		try {
			coord.setInitialKey2Result(null);
			fail("Expected Exception.");
		} catch (RuntimeException ex) {}

		coord.getKey2Obj().putAll(expectedString2Object);

		coord.setInitialKey2Result(key2Result);

		assertTrue(coord.getMissObjects().containsAll(missObjects));
		assertTrue(missObjects.containsAll(coord.getMissObjects()));

	}

	private static class AnnotationValidator {
		@ReadThroughMultiCache(keyIndex = -1, namespace = "bubba")
		public String cacheMe1() { return null; }
		@ReadThroughMultiCache(keyIndex = 0, namespace = "")
		public String cacheMe2() { return null; }
		@ReadThroughMultiCache(keyIndex = 0)
		public String cacheMe3() { return null; }
		@ReadThroughMultiCache(keyIndex = 0, namespace = "bubba", expiration = -1)
		public String cacheMe4() { return null; }
	}

	private static class ReturnTypeCheck {
		@ReadThroughMultiCache(keyIndex = 0, namespace = "bubba", expiration = 10)
		public List checkA() {return null;}
		@ReadThroughMultiCache(keyIndex = 0, namespace = "bubba", expiration = 10)
		public List<String> checkB() {return null;}
		@ReadThroughMultiCache(keyIndex = 0, namespace = "bubba", expiration = 10)
		public ArrayList checkC() {return null;}
		@ReadThroughMultiCache(keyIndex = 0, namespace = "bubba", expiration = 10)
		public ArrayList<String> checkD() {return null;}
		@ReadThroughMultiCache(keyIndex = 0, namespace = "bubba", expiration = 10)
		public String checkE() {return null;}
	}
}
