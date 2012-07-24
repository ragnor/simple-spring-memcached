/*
 * Copyright (c) 2008-2009 Nelson Carpentier
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the
 * Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.google.code.ssm.aop;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.reset;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;

import java.util.Collections;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.code.ssm.Cache;
import com.google.code.ssm.api.CacheKeyMethod;

/**
 * 
 * @author Nelson Carpentier
 * 
 */
public class ReadThroughSingleCacheMockTest {

    private static ReadThroughSingleCacheAdvice cut;
    private static ProceedingJoinPoint pjp;
    private static Cache cache;
    private static MethodSignature sig;

    @BeforeClass
    public static void beforeClass() {
        cut = new ReadThroughSingleCacheAdvice();
        cut.setCacheBase(new CacheBase());

        pjp = createMock(ProceedingJoinPoint.class);
        cache = createMock(Cache.class);
        sig = createMock(MethodSignature.class);
    }

    @Before
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

    /*
     * @Test public void testGetObjectId() throws Exception { final Method methodToCache =
     * AOPTargetClass2.class.getDeclaredMethod("cacheThis", AOPKeyClass.class); expect(pjp.getArgs()).andReturn(new
     * Object[] { new AOPKeyClass() });
     * 
     * replayAll();
     * 
     * @SuppressWarnings("deprecation") final String result = cut.getObjectId(0, pjp, methodToCache);
     * 
     * verifyAll();
     * 
     * assertEquals(AOPKeyClass.result, result); }
     */
    // @Test
    // public void testTopLevelCacheIndividualCacheHit() throws Throwable {
    // final String methodName = "cacheThis";
    // expect(pjp.getSignature()).andReturn(sig);
    // expect(sig.getName()).andReturn(methodName);
    // expect(sig.getParameterTypes()).andReturn(new Class[] {AOPKeyClass.class});
    // expect(pjp.getTarget()).andReturn(new AOPTargetClass2());
    // expect(pjp.getArgs()).andReturn(new Object[] {new AOPKeyClass()});
    // expect(pjp.toShortString()).andReturn("SHORTSTRING").anyTimes();
    // final String cachedResult = "A VALUE FROM THE CACHE";
    // expect(cache.get("BUBBA:" + AOPKeyClass.result)).andReturn(cachedResult);
    //
    // replayAll();
    //
    // final String result = (String) cut.cacheGetSingle(pjp);
    //
    // verifyAll();
    // assertEquals(cachedResult, result);
    // }

    // @Test
    // public void testTopLevelCacheIndividualCacheHitNull() throws Throwable {
    // final String methodName = "cacheThis";
    // expect(pjp.getSignature()).andReturn(sig);
    // expect(sig.getName()).andReturn(methodName);
    // expect(sig.getParameterTypes()).andReturn(new Class[] {AOPKeyClass.class});
    // expect(pjp.getTarget()).andReturn(new AOPTargetClass2());
    // expect(pjp.getArgs()).andReturn(new Object[] {new AOPKeyClass()});
    // expect(pjp.toShortString()).andReturn("SHORTSTRING").anyTimes();
    // expect(cache.get("BUBBA:" + AOPKeyClass.result)).andReturn(new PertinentNegativeNull());
    //
    // replayAll();
    //
    // final String result = (String) cut.cacheGetSingle(pjp);
    //
    // verifyAll();
    // assertNull(result);
    // }

    @Test
    public void testTopLevelCacheIndividualCachePreException() throws Throwable {
        expect(pjp.toShortString()).andReturn("SHORTSTRING").anyTimes();
        expect(pjp.getSignature()).andThrow(new RuntimeException("FORCE FOR TEST"));
        final String targetResult = "A VALUE FROM THE TARGET OBJECT";
        expect(pjp.proceed()).andReturn(targetResult);

        expect(cache.getName()).andReturn("cache-0");
        expect(cache.getAliases()).andReturn(Collections.<String> emptyList()).anyTimes();

        replayAll();

        cut.getCacheBase().addCache(cache);
        final String result = (String) cut.cacheGetSingle(pjp);

        verifyAll();
        assertEquals(targetResult, result);
    }

    // @Test
    // public void testTopLevelCacheIndividualCacheMissWithData() throws Throwable {
    // final String methodName = "cacheThis";
    // expect(pjp.getSignature()).andReturn(sig);
    // expect(sig.getName()).andReturn(methodName);
    // expect(sig.getParameterTypes()).andReturn(new Class[] {AOPKeyClass.class});
    // expect(pjp.getTarget()).andReturn(new AOPTargetClass2());
    // expect(pjp.getArgs()).andReturn(new Object[] {new AOPKeyClass()});
    // expect(pjp.toShortString()).andReturn("SHORTSTRING").anyTimes();
    // final String cacheKey = "BUBBA:" + AOPKeyClass.result;
    // final String targetResult = "A VALUE FROM THE CACHE";
    // expect(cache.get(cacheKey)).andReturn(null);
    // expect(pjp.proceed()).andReturn(targetResult);
    // expect(cache.set(cacheKey, 3600, targetResult)).andReturn(null);
    //
    // replayAll();
    //
    // final String result = (String) cut.cacheGetSingle(pjp);
    //
    // verifyAll();
    // assertEquals(targetResult, result);
    // }

    // @Test
    // public void testTopLevelCacheIndividualCacheMissWithNull() throws Throwable {
    // final String methodName = "cacheThis";
    // expect(pjp.getSignature()).andReturn(sig);
    // expect(sig.getName()).andReturn(methodName);
    // expect(sig.getParameterTypes()).andReturn(new Class[] {AOPKeyClass.class});
    // expect(pjp.getTarget()).andReturn(new AOPTargetClass2());
    // expect(pjp.getArgs()).andReturn(new Object[] {new AOPKeyClass()});
    // expect(pjp.toShortString()).andReturn("SHORTSTRING").anyTimes();
    // final String cacheKey = "BUBBA:" + AOPKeyClass.result;
    // expect(cache.get(cacheKey)).andReturn(null);
    // expect(pjp.proceed()).andReturn(null);
    // expect(cache.set(cacheKey, 3600, new PertinentNegativeNull())).andReturn(null);
    //
    // replayAll();
    //
    // final String result = (String) cut.cacheGetSingle(pjp);
    //
    // verifyAll();
    // assertNull(result);
    // }

    public static class AOPTargetClass2 {

        @com.google.code.ssm.api.ReadThroughSingleCache(namespace = "BUBBA", expiration = 3600)
        public String cacheThis(final AOPKeyClass p1) {
            throw new RuntimeException("Forced.");
        }

    }

    public static class AOPKeyClass {

        public static final String result = "CACHE KEY";

        @CacheKeyMethod
        public String getKey() {
            return result;
        }

    }

}
