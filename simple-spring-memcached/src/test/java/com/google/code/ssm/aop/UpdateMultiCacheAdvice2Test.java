/* Copyright (c) 2011 Jakub Białek
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

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.hamcrest.CoreMatchers;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.junit.runners.Parameterized.Parameters;

import com.google.code.ssm.api.ParameterDataUpdateContent;
import com.google.code.ssm.api.ParameterValueKeyProvider;
import com.google.code.ssm.api.ReturnDataUpdateContent;
import com.google.code.ssm.api.ReturnValueKeyProvider;
import com.google.code.ssm.api.UpdateMultiCache;
import com.google.code.ssm.api.UpdateMultiCacheOption;
import com.google.code.ssm.test.Point;

/**
 * 
 * @author Jakub Białek
 * 
 */
public class UpdateMultiCacheAdvice2Test extends AbstractCacheTest<UpdateMultiCacheAdvice> {

    @Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] { //
                        { true, "method1", new Class[] { List.class }, new Object[] { Arrays.asList(1, 2, 3, 4) },
                                Arrays.asList(2, 4, 6, 8), Arrays.asList(2, 4, 6, 8),
                                new String[] { NS + ":1", NS + ":2", NS + ":3", NS + ":4" } }, //
                        { true, "method2", new Class[] { List.class },
                                new Object[] { Arrays.asList(new Point(1, 2), new Point(2, 3), new Point(3, 4), new Point(4, 5)) },
                                Arrays.asList("2", "4", "6", "8"), Arrays.asList("2", "4", "6", "8"),
                                new String[] { NS + ":(1,2)", NS + ":(2,3)", NS + ":(3,4)", NS + ":(4,5)" } }, //
                        { true, "method3", new Class[] { List.class, String.class }, new Object[] { Arrays.asList(1, 2, 3, 4), "xyz" },
                                Arrays.asList("2", "4", "6", "8"), Arrays.asList("2", "4", "6", "8"),
                                new String[] { NS + ":xyz/1", NS + ":xyz/2", NS + ":xyz/3", NS + ":xyz/4" } }, //
                        { true, "method4", new Class[] { List.class }, new Object[] { Arrays.asList(1, 2, 3, 4) },
                                Arrays.asList(new Point(1, 2), new Point(2, 3), new Point(3, 4), new Point(4, 5)),
                                Arrays.asList(new Point(1, 2), new Point(2, 3), new Point(3, 4), new Point(4, 5)),
                                new String[] { NS + ":1", NS + ":2", NS + ":3", NS + ":4" } }, //
                        { true, "method5", new Class[] { List.class, List.class },
                                new Object[] { Arrays.asList("2", "4", "6", "8"), Arrays.asList("1", "2", "3", "4") },
                                Arrays.asList("2", "4", "6", "8"), null, new String[] { NS + ":1", NS + ":2", NS + ":3", NS + ":4" } }, //
                        { true, "method6", new Class[] { List.class, List.class, String.class },
                                new Object[] { Arrays.asList("2", "4", "6", "8"), Arrays.asList("1", "2", "3", "4"), "abc" },
                                Arrays.asList("2", "4", "6", "8"), null,
                                new String[] { NS + ":abc/1", NS + ":abc/2", NS + ":abc/3", NS + ":abc/4" } }, //
                        { true, "method7", new Class[] { List.class }, new Object[] { Arrays.asList("1", "2", "3", "4") },
                                Arrays.asList("1", "2", "3", "4"), 7, new String[] { NS + ":1", NS + ":2", NS + ":3", NS + ":4" } }, //
                        { true, "method8", new Class[] { List.class },
                                new Object[] { Arrays.asList(new Point(1, 2), new Point(2, 3), new Point(3, 4), new Point(4, 5)) },
                                Arrays.asList(new Point(1, 2), new Point(2, 3), new Point(3, 4), new Point(4, 5)), 8,
                                new String[] { NS + ":(1,2)", NS + ":(2,3)", NS + ":(3,4)", NS + ":(4,5)" } }, //
                        { true, "method9", new Class[] { List.class, String.class },
                                new Object[] { Arrays.asList("1", "2", "3", "4"), "zxc" }, Arrays.asList("1", "2", "3", "4"), 9,
                                new String[] { NS + ":1/zxc", NS + ":2/zxc", NS + ":3/zxc", NS + ":4/zxc" } }, //
                        { true, "method10", new Class[] { List.class }, new Object[] { Arrays.asList(1, 2, 3, 4) },
                                Arrays.asList("2", "4", "6", "8"), Arrays.asList("2", "4", "6", "8"),
                                new String[] { NS + ":1", NS + ":2", NS + ":3", NS + ":4" } }, //
                        { true, "method11", new Class[] { List.class }, new Object[] { Arrays.asList(1, 2, 3, 4) },
                                Arrays.asList("2", "4", "6", "8"), Arrays.asList("2", "4", "6", "8"),
                                new String[] { NS + ":2", NS + ":4", NS + ":6", NS + ":8" } }, //
                        { true, "method12", new Class[] {}, new Object[] {},
                                Arrays.asList(new Point(1, 2), new Point(2, 3), new Point(3, 4), new Point(4, 5)),
                                Arrays.asList(new Point(1, 2), new Point(2, 3), new Point(3, 4), new Point(4, 5)),
                                new String[] { NS + ":(1,2)", NS + ":(2,3)", NS + ":(3,4)", NS + ":(4,5)" } }, //
                        { true, "method13", new Class[] { List.class }, new Object[] { Arrays.asList("(1,2)", "(2,3)", "(3,4)", "(4,5)") },
                                Arrays.asList(new Point(1, 2), new Point(2, 3), new Point(3, 4), new Point(4, 5)),
                                Arrays.asList(new Point(1, 2), new Point(2, 3), new Point(3, 4), new Point(4, 5)),
                                new String[] { NS + ":(1,2)", NS + ":(2,3)", NS + ":(3,4)", NS + ":(4,5)" } }, //
                        { true, "method13", new Class[] { List.class }, new Object[] { Arrays.asList("(1,2)", "(2,3)", "(3,4)", "(4,5)") },
                                Arrays.asList(new Point(1, 2), PertinentNegativeNull.NULL, new Point(3, 4), PertinentNegativeNull.NULL),
                                Arrays.asList(new Point(1, 2), new Point(3, 4)),
                                new String[] { NS + ":(1,2)", NS + ":(2,3)", NS + ":(3,4)", NS + ":(4,5)" } }, //
                        { true, "method14", new Class[] { List.class }, new Object[] { Arrays.asList("(1,2)", "(2,3)", "(3,4)", "(4,5)") },
                                Arrays.asList(new Point(1, 2), new Point(2, 3), new Point(3, 4), new Point(4, 5)),
                                Arrays.asList(new Point(1, 2), new Point(2, 3), new Point(3, 4), new Point(4, 5)),
                                new String[] { NS + ":(1,2)", NS + ":(2,3)", NS + ":(3,4)", NS + ":(4,5)" } }, //
                        { true, "method14", new Class[] { List.class }, new Object[] { Arrays.asList("(1,2)", "(2,3)", "(3,4)", "(4,5)") },
                                Arrays.asList(new Point(1, 2), PertinentNegativeNull.NULL, new Point(3, 4), PertinentNegativeNull.NULL),
                                Arrays.asList(new Point(1, 2), new Point(3, 4)),
                                new String[] { NS + ":(1,2)", NS + ":(2,3)", NS + ":(3,4)", NS + ":(4,5)" } }, //

                        { false, "method50", new Class[] { List.class }, new Object[] { Arrays.asList("1", "2", "3", "4") }, null,
                                Arrays.asList("1", "2", "3", "4"), null }, //
                        { false, "method51", new Class[] { List.class }, new Object[] { Arrays.asList("1", "2", "3", "4") }, null,
                                Arrays.asList("1", "2", "3", "4"), null }, //
                        { false, "method52", new Class[] { List.class }, new Object[] { Arrays.asList("1", "2", "3", "4") }, null, null,
                                null }, //
                        { false, "method53", new Class[] { List.class }, new Object[] { Arrays.asList("1", "2", "3", "4") }, null, "", null }, //
                        { false, "method54", new Class[] { String.class }, new Object[] { "xyz" }, null, Arrays.asList("1", "2", "3", "4"),
                                null }, //
                        { false, "method55", new Class[] { List.class, String.class },
                                new Object[] { Arrays.asList("1", "2", "3", "4"), "abc" }, null, null, null }, //
                        { false, "method56", new Class[] { List.class, String.class },
                                new Object[] { Arrays.asList("1", "2", "3", "4"), "abc" }, null, null, null }, //
                        // amount of elements on list annotated @ParameterValueKeyProvider is different than on
                        // @ReturnDataUpdateContent
                        { false, "method1", new Class[] { List.class }, new Object[] { Arrays.asList(1, 2, 3, 4) }, null,
                                Arrays.asList(2, 4, 6), null }, //
                        // amount of elements on list annotated @ParameterValueKeyProvider is different than on
                        // @ReturnDataUpdateContent
                        { false, "method1", new Class[] { List.class }, new Object[] { Arrays.asList(1, 2, 3, 4) }, null, null, null }, //
                        // amount of elements on list annotated @ParameterValueKeyProvider is different than on
                        // @ReturnDataUpdateContent
                        { false, "method1", new Class[] { List.class }, new Object[] { null }, null, Arrays.asList(2, 4, 6), null }, //

                });
    }

    private static final String NS = "QWERTY";

    private static final int EXPIRATION = 345;

    private List<?> expectedValue;

    private Object returnValue;

    private String[] cacheKeys;

    public UpdateMultiCacheAdvice2Test(boolean isValid, String methodName, Class<?>[] paramTypes, Object[] params, List<?> expectedValue,
            Object returnValue, String[] cacheKeys) {
        super(isValid, methodName, paramTypes, params, null);
        this.returnValue = returnValue;
        this.expectedValue = expectedValue;
        this.cacheKeys = cacheKeys;
    }

    @Before
    public void setUp() {
        super.setUp(new TestService());
    }

    @Test
    public void valid() throws Throwable {
        Assume.assumeTrue(isValid);

        advice.cacheUpdateMulti(pjp, returnValue);

        for (int i = 0; i < cacheKeys.length; i++) {
            if (advice.getMethodToCache(pjp).getAnnotation(UpdateMultiCache.class).option().overwriteNoNulls()) {
                verify(client).set(eq(cacheKeys[i]), eq(EXPIRATION), eq(expectedValue.get(i)));
            } else if (advice.getMethodToCache(pjp).getAnnotation(UpdateMultiCache.class).option().addNullsToCache()
                    && expectedValue.get(i) instanceof PertinentNegativeNull) {
                verify(client).add(eq(cacheKeys[i]), eq(EXPIRATION), eq(expectedValue.get(i)));
            } else {
                verify(client).set(eq(cacheKeys[i]), eq(EXPIRATION), eq(expectedValue.get(i)));
            }

        }
    }

    @Test
    public void invalid() throws Throwable {
        Assume.assumeThat(isValid, CoreMatchers.is(false));

        advice.cacheUpdateMulti(pjp, expectedValue);

        verify(client, never()).set(anyString(), anyInt(), any());
    }

    @Override
    protected UpdateMultiCacheAdvice createAdvice() {
        return new UpdateMultiCacheAdvice();
    }

    @Override
    protected String getNamespace() {
        return NS;
    }

    @SuppressWarnings("unused")
    private static class TestService {

        @ReturnDataUpdateContent
        @UpdateMultiCache(namespace = NS, expiration = EXPIRATION)
        public List<Integer> method1(@ParameterValueKeyProvider List<Integer> ids) {
            return Collections.<Integer> emptyList();
        }

        @ReturnDataUpdateContent
        @UpdateMultiCache(namespace = NS, expiration = EXPIRATION)
        public List<String> method2(@ParameterValueKeyProvider List<Point> ids) {
            return Collections.<String> emptyList();
        }

        @ReturnDataUpdateContent
        @UpdateMultiCache(namespace = NS, expiration = EXPIRATION)
        public List<String> method3(@ParameterValueKeyProvider(order = 2) List<Integer> ids,
                @ParameterValueKeyProvider(order = 1) String value) {
            return Collections.<String> emptyList();
        }

        @ReturnDataUpdateContent
        @UpdateMultiCache(namespace = NS, expiration = EXPIRATION)
        public List<Point> method4(@ParameterValueKeyProvider List<Integer> ids) {
            return Collections.<Point> emptyList();
        }

        @UpdateMultiCache(namespace = NS, expiration = EXPIRATION)
        public void method5(@ParameterDataUpdateContent List<String> contents, @ParameterValueKeyProvider List<String> ids) {

        }

        @UpdateMultiCache(namespace = NS, expiration = EXPIRATION)
        public void method6(@ParameterDataUpdateContent List<String> contents, @ParameterValueKeyProvider(order = 2) List<String> ids,
                @ParameterValueKeyProvider(order = 1) String value) {

        }

        @UpdateMultiCache(namespace = NS, expiration = EXPIRATION)
        public int method7(@ParameterDataUpdateContent @ParameterValueKeyProvider List<String> contents) {
            return 7;
        }

        @UpdateMultiCache(namespace = NS, expiration = EXPIRATION)
        public int method8(@ParameterDataUpdateContent @ParameterValueKeyProvider List<Point> contents) {
            return 8;
        }

        @UpdateMultiCache(namespace = NS, expiration = EXPIRATION)
        public int method9(@ParameterDataUpdateContent @ParameterValueKeyProvider(order = 1) List<String> contents,
                @ParameterValueKeyProvider(order = 2) String value) {
            return 9;
        }

        // currently it's valid to use both @ParameterDataUpdateContent and @ReturnDataUpdateContent
        // in such case @ReturnDataUpdateContent takes precedence
        @ReturnDataUpdateContent
        @UpdateMultiCache(namespace = NS, expiration = EXPIRATION)
        public List<String> method10(@ParameterDataUpdateContent @ParameterValueKeyProvider List<Integer> ids) {
            return Collections.<String> emptyList();
        }

        // currently it's valid to use both @ParameterDataUpdateContent and @ReturnDataUpdateContent
        // in such case @ReturnDataUpdateContent takes precedence
        @ReturnValueKeyProvider
        @ReturnDataUpdateContent
        @UpdateMultiCache(namespace = NS, expiration = EXPIRATION)
        public List<String> method11(@ParameterDataUpdateContent List<Integer> ids) {
            return Collections.<String> emptyList();
        }

        @ReturnValueKeyProvider
        @ReturnDataUpdateContent
        @UpdateMultiCache(namespace = NS, expiration = EXPIRATION)
        public List<Point> method12() {
            return Collections.<Point> emptyList();
        }

        @ReturnDataUpdateContent
        @UpdateMultiCache(namespace = NS, expiration = EXPIRATION, option = @UpdateMultiCacheOption(addNullsToCache = true))
        public List<Point> method13(@ParameterValueKeyProvider List<String> ids) {
            return Collections.<Point> emptyList();
        }

        @ReturnDataUpdateContent
        @UpdateMultiCache(namespace = NS, expiration = EXPIRATION, option = @UpdateMultiCacheOption(addNullsToCache = true, overwriteNoNulls = true))
        public List<Point> method14(@ParameterValueKeyProvider List<String> ids) {
            return Collections.<Point> emptyList();
        }

        // no @ParameterValueKeyProvider
        @ReturnDataUpdateContent
        @UpdateMultiCache(namespace = NS, expiration = EXPIRATION)
        public List<String> method50(List<String> ids) {
            return Collections.<String> emptyList();
        }

        // no @ParameterDataUpdateContent or @ReturnDataUpdateContent
        @UpdateMultiCache(namespace = NS, expiration = EXPIRATION)
        public List<String> method51(@ParameterValueKeyProvider List<String> ids) {
            return Collections.<String> emptyList();
        }

        // @ReturnDataUpdateContent but void return type
        @ReturnDataUpdateContent
        @UpdateMultiCache(namespace = NS, expiration = EXPIRATION)
        public void method52(@ParameterValueKeyProvider List<String> ids) {

        }

        // @ReturnDataUpdateContent but return type is not List
        @ReturnDataUpdateContent
        @UpdateMultiCache(namespace = NS, expiration = EXPIRATION)
        public String method53(@ParameterValueKeyProvider List<String> ids) {
            return null;
        }

        // no list method parameter annotated with @ParameterValueKeyProvider
        @ReturnDataUpdateContent
        @UpdateMultiCache(namespace = NS, expiration = EXPIRATION)
        public List<String> method54(@ParameterValueKeyProvider String id) {
            return Collections.<String> emptyList();
        }

        // @ParameterDataUpdateContent but annotated method parameter is not List
        @UpdateMultiCache(namespace = NS, expiration = EXPIRATION)
        public void method55(@ParameterValueKeyProvider List<String> ids, @ParameterDataUpdateContent String content) {

        }

        // no list method parameter annotated with @ParameterValueKeyProvider
        @UpdateMultiCache(namespace = NS, expiration = EXPIRATION)
        public void method56(List<String> ids, @ParameterDataUpdateContent List<String> contents, @ParameterValueKeyProvider String id) {

        }

    }

}
