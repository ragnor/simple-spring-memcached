/*
 * Copyright (c) 2008-2013 Nelson Carpentier, Jakub Białek
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

package com.google.code.ssm.aop.support;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.security.InvalidParameterException;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

import net.vidageek.mirror.dsl.Mirror;

import org.junit.Test;

import com.google.code.ssm.aop.AnnotationDataDummy;
import com.google.code.ssm.api.InvalidateAssignCache;
import com.google.code.ssm.api.InvalidateSingleCache;
import com.google.code.ssm.api.UpdateAssignCache;

/**
 * 
 * @author Nelson Carpentier
 * @author Jakub Białek
 * 
 */
public class AnnotationDataBuilderTest {

    @Test
    public void testPopulateAssignedKey() throws Exception {
        final AnnotationData data = new AnnotationData();

        Class<? extends Annotation> expected;
        String method;
        Annotation annotation;

        // Falls outside the set that assigns a key
        expected = InvalidateSingleCache.class;
        method = "populateAssign01";
        annotation = new Mirror().on(AnnotationDataDummy.class).reflect().annotation(expected).atMethod(method).withArgs(String.class);
        AnnotationDataBuilder.populateAssignedKey(data, annotation, expected, method);
        assertEquals("", data.getAssignedKey());

        // Default (or unassigned) value throws an exception
        expected = InvalidateAssignCache.class;
        method = "populateAssign02";
        annotation = new Mirror().on(AnnotationDataDummy.class).reflect().annotation(expected).atMethod(method).withArgs(String.class);
        try {
            AnnotationDataBuilder.populateAssignedKey(data, annotation, expected, method);
            fail("expected exception");
        } catch (InvalidParameterException ex) {
        }
        assertEquals("", data.getAssignedKey());

        // 0-length value throws an exception
        expected = InvalidateAssignCache.class;
        method = "populateAssign03";
        annotation = new Mirror().on(AnnotationDataDummy.class).reflect().annotation(expected).atMethod(method).withArgs(String.class);
        try {
            AnnotationDataBuilder.populateAssignedKey(data, annotation, expected, method);
            fail("expected exception");
        } catch (InvalidParameterException ex) {
        }
        assertEquals("", data.getAssignedKey());

        // Successful assignment
        expected = InvalidateAssignCache.class;
        method = "populateAssign04";
        annotation = new Mirror().on(AnnotationDataDummy.class).reflect().annotation(expected).atMethod(method).withArgs(String.class);
        AnnotationDataBuilder.populateAssignedKey(data, annotation, expected, method);
        assertEquals(AnnotationDataDummy.SAMPLE_KEY, data.getAssignedKey());

    }

    @Test
    public void testPopulateNamespace() throws Exception {
        final AnnotationData data = new AnnotationData();

        Class<? extends Annotation> expected;
        String method;
        Annotation annotation;

        // Default Namespace
        expected = InvalidateAssignCache.class;
        method = "populateNamespace01";
        annotation = new Mirror().on(AnnotationDataDummy.class).reflect().annotation(expected).atMethod(method).withArgs(String.class);
        try {
            AnnotationDataBuilder.populateNamespace(data, annotation, expected, method);
            fail("expected exception");
        } catch (InvalidParameterException ex) {
        }
        assertEquals("", data.getNamespace());

        // 0-length String as Namespace
        expected = InvalidateAssignCache.class;
        method = "populateNamespace02";
        annotation = new Mirror().on(AnnotationDataDummy.class).reflect().annotation(expected).atMethod(method).withArgs(String.class);
        try {
            AnnotationDataBuilder.populateNamespace(data, annotation, expected, method);
            fail("expected exception");
        } catch (InvalidParameterException ex) {
        }
        assertEquals("", data.getNamespace());

        // Get the Namespace successfully
        expected = InvalidateAssignCache.class;
        method = "populateNamespace03";
        annotation = new Mirror().on(AnnotationDataDummy.class).reflect().annotation(expected).atMethod(method).withArgs(String.class);
        AnnotationDataBuilder.populateNamespace(data, annotation, expected, method);
        assertEquals(AnnotationDataDummy.SAMPLE_NS, data.getNamespace());
    }

    @Test
    public void testPopulateExpiration() throws Exception {
        final AnnotationData data = new AnnotationData();

        Class<? extends Annotation> expected;
        String method;
        Annotation annotation;

        // Excluded from having an Expiration
        expected = InvalidateAssignCache.class;
        method = "populateExpiration01";
        annotation = new Mirror().on(AnnotationDataDummy.class).reflect().annotation(expected).atMethod(method).withArgs(String.class);
        AnnotationDataBuilder.populateExpiration(data, annotation, expected, method);
        assertEquals(0, data.getExpiration());

        // Negative Expiration
        expected = UpdateAssignCache.class;
        method = "populateExpiration02";
        annotation = new Mirror().on(AnnotationDataDummy.class).reflect().annotation(expected).atMethod(method).withArgs(String.class);
        try {
            AnnotationDataBuilder.populateExpiration(data, annotation, expected, method);
            fail("expected exception");
        } catch (InvalidParameterException ex) {
        }
        assertEquals(0, data.getExpiration());

        // Negative Expiration
        expected = UpdateAssignCache.class;
        method = "populateExpiration03";
        annotation = new Mirror().on(AnnotationDataDummy.class).reflect().annotation(expected).atMethod(method).withArgs(String.class);
        AnnotationDataBuilder.populateExpiration(data, annotation, expected, method);
        assertEquals(AnnotationDataDummy.SAMPLE_EXP, data.getExpiration());
    }

    @Test
    public void testPopulateClassName() throws Exception {
        final AnnotationData data = new AnnotationData();

        Class<? extends Annotation> expected;
        String method;
        Annotation annotation;

        // Null Annotation
        expected = InvalidateAssignCache.class;
        method = "populateClassName01";
        try {
            AnnotationDataBuilder.populateClassName(data, null, expected);
            fail("expected exception");
        } catch (InvalidParameterException ex) {
        }
        assertEquals("", data.getClassName());

        // Annotation doesn't match the expected Annotation Class.
        expected = InvalidateAssignCache.class;
        method = "populateClassName01";
        annotation = new Mirror().on(AnnotationDataDummy.class).reflect().annotation(expected).atMethod(method).withArgs(String.class);
        try {
            AnnotationDataBuilder.populateClassName(data, annotation, UpdateAssignCache.class);
            fail("expected exception");
        } catch (InvalidParameterException ex) {
        }
        assertEquals("", data.getClassName());

        // Success
        expected = InvalidateAssignCache.class;
        method = "populateClassName01";
        annotation = new Mirror().on(AnnotationDataDummy.class).reflect().annotation(expected).atMethod(method).withArgs(String.class);
        AnnotationDataBuilder.populateClassName(data, annotation, expected);
        assertEquals(expected.getName(), data.getClassName());
    }

    // disabled
    @Test
    public void testPopulateKeyProvider() throws Exception {
        AnnotationData data;

        Class<? extends Annotation> expected = InvalidateAssignCache.class;
        Method targetMethod = null;

        // Expected Annotation doesn't require keyProvider
        data = new AnnotationData();
        AnnotationDataBuilder.populateKeyIndexes(data, expected, targetMethod);
        assertTrue(data.getKeyIndexes().isEmpty());
        assertFalse(data.isReturnKeyIndex());

        // No KeyProvider annotations at all
        data = new AnnotationData();
        expected = InvalidateSingleCache.class;
        targetMethod = new Mirror().on(AnnotationDataDummy.class).reflect().method("populateKeyProvider01").withArgs(String.class);
        try {
            AnnotationDataBuilder.populateKeyIndexes(data, expected, targetMethod);
            fail("expected exception");
        } catch (InvalidParameterException ex) {
        }
        assertTrue(data.getKeyIndexes().isEmpty());
        assertFalse(data.isReturnKeyIndex());

        // ReturnKeyProvider
        data = new AnnotationData();
        expected = InvalidateSingleCache.class;
        targetMethod = new Mirror().on(AnnotationDataDummy.class).reflect().method("populateKeyProvider02").withArgs(String.class);
        AnnotationDataBuilder.populateKeyIndexes(data, expected, targetMethod);
        assertTrue(data.getKeyIndexes().isEmpty());
        assertTrue(data.isReturnKeyIndex());

        // ParamKeyProvider
        data = new AnnotationData();
        expected = InvalidateSingleCache.class;
        targetMethod = new Mirror().on(AnnotationDataDummy.class).reflect().method("populateKeyProvider03").withArgs(String.class);
        AnnotationDataBuilder.populateKeyIndexes(data, expected, targetMethod);
        assertCollectionEquals(Collections.singleton(0), data.getKeyIndexes());
        assertFalse(data.isReturnKeyIndex());

        // Multiple ParamKeyProviders with the same order value
        data = new AnnotationData();
        expected = InvalidateSingleCache.class;
        targetMethod = new Mirror().on(AnnotationDataDummy.class).reflect().method("populateKeyProvider04")
                .withArgs(String.class, String.class);
        try {
            AnnotationDataBuilder.populateKeyIndexes(data, expected, targetMethod);
            fail("expected exception");
        } catch (InvalidParameterException ex) {
        }
        assertFalse(data.isReturnKeyIndex());

        // Param KeyProvider with override
        data = new AnnotationData();
        expected = InvalidateSingleCache.class;
        targetMethod = new Mirror().on(AnnotationDataDummy.class).reflect().method("populateKeyProvider05")
                .withArgs(String.class, String.class, String.class);
        AnnotationDataBuilder.populateKeyIndexes(data, expected, targetMethod);
        assertCollectionEquals(Collections.singleton(2), data.getKeyIndexes());
        assertFalse(data.isReturnKeyIndex());

        // Return KeyProvider with override
        data = new AnnotationData();
        expected = InvalidateSingleCache.class;
        targetMethod = new Mirror().on(AnnotationDataDummy.class).reflect().method("populateKeyProvider06").withArgs(String.class);
        AnnotationDataBuilder.populateKeyIndexes(data, expected, targetMethod);
        assertTrue(data.isReturnKeyIndex());
        assertTrue(data.getKeyIndexes().isEmpty());

        // Param KeyProvider without override
        data = new AnnotationData();
        expected = InvalidateSingleCache.class;
        targetMethod = new Mirror().on(AnnotationDataDummy.class).reflect().method("populateKeyProvider07")
                .withArgs(String.class, String.class, String.class);
        AnnotationDataBuilder.populateKeyIndexes(data, expected, targetMethod);
        assertCollectionEquals(Collections.singleton(1), data.getKeyIndexes());
        assertFalse(data.isReturnKeyIndex());

        // Return KeyProvider without override
        data = new AnnotationData();
        expected = InvalidateSingleCache.class;
        targetMethod = new Mirror().on(AnnotationDataDummy.class).reflect().method("populateKeyProvider08").withArgs(String.class);
        AnnotationDataBuilder.populateKeyIndexes(data, expected, targetMethod);
        assertTrue(data.isReturnKeyIndex());
        assertTrue(data.getKeyIndexes().isEmpty());

        // Multiple ParamKeyProviders with the different order
        data = new AnnotationData();
        expected = InvalidateSingleCache.class;
        targetMethod = new Mirror().on(AnnotationDataDummy.class).reflect().method("populateKeyProvider09")
                .withArgs(String.class, String.class);

        AnnotationDataBuilder.populateKeyIndexes(data, expected, targetMethod);
        assertEquals(2, data.getKeyIndexes().size());
        Iterator<Integer> iter = data.getKeyIndexes().iterator();
        assertEquals(1, iter.next().intValue());
        assertEquals(0, iter.next().intValue());
        assertFalse(data.isReturnKeyIndex());
    }

    @Test
    public void testPopulateDataIndex() throws Exception {
        final AnnotationData data = new AnnotationData();

        Class<? extends Annotation> expected = InvalidateAssignCache.class;
        Method targetMethod = null;

        // Not an update type.
        AnnotationDataBuilder.populateDataIndexFromAnnotations(data, expected, null);
        assertEquals(AnnotationData.DEFAULT_INTEGER, data.getDataIndex());

        // Not an update type.
        expected = UpdateAssignCache.class;
        targetMethod = new Mirror().on(AnnotationDataDummy.class).reflect().method("populateData01").withArgs(String.class);
        try {
            AnnotationDataBuilder.populateDataIndexFromAnnotations(data, expected, targetMethod);
            fail("expected exception");
        } catch (InvalidParameterException ex) {
        }
        assertEquals(AnnotationData.DEFAULT_INTEGER, data.getDataIndex());

        // Multiple Parameter data values
        expected = UpdateAssignCache.class;
        targetMethod = new Mirror().on(AnnotationDataDummy.class).reflect().method("populateData02").withArgs(String.class, String.class);
        try {
            AnnotationDataBuilder.populateDataIndexFromAnnotations(data, expected, targetMethod);
            fail("expected exception");
        } catch (InvalidParameterException ex) {
        }
        assertEquals(AnnotationData.DEFAULT_INTEGER, data.getDataIndex());

        // Return data values
        expected = UpdateAssignCache.class;
        targetMethod = new Mirror().on(AnnotationDataDummy.class).reflect().method("populateData03").withArgs(String.class);
        AnnotationDataBuilder.populateDataIndexFromAnnotations(data, expected, targetMethod);
        assertEquals(-1, data.getDataIndex());

        // Parameter data values
        expected = UpdateAssignCache.class;
        targetMethod = new Mirror().on(AnnotationDataDummy.class).reflect().method("populateData04").withArgs(String.class, String.class);
        AnnotationDataBuilder.populateDataIndexFromAnnotations(data, expected, targetMethod);
        assertEquals(1, data.getDataIndex());
    }

    private static void assertCollectionEquals(final Collection<?> expected, final Collection<?> target) {
        assertTrue(String.format("Expected %s, currect %s", expected, target), expected.containsAll(target));
        assertTrue(String.format("Expected %s, currect %s", expected, target), target.containsAll(expected));
    }

}
