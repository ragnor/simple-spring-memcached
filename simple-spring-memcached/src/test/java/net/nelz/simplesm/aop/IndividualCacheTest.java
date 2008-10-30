package net.nelz.simplesm.aop;

import net.nelz.simplesm.annotations.*;
import net.nelz.simplesm.exceptions.*;
import static org.testng.AssertJUnit.*;
import org.testng.annotations.*;

import java.lang.reflect.*;
import java.security.*;

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
public class IndividualCacheTest {
	private IndividualCache cut;

	@BeforeClass
	public void beforeClass() {
		cut = new IndividualCache();
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
	public void testGenerateCacheKey() throws Exception {
		final Method method = KeyObject06.class.getMethod("toString", null);

		try {
			cut.generateObjectId(method, new KeyObject06(null));
			fail("Expected Exception.");
		} catch (RuntimeException ex) {
			assertTrue(ex.getMessage().indexOf("empty key value") != -1);
		}

		try {
			cut.generateObjectId(method, new KeyObject06(""));
			fail("Expected Exception.");
		} catch (RuntimeException ex) {
			assertTrue(ex.getMessage().indexOf("empty key value") != -1);
		}

		final String result = "momma";
		assertEquals(result, cut.generateObjectId(method, new KeyObject06(result)));
	}

	@Test
	public void testAnnotationValidation() throws Exception {
		final AnnotationValidator testClass = new AnnotationValidator();
		Method method = null;
		SSMIndividual annotation = null;

		method = testClass.getClass().getMethod("cacheMe1",null);
		annotation = method.getAnnotation(SSMIndividual.class);
		try {
			cut.validateAnnotation(annotation, method);
			fail("Expected Exception.");
		} catch (InvalidParameterException ex) {
			System.out.println(ex.getMessage());
			assertTrue(ex.getMessage().indexOf("KeyIndex") != -1);
		}

		method = testClass.getClass().getMethod("cacheMe2",null);
		annotation = method.getAnnotation(SSMIndividual.class);
		try {
			cut.validateAnnotation(annotation, method);
			fail("Expected Exception.");
		} catch (InvalidParameterException ex) {
			System.out.println(ex.getMessage());
			assertTrue(ex.getMessage().indexOf("Namespace") != -1);
		}

		method = testClass.getClass().getMethod("cacheMe3",null);
		annotation = method.getAnnotation(SSMIndividual.class);
		try {
			cut.validateAnnotation(annotation, method);
			fail("Expected Exception.");
		} catch (InvalidParameterException ex) {
			System.out.println(ex.getMessage());
			assertTrue(ex.getMessage().indexOf("Namespace") != -1);
		}


		method = testClass.getClass().getMethod("cacheMe4",null);
		annotation = method.getAnnotation(SSMIndividual.class);
		try {
			cut.validateAnnotation(annotation, method);
			fail("Expected Exception.");
		} catch (InvalidParameterException ex) {
			System.out.println(ex.getMessage());
			assertTrue(ex.getMessage().indexOf("Expiration") != -1);
		}
	}

	private static class KeyObject01 {
		@SSMCacheKeyMethod
		public void doIt(final String nonsense) { }
	}

	private static class KeyObject02 {
		@SSMCacheKeyMethod
		public void doIt() { }
	}

	private static class KeyObject03 {
		@SSMCacheKeyMethod
		public Long doIt() { return null; }
	}

	private static class KeyObject04 {
		@SSMCacheKeyMethod
		public String doIt() { return null; }
		@SSMCacheKeyMethod
		public String doItAgain() { return null; }
	}

	private static class KeyObject05 {
		public static final String result = "shrimp";
		@SSMCacheKeyMethod
		public String doIt() { return result; }
	}
	private static class KeyObject06 {
		private String result;
		private KeyObject06(String result) { this.result = result;}
		public String toString() { return result; }
	}
	private static class AnnotationValidator {
		@SSMIndividual(keyIndex = -1, namespace = "bubba")
		public String cacheMe1() { return null; }
		@SSMIndividual(keyIndex = 0, namespace = "")
		public String cacheMe2() { return null; }
		@SSMIndividual(keyIndex = 0)
		public String cacheMe3() { return null; }
		@SSMIndividual(keyIndex = 0, namespace = "bubba", expiration = -1)
		public String cacheMe4() { return null; }
	}

}
