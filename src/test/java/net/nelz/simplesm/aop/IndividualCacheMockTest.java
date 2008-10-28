package net.nelz.simplesm.aop;

import net.nelz.simplesm.annotations.*;
import net.spy.memcached.*;
import org.aspectj.lang.*;
import org.aspectj.lang.reflect.*;
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
public class IndividualCacheMockTest {

	private IndividualCache cut;
	private ProceedingJoinPoint pjp;
	private MemcachedClientIF cache;
	private MethodSignature sig;

	@BeforeClass
	public void beforeClass() {
		cut = new IndividualCache();

		pjp = createMock(ProceedingJoinPoint.class);
		cache = createMock(MemcachedClientIF.class);
		sig = createMock(MethodSignature.class);

		cut.setCache(cache);
	}

	@BeforeMethod
	public void beforeMethod() {
		reset(pjp);
		reset(cache);
		reset(sig);
	}

	public void replayAll() {
		replay(pjp);
		replay(cache);
		replay(sig);
	}

	public void verifyAll() {
		verify(pjp);
		verify(cache);
		verify(sig);
	}

	@Test
	public void testKeyObject() throws Exception {
		final String answer = "bubba";
		final Object[] args = new Object[] {null, answer, "blue"};
		expect(pjp.getArgs()).andReturn(args).times(4);

		final Method method = AOPTargetClass1.class.getDeclaredMethod("doIt", String.class, String.class, String.class);

		replayAll();

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

	@Test
	public void testGetObjectId() throws Exception {
		final Method methodToCache = AOPTargetClass2.class.getDeclaredMethod("cacheThis", AOPKeyClass.class);
		expect(pjp.getArgs()).andReturn(new Object[] {new AOPKeyClass()});

		replayAll();

		final String result = cut.getObjectId(0, pjp, methodToCache);

		verifyAll();

		assertEquals(AOPKeyClass.result, result);
	}

	@Test
	public void testTopLevelCacheIndividualCacheHit() throws Throwable {
		final Method methodToCache = AOPTargetClass2.class.getDeclaredMethod("cacheThis", AOPKeyClass.class);
		expect(pjp.getSignature()).andReturn(sig);
		expect(sig.getMethod()).andReturn(methodToCache);
		expect(pjp.getArgs()).andReturn(new Object[] {new AOPKeyClass()});
		expect(pjp.toShortString()).andReturn("SHORTSTRING").anyTimes();
		final String cachedResult = "A VALUE FROM THE CACHE";
		expect(cache.get("BUBBA:" + AOPKeyClass.result)).andReturn(cachedResult);

		replayAll();

		final String result = (String) cut.cacheIndividual(pjp);

		verifyAll();
		assertEquals(cachedResult, result);
	}

	@Test
	public void testTopLevelCacheIndividualCacheHitNull() throws Throwable {
		final Method methodToCache = AOPTargetClass2.class.getDeclaredMethod("cacheThis", AOPKeyClass.class);
		expect(pjp.getSignature()).andReturn(sig);
		expect(sig.getMethod()).andReturn(methodToCache);
		expect(pjp.getArgs()).andReturn(new Object[] {new AOPKeyClass()});
		expect(pjp.toShortString()).andReturn("SHORTSTRING").anyTimes();
		expect(cache.get("BUBBA:" + AOPKeyClass.result)).andReturn(new PertinentNegativeNull());

		replayAll();

		final String result = (String) cut.cacheIndividual(pjp);

		verifyAll();
		assertNull(result);
	}

	@Test
	public void testTopLevelCacheIndividualCachePreException() throws Throwable {
		expect(pjp.toShortString()).andReturn("SHORTSTRING").anyTimes();
		expect(pjp.getSignature()).andThrow(new RuntimeException("FORCE FOR TEST"));
		final String targetResult = "A VALUE FROM THE TARGET OBJECT";
		expect(pjp.proceed()).andReturn(targetResult);

		replayAll();

		final String result = (String) cut.cacheIndividual(pjp);

		verifyAll();
		assertEquals(targetResult, result);
	}

	@Test
	public void testTopLevelCacheIndividualCacheMissWithData() throws Throwable {
		final Method methodToCache = AOPTargetClass2.class.getDeclaredMethod("cacheThis", AOPKeyClass.class);
		expect(pjp.getSignature()).andReturn(sig);
		expect(sig.getMethod()).andReturn(methodToCache);
		expect(pjp.getArgs()).andReturn(new Object[] {new AOPKeyClass()});
		expect(pjp.toShortString()).andReturn("SHORTSTRING").anyTimes();
		final String cacheKey = "BUBBA:" + AOPKeyClass.result;
		final String targetResult = "A VALUE FROM THE CACHE";
		expect(cache.get(cacheKey)).andReturn(null);
		expect(pjp.proceed()).andReturn(targetResult);
		expect(cache.set(cacheKey, 3600, targetResult)).andReturn(null);

		replayAll();

		final String result = (String) cut.cacheIndividual(pjp);

		verifyAll();
		assertEquals(targetResult, result);
	}

	@Test
	public void testTopLevelCacheIndividualCacheMissWithNull() throws Throwable {
		final Method methodToCache = AOPTargetClass2.class.getDeclaredMethod("cacheThis", AOPKeyClass.class);
		expect(pjp.getSignature()).andReturn(sig);
		expect(sig.getMethod()).andReturn(methodToCache);
		expect(pjp.getArgs()).andReturn(new Object[] {new AOPKeyClass()});
		expect(pjp.toShortString()).andReturn("SHORTSTRING").anyTimes();
		final String cacheKey = "BUBBA:" + AOPKeyClass.result;
		expect(cache.get(cacheKey)).andReturn(null);
		expect(pjp.proceed()).andReturn(null);
		expect(cache.set(cacheKey, 3600, new PertinentNegativeNull())).andReturn(null);

		replayAll();

		final String result = (String) cut.cacheIndividual(pjp);

		verifyAll();
		assertNull(result);
	}

	private static class AOPTargetClass1 {
		public String doIt(final String s1, final String s2, final String s3) { return null; }
	}

	private static class AOPTargetClass2 {
		@SSMIndividual(namespace = "BUBBA", keyIndex = 0, expiration = 3600)
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
