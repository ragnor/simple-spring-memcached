package net.nelz.simplesm.aop;

import net.nelz.simplesm.annotations.*;
import org.aspectj.lang.*;
import static org.easymock.EasyMock.*;
import static org.testng.AssertJUnit.*;
import org.testng.annotations.*;

import java.lang.reflect.*;
import java.security.*;

/**
 * Copyright 2008 Widgetbox, Inc.
 * All rights reserved.
 * THIS PROGRAM IS CONFIDENTIAL AND AN UNPUBLISHED WORK AND TRADE
 * SECRET OF THE COPYRIGHT HOLDER, AND DISTRIBUTED ONLY UNDER RESTRICTION.
 */
public class IndividualCacheObjectIdTest {

	private IndividualCache cut;
	private ProceedingJoinPoint pjp;

	@BeforeClass
	public void beforeClass() {
		cut = new IndividualCache();

		pjp = createMock(ProceedingJoinPoint.class);
	}

	@BeforeMethod
	public void beforeMethod() {
		reset(pjp);
	}

	@Test
	public void testKeyObject() throws Exception {
		final String answer = "bubba";
		final Object[] args = new Object[] {null, answer, "blue"};
		expect(pjp.getArgs()).andReturn(args).times(4);

		final Method method = AOPTargetClass1.class.getDeclaredMethod("doIt", String.class, String.class, String.class);
		replay(pjp);

		try {
			cut.getKeyObject(3, pjp, method);
			fail("Expected Exception");
		} catch (InvalidParameterException ex) {
			assertTrue(ex.getMessage().indexOf("too big") != -1);
			System.out.println(ex.getMessage());
		}
		try {
			cut.getKeyObject(4, pjp, method);
			fail("Expected Exception");
		} catch (InvalidParameterException ex) {
			assertTrue(ex.getMessage().indexOf("too big") != -1);
			System.out.println(ex.getMessage());
		}

		try {
			cut.getKeyObject(0, pjp, method);
			fail("Expected Exception");
		} catch (InvalidParameterException ex) {
			assertTrue(ex.getMessage().indexOf("null") != -1);
			System.out.println(ex.getMessage());
		}

		assertEquals(answer, cut.getKeyObject(1, pjp, method));

		verify(pjp);
	}

	@Test
	public void testGetObjectId() throws Exception {
		final Method methodToCache = AOPTargetClass2.class.getDeclaredMethod("cacheThis", AOPKeyClass.class);
		expect(pjp.getArgs()).andReturn(new Object[] {new AOPKeyClass()});

		replay(pjp);

		final String result = cut.getObjectId(0, pjp, methodToCache);

		verify(pjp);

		assertEquals(AOPKeyClass.result, result);
	}

	private static class AOPTargetClass1 {
		public String doIt(final String s1, final String s2, final String s3) { return null; }
	}

	private static class AOPTargetClass2 {
		public String cacheThis(final AOPKeyClass p1) {
			throw new RuntimeException("Forced.");
		}
	}

	private static class AOPKeyClass {
		public static final String result = "CACHE KEY";
		@SSMCacheKeyMethod
		public String getKey() {
			return result;
		}
	}
}
