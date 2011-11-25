package com.google.code.ssm;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.security.InvalidParameterException;

import org.apache.commons.lang.RandomStringUtils;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.code.ssm.impl.CacheKeyBuilderImpl;

public class CacheKeyBuilderImplTest {
    
    private static CacheKeyBuilderImpl cacheKeyBuilder;

    @BeforeClass
    public static void beforeClass() {
        cacheKeyBuilder = new CacheKeyBuilderImpl();
    }


    @Test
    public void getAssignCacheKey() {
        try {
            cacheKeyBuilder.getAssignCacheKey((String) null, (String) null);
            fail("Expected exception.");
        } catch (InvalidParameterException ex) {
            assertTrue(ex.getMessage().indexOf("at least 1 character") != -1);
            System.out.println(ex.getMessage());
        }

        try {
            cacheKeyBuilder.getAssignCacheKey("", (String) null);
            fail("Expected exception.");
        } catch (InvalidParameterException ex) {
            assertTrue(ex.getMessage().indexOf("at least 1 character") != -1);
            System.out.println(ex.getMessage());
        }

        final String objectId = RandomStringUtils.randomAlphanumeric(20);
        final String namespace = RandomStringUtils.randomAlphanumeric(12);

        final String result = cacheKeyBuilder.getAssignCacheKey(objectId, namespace);

        assertTrue(result.indexOf(objectId) != -1);
        assertTrue(result.indexOf(namespace) != -1);
    }

    
}
