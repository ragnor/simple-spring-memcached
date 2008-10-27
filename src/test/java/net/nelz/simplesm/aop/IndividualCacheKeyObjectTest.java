package net.nelz.simplesm.aop;

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
public class IndividualCacheKeyObjectTest {

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

	private void verifyAll() {
		verify(pjp);
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

		verifyAll();
	}

	private static class AOPTargetClass1 {
		public String doIt(final String s1, final String s2, final String s3) { return null; }
	}
}
