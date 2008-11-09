package net.nelz.simplesm.aop;

import org.testng.annotations.*;
import static org.testng.AssertJUnit.*;
import net.nelz.simplesm.exceptions.*;
import net.nelz.simplesm.annotations.*;

/**
 * Copyright 2008 Widgetbox, Inc.
 * All rights reserved.
 * THIS PROGRAM IS CONFIDENTIAL AND AN UNPUBLISHED WORK AND TRADE
 * SECRET OF THE COPYRIGHT HOLDER, AND DISTRIBUTED ONLY UNDER RESTRICTION.
 */
public class CacheBaseTest {
	private CacheBase cut;

	@BeforeClass
	public void beforeClass() {
		cut = new CacheBase();
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
