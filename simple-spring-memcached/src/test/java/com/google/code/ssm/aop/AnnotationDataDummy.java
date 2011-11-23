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

import com.google.code.ssm.api.*;

/**
 * 
 * @author Nelson Carpentier
 * 
 */
public class AnnotationDataDummy {

    public static final String SAMPLE_KEY = "bigSampleKey";
    public static final String SAMPLE_NS = "bigSampleNamespace";
    public static final int SAMPLE_EXP = 42;
    public static final String SAMPLE_PARAM_BEAN = "bigParamBean";
    public static final String SAMPLE_RETURN_BEAN = "bigReturnBean";

    @InvalidateSingleCache
    public void populateAssign01(final String key1) {
    }

    @InvalidateAssignCache
    public void populateAssign02(final String key1) {
    }

    @InvalidateAssignCache(assignedKey = "")
    public void populateAssign03(final String key1) {
    }

    @InvalidateAssignCache(assignedKey = SAMPLE_KEY)
    public void populateAssign04(final String key1) {
    }

    @InvalidateAssignCache
    public void populateNamespace01(final String key1) {
    }

    @InvalidateAssignCache(namespace = "")
    public void populateNamespace02(final String key1) {
    }

    @InvalidateAssignCache(namespace = SAMPLE_NS)
    public void populateNamespace03(final String key1) {
    }

    @InvalidateAssignCache
    public void populateExpiration01(final String key1) {
    }

    @UpdateAssignCache(expiration = -1)
    public void populateExpiration02(final String key1) {
    }

    @UpdateAssignCache(expiration = SAMPLE_EXP)
    public void populateExpiration03(final String key1) {
    }

    @InvalidateAssignCache
    public void populateClassName01(final String key1) {
    }

    @InvalidateSingleCache
    public void populateKeyProvider01(final String key1) {
    }

/*    @InvalidateSingleCache
    @ReturnValueKeyProvider(keyProviderBeanName = "")
    public void populateKeyProvider02(final String key1) {
    }

    @InvalidateSingleCache
    public void populateKeyProvider03(@ParameterValueKeyProvider(keyProviderBeanName = "") final String key1) {
    }

    @InvalidateSingleCache
    public void populateKeyProvider04(@ParameterValueKeyProvider(keyProviderBeanName = SAMPLE_PARAM_BEAN) final String key1,
            @ParameterValueKeyProvider(keyProviderBeanName = "anotherSampleBean") final String key2) {
    }

    @InvalidateSingleCache
    public void populateKeyProvider05(final String key1, final String key2,
            @ParameterValueKeyProvider(keyProviderBeanName = SAMPLE_PARAM_BEAN) final String key3) {
    }

    @InvalidateSingleCache
    @ReturnValueKeyProvider(keyProviderBeanName = SAMPLE_RETURN_BEAN)
    public void populateKeyProvider06(final String key1) {
    }
*/
    @InvalidateSingleCache
    public void populateKeyProvider07(final String key1, @ParameterValueKeyProvider final String key2, final String key3) {
    }

    @InvalidateSingleCache
    @ReturnValueKeyProvider
    public void populateKeyProvider08(final String key1) {
    }

  /*  @InvalidateSingleCache
    public void populateKeyProvider09(@ParameterValueKeyProvider(keyProviderBeanName = SAMPLE_PARAM_BEAN, order = 1) final String key1,
            @ParameterValueKeyProvider(keyProviderBeanName = "anotherSampleBean", order = 0) final String key2) {
    }*/

    @UpdateAssignCache
    public void populateData01(final String key1) {
    }

    @UpdateAssignCache
    public void populateData02(@ParameterDataUpdateContent final String key1, @ParameterDataUpdateContent final String key2) {
    }

    @UpdateAssignCache
    @ReturnDataUpdateContent
    public String populateData03(final String key1) {
        return null;
    }

    @UpdateAssignCache
    public void populateData04(final String key1, @ParameterDataUpdateContent final String key2) {
    }

}
