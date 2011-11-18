package com.google.code.ssm.aop;

import static org.junit.Assert.*;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.security.InvalidParameterException;
import java.util.Iterator;

import org.junit.Test;

import com.google.code.ssm.api.InvalidateAssignCache;
import com.google.code.ssm.api.InvalidateSingleCache;
import com.google.code.ssm.api.UpdateAssignCache;
import com.google.code.ssm.api.AnnotationConstants;
import net.vidageek.mirror.dsl.Mirror;

/**
 * Copyright (c) 2008, 2009 Nelson Carpentier
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
 * 
 * @author Nelson Carpentier
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

    // was disabled
    @Test
    public void testPopulateKeyProvider() throws Exception {
        final AnnotationData data = new AnnotationData();

        Class<? extends Annotation> expected = InvalidateAssignCache.class;
        Method targetMethod = null;

        // Expected Annotation doesn't require keyProvider
        AnnotationDataBuilder.populateKeyIndexAndBeanName(data, expected, targetMethod);
        assertEquals("", data.getKeyProviderBeanName());
        assertEquals(AnnotationData.DEFAULT_INTEGER, data.getKeyIndex());

        // No KeyProvider annotations at all
        expected = InvalidateSingleCache.class;
        targetMethod = new Mirror().on(AnnotationDataDummy.class).reflect().method("populateKeyProvider01").withArgs(String.class);
        try {
            AnnotationDataBuilder.populateKeyIndexAndBeanName(data, expected, targetMethod);
            fail("expected exception");
        } catch (InvalidParameterException ex) {
        }
        assertEquals("", data.getKeyProviderBeanName());
        assertEquals(AnnotationData.DEFAULT_INTEGER, data.getKeyIndex());

        // ReturnKeyProvider with an empty bean name
        expected = InvalidateSingleCache.class;
        targetMethod = new Mirror().on(AnnotationDataDummy.class).reflect().method("populateKeyProvider02").withArgs(String.class);
        try {
            AnnotationDataBuilder.populateKeyIndexAndBeanName(data, expected, targetMethod);
            fail("expected exception");
        } catch (InvalidParameterException ex) {
        }
        assertEquals("", data.getKeyProviderBeanName());
        assertEquals(AnnotationData.DEFAULT_INTEGER, data.getKeyIndex());

        // ParamKeyProvider with an empty bean name
        expected = InvalidateSingleCache.class;
        targetMethod = new Mirror().on(AnnotationDataDummy.class).reflect().method("populateKeyProvider03").withArgs(String.class);
        try {
            AnnotationDataBuilder.populateKeyIndexAndBeanName(data, expected, targetMethod);
            fail("expected exception");
        } catch (InvalidParameterException ex) {
        }
        assertEquals("", data.getKeyProviderBeanName());
        assertEquals(AnnotationData.DEFAULT_INTEGER, data.getKeyIndex());

        // Multiple ParamKeyProviders with the same order value
        expected = InvalidateSingleCache.class;
        targetMethod = new Mirror().on(AnnotationDataDummy.class).reflect().method("populateKeyProvider04")
                .withArgs(String.class, String.class);
        try {
            AnnotationDataBuilder.populateKeyIndexAndBeanName(data, expected, targetMethod);
            fail("expected exception");
        } catch (InvalidParameterException ex) {
        }
        // assertEquals("", data.getKeyProviderBeanName());
        assertEquals(AnnotationData.DEFAULT_INTEGER, data.getKeyIndex());

        // Param KeyProvider with override
        expected = InvalidateSingleCache.class;
        targetMethod = new Mirror().on(AnnotationDataDummy.class).reflect().method("populateKeyProvider05")
                .withArgs(String.class, String.class, String.class);
        AnnotationDataBuilder.populateKeyIndexAndBeanName(data, expected, targetMethod);
        assertEquals(AnnotationDataDummy.SAMPLE_PARAM_BEAN, data.getKeyProviderBeanName());
        assertEquals(2, data.getKeyIndex());

        // Return KeyProvider with override
        expected = InvalidateSingleCache.class;
        targetMethod = new Mirror().on(AnnotationDataDummy.class).reflect().method("populateKeyProvider06").withArgs(String.class);
        AnnotationDataBuilder.populateKeyIndexAndBeanName(data, expected, targetMethod);
        assertEquals(AnnotationDataDummy.SAMPLE_RETURN_BEAN, data.getKeyProviderBeanName());
        assertEquals(-1, data.getKeyIndex());

        // Param KeyProvider without override
        expected = InvalidateSingleCache.class;
        targetMethod = new Mirror().on(AnnotationDataDummy.class).reflect().method("populateKeyProvider07")
                .withArgs(String.class, String.class, String.class);
        AnnotationDataBuilder.populateKeyIndexAndBeanName(data, expected, targetMethod);
        assertEquals(AnnotationConstants.DEFAULT_KEY_PROVIDER_BEAN_NAME, data.getKeyProviderBeanName());
        assertEquals(1, data.getKeyIndex());

        // Return KeyProvider without override
        expected = InvalidateSingleCache.class;
        targetMethod = new Mirror().on(AnnotationDataDummy.class).reflect().method("populateKeyProvider08").withArgs(String.class);
        AnnotationDataBuilder.populateKeyIndexAndBeanName(data, expected, targetMethod);
        assertEquals(AnnotationConstants.DEFAULT_KEY_PROVIDER_BEAN_NAME, data.getKeyProviderBeanName());
        assertEquals(-1, data.getKeyIndex());

        // Multiple ParamKeyProviders with the different order
        expected = InvalidateSingleCache.class;
        targetMethod = new Mirror().on(AnnotationDataDummy.class).reflect().method("populateKeyProvider09")
                .withArgs(String.class, String.class);

        AnnotationDataBuilder.populateKeyIndexAndBeanName(data, expected, targetMethod);
        // assertEquals("", data.getKeyProviderBeanName());
        assertEquals(2, data.getKeysIndex().size());
        Iterator<Integer> iter = data.getKeysIndex().iterator();
        assertEquals(1, iter.next().intValue());
        assertEquals(0, iter.next().intValue());
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

}
