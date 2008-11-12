package net.nelz.simplesm.aop;

import net.nelz.simplesm.annotations.*;
import net.nelz.simplesm.exceptions.*;
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

	@BeforeClass
	public void beforeClass() {
		cut = new ReadThroughMultiCacheAdvice();
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
