package net.nelz.simplesm.aop;

import net.nelz.simplesm.annotations.*;
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
public class ReadThroughSingleCacheTest {
	private ReadThroughSingleCacheAdvice cut;

	@BeforeClass
	public void beforeClass() {
		cut = new ReadThroughSingleCacheAdvice();
	}

	@Test
	public void testAnnotationValidation() throws Exception {
		final AnnotationValidator testClass = new AnnotationValidator();
		Method method = null;
		ReadThroughSingleCache annotation = null;

		method = testClass.getClass().getMethod("cacheMe1",null);
		annotation = method.getAnnotation(ReadThroughSingleCache.class);
		try {
			cut.validateAnnotation(annotation, method);
			fail("Expected Exception.");
		} catch (InvalidParameterException ex) {
			System.out.println(ex.getMessage());
			assertTrue(ex.getMessage().indexOf("KeyIndex") != -1);
		}

		method = testClass.getClass().getMethod("cacheMe2",null);
		annotation = method.getAnnotation(ReadThroughSingleCache.class);
		try {
			cut.validateAnnotation(annotation, method);
			fail("Expected Exception.");
		} catch (InvalidParameterException ex) {
			System.out.println(ex.getMessage());
			assertTrue(ex.getMessage().indexOf("Namespace") != -1);
		}

		method = testClass.getClass().getMethod("cacheMe3",null);
		annotation = method.getAnnotation(ReadThroughSingleCache.class);
		try {
			cut.validateAnnotation(annotation, method);
			fail("Expected Exception.");
		} catch (InvalidParameterException ex) {
			System.out.println(ex.getMessage());
			assertTrue(ex.getMessage().indexOf("Namespace") != -1);
		}


		method = testClass.getClass().getMethod("cacheMe4",null);
		annotation = method.getAnnotation(ReadThroughSingleCache.class);
		try {
			cut.validateAnnotation(annotation, method);
			fail("Expected Exception.");
		} catch (InvalidParameterException ex) {
			System.out.println(ex.getMessage());
			assertTrue(ex.getMessage().indexOf("Expiration") != -1);
		}
	}

	private static class AnnotationValidator {
		@ReadThroughSingleCache(keyIndex = -1, namespace = "bubba")
		public String cacheMe1() { return null; }
		@ReadThroughSingleCache(keyIndex = 0, namespace = "")
		public String cacheMe2() { return null; }
		@ReadThroughSingleCache(keyIndex = 0)
		public String cacheMe3() { return null; }
		@ReadThroughSingleCache(keyIndex = 0, namespace = "bubba", expiration = -1)
		public String cacheMe4() { return null; }
	}

}
