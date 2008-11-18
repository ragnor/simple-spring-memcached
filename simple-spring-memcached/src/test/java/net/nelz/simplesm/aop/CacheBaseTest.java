package net.nelz.simplesm.aop;

import net.nelz.simplesm.annotations.*;
import net.nelz.simplesm.exceptions.*;
import org.apache.commons.lang.*;
import static org.testng.AssertJUnit.*;
import org.testng.annotations.*;

import java.security.*;
import java.lang.reflect.*;

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
public class CacheBaseTest {
	private CacheBase cut;

	@BeforeClass
	public void beforeClass() {
		cut = new CacheBase();

		cut.setMethodStore(new CacheKeyMethodStoreImpl());
	}

	@Test
	public void testKeyMethodArgs() throws Exception {
		try {
			cut.getKeyMethod(new KeyObject01());
			fail("Expected exception.");
		} catch (InvalidAnnotationException ex) {
			assertTrue(ex.getMessage().indexOf("0 arguments") != -1);
			System.out.println(ex.getMessage());
		}

		try {
			cut.getKeyMethod(new KeyObject02());
			fail("Expected exception.");
		} catch (InvalidAnnotationException ex) {
			assertTrue(ex.getMessage().indexOf("String") != -1);
			System.out.println(ex.getMessage());
		}

		try {
			cut.getKeyMethod(new KeyObject03());
			fail("Expected exception.");
		} catch (InvalidAnnotationException ex) {
			assertTrue(ex.getMessage().indexOf("String") != -1);
			System.out.println(ex.getMessage());
		}

		try {
			cut.getKeyMethod(new KeyObject04());
			fail("Expected exception.");
		} catch (InvalidAnnotationException ex) {
			assertTrue(ex.getMessage().indexOf("only one method") != -1);
			System.out.println(ex.getMessage());
		}

		assertEquals("doIt", cut.getKeyMethod(new KeyObject05()).getName());
		assertEquals("toString", cut.getKeyMethod(new KeyObject06(null)).getName());
	}

	@Test
	public void testBuildCacheKey() {
		try {
			cut.buildCacheKey(null, null);
			fail("Expected exception.");
		} catch (InvalidParameterException ex) {
			assertTrue(ex.getMessage().indexOf("at least 1 character") != -1);
			System.out.println(ex.getMessage());
		}

		try {
			cut.buildCacheKey("", null);
			fail("Expected exception.");
		} catch (InvalidParameterException ex) {
			assertTrue(ex.getMessage().indexOf("at least 1 character") != -1);
			System.out.println(ex.getMessage());
		}

		try {
			cut.buildCacheKey("a", null);
			fail("Expected exception.");
		} catch (InvalidParameterException ex) {
			assertTrue(ex.getMessage().indexOf("at least 1 character") != -1);
			System.out.println(ex.getMessage());
		}

		try {
			cut.buildCacheKey("a", "");
			fail("Expected exception.");
		} catch (InvalidParameterException ex) {
			assertTrue(ex.getMessage().indexOf("at least 1 character") != -1);
			System.out.println(ex.getMessage());
		}

		final String objectId = RandomStringUtils.randomAlphanumeric(20);
		final String namespace = RandomStringUtils.randomAlphanumeric(12);

		final String result = cut.buildCacheKey(objectId, namespace);

		assertTrue(result.indexOf(objectId) != -1);
		assertTrue(result.indexOf(namespace) != -1);
	}

	@Test
	public void testGenerateCacheKey() throws Exception {
		final Method method = KeyObject.class.getMethod("toString", null);

		try {
			cut.generateObjectId(method, new KeyObject(null));
			fail("Expected Exception.");
		} catch (RuntimeException ex) {
			assertTrue(ex.getMessage().indexOf("empty key value") != -1);
		}

		try {
			cut.generateObjectId(method, new KeyObject(""));
			fail("Expected Exception.");
		} catch (RuntimeException ex) {
			assertTrue(ex.getMessage().indexOf("empty key value") != -1);
		}

		final String result = "momma";
		assertEquals(result, cut.generateObjectId(method, new KeyObject(result)));
	}

	@Test
	public void testValidateAnnotationExists() {
		// This is actually a very base !=null test, because there's no common class for annotations.
		cut.validateAnnotationExists("bubba", CacheKeyMethod.class);

		try {
			cut.validateAnnotationExists(null, CacheKeyMethod.class);
			fail("Expected Exception.");
		} catch (RuntimeException ex) {
			assertTrue(ex.getMessage().indexOf("CacheKeyMethod") != -1);
		}
	}

	@Test
	public void testValidateAnnotationIndex() throws Exception {
		cut.validateAnnotationIndex(0, false, null, null);
		cut.validateAnnotationIndex(-1, true, null, null);

		final Method method = KeyObject.class.getMethod("toString", null);

		try {
			cut.validateAnnotationIndex(-1, false, CacheKeyMethod.class, method);
			fail("Expected Exception.");
		} catch (RuntimeException ex) {
			assertTrue(ex.getMessage().indexOf("KeyIndex") != -1);
			assertTrue(ex.getMessage().indexOf("0") != -1);
			assertTrue(ex.getMessage().indexOf("CacheKeyMethod") != -1);
			assertTrue(ex.getMessage().indexOf("toString") != -1);
		}

		try {
			cut.validateAnnotationIndex(-2, true, CacheKeyMethod.class, method);
			fail("Expected Exception.");
		} catch (RuntimeException ex) {
			assertTrue(ex.getMessage().indexOf("KeyIndex") != -1);
			assertTrue(ex.getMessage().indexOf("-1") != -1);
			assertTrue(ex.getMessage().indexOf("CacheKeyMethod") != -1);
			assertTrue(ex.getMessage().indexOf("toString") != -1);
		}
	}

	@Test
	public void testValidateAnnotationNamespace() throws Exception {
		cut.validateAnnotationNamespace("Bubba", null, null);

		final Method method = KeyObject.class.getMethod("toString", null);
		try {
			cut.validateAnnotationNamespace(null, CacheKeyMethod.class, method);
			fail("Expected Exception.");
		} catch (RuntimeException ex) {
			assertTrue(ex.getMessage().indexOf("Namespace") != -1);
			assertTrue(ex.getMessage().indexOf("CacheKeyMethod") != -1);
			assertTrue(ex.getMessage().indexOf("toString") != -1);
		}

		try {
			cut.validateAnnotationNamespace(AnnotationConstants.DEFAULT_STRING, CacheKeyMethod.class, method);
			fail("Expected Exception.");
		} catch (RuntimeException ex) {
			assertTrue(ex.getMessage().indexOf("Namespace") != -1);
			assertTrue(ex.getMessage().indexOf("CacheKeyMethod") != -1);
			assertTrue(ex.getMessage().indexOf("toString") != -1);
		}

		try {
			cut.validateAnnotationNamespace("", CacheKeyMethod.class, method);
			fail("Expected Exception.");
		} catch (RuntimeException ex) {
			assertTrue(ex.getMessage().indexOf("Namespace") != -1);
			assertTrue(ex.getMessage().indexOf("CacheKeyMethod") != -1);
			assertTrue(ex.getMessage().indexOf("toString") != -1);
		}
	}

	@Test
	public void testValidateAnnotationExpiration() throws Exception {
		cut.validateAnnotationExpiration(0, null, null);
		cut.validateAnnotationExpiration(10, null, null);

		final Method method = KeyObject.class.getMethod("toString", null);
		try {
			cut.validateAnnotationExpiration(-1, CacheKeyMethod.class, method);
			fail("Expected Exception.");
		} catch (RuntimeException ex) {
			assertTrue(ex.getMessage().indexOf("Expiration") != -1);
			assertTrue(ex.getMessage().indexOf("CacheKeyMethod") != -1);
			assertTrue(ex.getMessage().indexOf("toString") != -1);
		}
	}

	private static class KeyObject {
		private String result;
		private KeyObject(String result) { this.result = result;}
		public String toString() { return result; }
	}

	private static class KeyObject01 {
		@CacheKeyMethod
		public void doIt(final String nonsense) { }
	}

	private static class KeyObject02 {
		@CacheKeyMethod
		public void doIt() { }
	}

	private static class KeyObject03 {
		@CacheKeyMethod
		public Long doIt() { return null; }
	}

	private static class KeyObject04 {
		@CacheKeyMethod
		public String doIt() { return null; }
		@CacheKeyMethod
		public String doItAgain() { return null; }
	}

	private static class KeyObject05 {
		public static final String result = "shrimp";
		@CacheKeyMethod
		public String doIt() { return result; }
	}

	private static class KeyObject06 {
		private String result;
		private KeyObject06(String result) { this.result = result;}
		public String toString() { return result; }
	}
}
