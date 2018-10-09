/* Copyright (c) 2014-2018 Jakub Białek
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static com.google.code.ssm.test.Matcher.any;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import com.google.code.ssm.Cache;
import com.google.code.ssm.aop.support.AnnotationData;
import com.google.code.ssm.aop.support.PertinentNegativeNull;
import com.google.code.ssm.api.ParameterValueKeyProvider;
import com.google.code.ssm.api.ReadThroughMultiCache;
import com.google.code.ssm.api.ReadThroughMultiCacheOption;
import com.google.code.ssm.api.format.SerializationType;
import com.google.code.ssm.providers.CacheException;

/**
 * 
 * @author Jakub Białek
 * 
 */
@RunWith(MockitoJUnitRunner.class)
public class ReadThroughMultiCacheAdviceCoordTest {

    private static final String NS = "T1";
    private static final int EXPIRATION = 321;

    private final List<String> expected = Arrays.asList("a", "b");
    private final Object[] args = new Object[] { Arrays.asList(1, 2) };
    private final List<String> cacheKeys = Arrays.asList(NS + ":" + 1, NS + ":" + 2);

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private CacheBase cacheBase;

    @Mock
    private ProceedingJoinPoint pjp;

    @Mock
    private ReadThroughMultiCache annotation;

    @Mock
    private Cache cache;

    @InjectMocks
    private ReadThroughMultiCacheAdvice advice = new ReadThroughMultiCacheAdvice();

    @Test
    public void shouldExecuteMethodAndNotModifyArgsIfAllMiss() throws Throwable {
        final Method methodToCache = TestService.class.getMethod("getList", List.class);

        initMocks(methodToCache, Collections.<String, Object> emptyMap());
        when(pjp.proceed(args)).thenReturn(expected);

        final Object result = advice.cacheMulti(pjp);

        assertNotNull(result);
        assertEquals(expected, result);
        verify(pjp).proceed(args);
        verify(cache).getBulk(eq(new HashSet<String>(cacheKeys)), any(SerializationType.class));
        for (int i = 0; i < expected.size(); i++) {
            verify(cache).setSilently(cacheKeys.get(i), EXPIRATION, expected.get(i), null);
        }
    }

    @Test
    public void shouldExecuteMethodAndModifyArgsIfSomeMiss() throws Throwable {
        final Method methodToCache = TestService.class.getMethod("getList", List.class);
        final Object[] modifiedArgs = new Object[] { Arrays.asList(2) };
        final Map<String, Object> cacheResponse = Collections.<String, Object> singletonMap(cacheKeys.get(0), expected.get(0));

        initMocks(methodToCache, cacheResponse);
        when(pjp.proceed(modifiedArgs)).thenReturn(Collections.singletonList("b"));

        final Object result = advice.cacheMulti(pjp);

        assertNotNull(result);
        assertEquals(expected, result);
        verify(pjp).proceed(modifiedArgs);
        verify(cache).getBulk(eq(new HashSet<String>(cacheKeys)), any(SerializationType.class));
        verify(cache).setSilently(NS + ":" + 2, EXPIRATION, "b", null);
    }

    @Test
    public void shouldNotExecuteMethodIfAllHits() throws Throwable {
        final Method methodToCache = TestService.class.getMethod("getList", List.class);
        final Map<String, Object> cacheResponse = new HashMap<String, Object>();
        cacheResponse.put(cacheKeys.get(0), expected.get(0));
        cacheResponse.put(cacheKeys.get(1), expected.get(1));

        initMocks(methodToCache, cacheResponse);

        final Object result = advice.cacheMulti(pjp);

        assertNotNull(result);
        assertEquals(expected, result);
        verify(cache).getBulk(eq(new HashSet<String>(cacheKeys)), any(SerializationType.class));
        verify(pjp, never()).proceed(any(Object[].class));
        verify(cache, never()).setSilently(anyString(), anyInt(), any(), any(SerializationType.class));
    }

    @Test
    public void shouldNotAddNullsToCache() throws Throwable {
        final Method methodToCache = TestService.class.getMethod("getList", List.class);
        final List<String> expected = Collections.emptyList();

        initMocks(methodToCache, Collections.<String, Object> emptyMap());
        when(pjp.proceed(args)).thenReturn(expected);

        final Object result = advice.cacheMulti(pjp);

        assertNotNull(result);
        assertEquals(expected, result);
        verify(pjp).proceed(args);
        verify(cache).getBulk(eq(new HashSet<String>(cacheKeys)), any(SerializationType.class));
        verify(cache, never()).setSilently(anyString(), anyInt(), any(), any(SerializationType.class));
        verify(cache, never()).addSilently(anyString(), anyInt(), any(), any(SerializationType.class));
    }

    @Test
    public void shouldAddNullsToCache() throws Throwable {
        final Method methodToCache = TestService.class.getMethod("getListCacheNulls", List.class);
        final List<String> expected = Collections.emptyList();

        initMocks(methodToCache, Collections.<String, Object> emptyMap());
        when(pjp.proceed(args)).thenReturn(expected);

        final Object result = advice.cacheMulti(pjp);

        assertNotNull(result);
        assertEquals(expected, result);
        verify(pjp).proceed(args);
        verify(cache).getBulk(eq(new HashSet<String>(cacheKeys)), any(SerializationType.class));
        verify(cache, never()).setSilently(anyString(), anyInt(), any(), any(SerializationType.class));
        verify(cache).addSilently(eq(cacheKeys.get(0)), eq(EXPIRATION), eq(PertinentNegativeNull.NULL), any(SerializationType.class));
        verify(cache).addSilently(eq(cacheKeys.get(1)), eq(EXPIRATION), eq(PertinentNegativeNull.NULL), any(SerializationType.class));
    }

    private void initMocks(final Method methodToCache, final Map<String, Object> cacheResponse) throws NoSuchMethodException,
            TimeoutException, CacheException {
        when(pjp.getArgs()).thenReturn(args);

        when(cacheBase.getMethodToCache(pjp, ReadThroughMultiCache.class)).thenReturn(methodToCache);
        when(cacheBase.getCache(any(AnnotationData.class))).thenReturn(cache);
        when(cacheBase.getSubmission(any())).thenAnswer(new Answer<Object>() {

            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                return (invocation.getArguments()[0] == null) ? PertinentNegativeNull.NULL : invocation.getArguments()[0];
            }

        });
        when(cacheBase.getCacheKeyBuilder().getCacheKeys(any(AnnotationData.class), eq(args), eq(methodToCache.toString()))).thenReturn(
                cacheKeys);
        when(cache.getBulk(eq(new HashSet<String>(cacheKeys)), any(SerializationType.class))).thenReturn(cacheResponse);
    }

    private static class TestService {

        @ReadThroughMultiCache(namespace = NS, expiration = EXPIRATION)
        public List<String> getList(@ParameterValueKeyProvider final List<Integer> id1) {
            return Collections.<String> emptyList();
        }

        @ReadThroughMultiCache(namespace = NS, expiration = EXPIRATION, option = @ReadThroughMultiCacheOption(addNullsToCache = true))
        public List<String> getListCacheNulls(@ParameterValueKeyProvider final List<Integer> id1) {
            return Collections.<String> emptyList();
        }

    }

}
