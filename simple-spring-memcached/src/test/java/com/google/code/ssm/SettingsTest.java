package com.google.code.ssm;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.context.ApplicationContext;

import com.google.code.ssm.aop.CacheBase;

@RunWith(MockitoJUnitRunner.class)
public class SettingsTest {

    @Mock
    private Settings globalSSMSettings;

    @Mock
    private ApplicationContext applicationContext;

    @InjectMocks
    private CacheBase cacheBase = new CacheBase();

    @Before
    public void init() {
        when(applicationContext.getBean(Settings.class)).thenReturn(globalSSMSettings);
        cacheBase.setApplicationContext(applicationContext);

    }

    @Test
    public void testCacheSettingDisabled() throws Exception {
        when(globalSSMSettings.isDisableCache()).thenReturn(true);
        cacheBase.afterPropertiesSet();
        assertTrue(cacheBase.isCacheDisabled());
    }

    @Test
    public void testCacheEnabled() throws Exception {
        when(globalSSMSettings.isDisableCache()).thenReturn(false);
        cacheBase.afterPropertiesSet();
        assertFalse(cacheBase.isCacheDisabled());
    }
}
