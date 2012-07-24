package com.google.code.ssm.aop.support;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.security.InvalidParameterException;

import org.apache.commons.lang.RandomStringUtils;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.code.ssm.aop.support.AnnotationData;
import com.google.code.ssm.aop.support.CacheKeyBuilderImpl;


public class CacheKeyBuilderImplTest {
    
    private static CacheKeyBuilderImpl cacheKeyBuilder;

    @BeforeClass
    public static void beforeClass() {
        cacheKeyBuilder = new CacheKeyBuilderImpl();
    }


    @Test
    public void getAssignCacheKey() {
        AnnotationData data = new AnnotationData();
        try {
            data.setAssignedKey(null);
            data.setNamespace(null);
            cacheKeyBuilder.getAssignCacheKey(data);
            fail("Expected exception.");
        } catch (InvalidParameterException ex) {
            assertTrue(ex.getMessage().indexOf("at least 1 character") != -1);
            System.out.println(ex.getMessage());
        }

        try {
            data.setAssignedKey("");
            cacheKeyBuilder.getAssignCacheKey(data);
            fail("Expected exception.");
        } catch (InvalidParameterException ex) {
            assertTrue(ex.getMessage().indexOf("at least 1 character") != -1);
            System.out.println(ex.getMessage());
        }

        final String objectId = RandomStringUtils.randomAlphanumeric(20);
        final String namespace = RandomStringUtils.randomAlphanumeric(12);

        data.setAssignedKey(objectId);
        data.setNamespace(namespace);
        final String result = cacheKeyBuilder.getAssignCacheKey(data);

        assertTrue(result.indexOf(objectId) != -1);
        assertTrue(result.indexOf(namespace) != -1);
    }
    
}
