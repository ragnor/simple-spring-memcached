package net.nelz.simplesm.aop;

import net.nelz.simplesm.annotations.*;
import net.nelz.simplesm.exceptions.*;
import static org.testng.AssertJUnit.*;
import org.testng.annotations.*;

import java.lang.reflect.*;
import java.security.*;
import java.util.*;

/**
 * Copyright 2008 Widgetbox, Inc.
 * All rights reserved.
 * THIS PROGRAM IS CONFIDENTIAL AND AN UNPUBLISHED WORK AND TRADE
 * SECRET OF THE COPYRIGHT HOLDER, AND DISTRIBUTED ONLY UNDER RESTRICTION.
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
