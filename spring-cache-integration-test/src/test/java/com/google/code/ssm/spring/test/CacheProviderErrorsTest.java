/*
 * Copyright (c) 2014-2015 Jakub Białek
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

package com.google.code.ssm.spring.test;

import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;

import java.util.concurrent.TimeoutException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

import com.google.code.ssm.Cache;
import com.google.code.ssm.api.format.SerializationType;
import com.google.code.ssm.providers.CacheException;
import com.google.code.ssm.spring.test.dao.TestDAO;
import com.google.code.ssm.spring.test.service.TestService;

/**
 * 
 * @author Jakub Białek
 * @since 3.4.0
 * 
 */
@RunWith(SpringJUnit4ClassRunner.class)
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class })
@ContextConfiguration(locations = { "classpath*:simplesm-context.xml", "classpath*:test-application-context.xml" })
public class CacheProviderErrorsTest {

    @SuppressWarnings("rawtypes")
    private final Class[] cacheProvidersExceptionClasses = new Class[] { TimeoutException.class, CacheException.class,
            RuntimeException.class };

    private final long id = 145L;

    @Mock
    private TestDAO testDaoMock;

    @Autowired
    private TestService testService;

    @Autowired
    private Cache cacheMock;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        testService.setDao(testDaoMock);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testErrorsOnCacheable() throws TimeoutException, CacheException {
        final String value = "value to cache";
        for (Class<Throwable> exception : cacheProvidersExceptionClasses) {
            reset(testDaoMock, cacheMock);

            doThrow(exception).when(cacheMock).get(Long.toString(id), null);
            doReturn(value).when(testDaoMock).get(id);
            doThrow(exception).when(cacheMock).set(eq(Long.toString(id)), anyInt(), eq(value), isNull(SerializationType.class));

            testService.get(id);

            // check that intercepted method was invoked
            verify(testDaoMock).get(id);
            // check that cache was invoked
            verify(cacheMock).get(Long.toString(id), null);
            verify(cacheMock).set(eq(Long.toString(id)), anyInt(), eq(value), isNull(SerializationType.class));
        }
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testErrorsOnCacheEvict() throws TimeoutException, CacheException {
        for (Class<Throwable> exception : cacheProvidersExceptionClasses) {
            reset(testDaoMock, cacheMock);

            doThrow(exception).when(cacheMock).delete(Long.toString(id));

            testService.remove(id);

            // check that intercepted method was invoked
            verify(testDaoMock).remove(id);
            // check that cache was invoked
            verify(cacheMock).delete(Long.toString(id));
        }
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testErrorsOnCacheEvictWithBeforeInvocation() throws TimeoutException, CacheException {
        for (Class<Throwable> exception : cacheProvidersExceptionClasses) {
            reset(testDaoMock, cacheMock);

            doThrow(exception).when(cacheMock).delete(Long.toString(id));

            testService.removeClearCache(id);

            // check that intercepted method was invoked
            verify(testDaoMock).remove(id);
            // check that cache was invoked
            verify(cacheMock).delete(Long.toString(id));
        }
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testErrorsOnCachePut() throws TimeoutException, CacheException {
        final String value = "some test value to cache";
        for (Class<Throwable> exception : cacheProvidersExceptionClasses) {
            reset(testDaoMock, cacheMock);

            doThrow(exception).when(cacheMock).set(eq(Long.toString(id)), anyInt(), eq(value), isNull(SerializationType.class));
            doReturn(value).when(testDaoMock).update(id, value);

            testService.update(id, value);

            // check that intercepted method was invoked
            verify(testDaoMock).update(id, value);
            // check that cache was invoked
            verify(cacheMock).set(eq(Long.toString(id)), anyInt(), eq(value), isNull(SerializationType.class));
        }
    }

}
