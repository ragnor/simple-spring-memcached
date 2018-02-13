/*
 * Copyright (c) 2018 Jakub Białek
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

package com.google.code.ssm.config;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

import com.google.code.ssm.CacheFactory;
import com.google.code.ssm.Settings;
import com.google.code.ssm.aop.CacheBase;
import com.google.code.ssm.aop.InvalidateAssignCacheAdvice;
import com.google.code.ssm.aop.InvalidateMultiCacheAdvice;
import com.google.code.ssm.aop.InvalidateSingleCacheAdvice;
import com.google.code.ssm.aop.ReadThroughAssignCacheAdvice;
import com.google.code.ssm.aop.ReadThroughMultiCacheAdvice;
import com.google.code.ssm.aop.ReadThroughSingleCacheAdvice;
import com.google.code.ssm.aop.UpdateAssignCacheAdvice;
import com.google.code.ssm.aop.UpdateMultiCacheAdvice;
import com.google.code.ssm.aop.UpdateSingleCacheAdvice;
import com.google.code.ssm.aop.counter.DecrementCounterInCacheAdvice;
import com.google.code.ssm.aop.counter.IncrementCounterInCacheAdvice;
import com.google.code.ssm.aop.counter.ReadCounterFromCacheAdvice;
import com.google.code.ssm.aop.counter.UpdateCounterInCacheAdvice;

/**
 * Basic xml-less configuration of SSM. Specific configuration should inherit from this one and provide 
 * at least implementation of {@link #defaultMemcachedClient()}.
 * 
 * <p>For example configuration for memcached instance on localhost using xmemcached:
 * <pre class="code">
 * public class LocalSSMConfiguration extends AbstractSSMConfiguration {
 *
 *    &#064;Bean
 *    &#064;Overwrite
 *    public CacheFactory defaultMemcachedClient() {
 *        final CacheConfiguration conf = new CacheConfiguration();
 *        conf.setConsistentHashing(true);
 *        final CacheFactory cf = new CacheFactory();
 *        cf.setCacheClientFactory(new com.google.code.ssm.providers.xmemcached.MemcacheClientFactoryImpl());
 *        cf.setAddressProvider(new DefaultAddressProvider("127.0.0.1:11211"));
 *        cf.setConfiguration(conf);
 *        return cf;
 *    }
 *   }
 * <pre class="code">
 * 
 * @author Jakub Białek
 * @since 4.0.0
 * 
 */
@EnableAspectJAutoProxy
public abstract class AbstractSSMConfiguration {

    @Bean
    public abstract CacheFactory defaultMemcachedClient();
    
    
    @Bean
    public Settings settings() {
        return new Settings();
    }
    
    @Bean
    protected CacheBase cacheBase(ApplicationContext applicationContext) {
       final CacheBase cacheBase = new CacheBase();
       cacheBase.setApplicationContext(applicationContext);
       return cacheBase;
    } 
    
    @Bean
    ReadThroughSingleCacheAdvice readThroughSingleCache(final CacheBase cacheBase) {
        final ReadThroughSingleCacheAdvice advice = new ReadThroughSingleCacheAdvice();
        advice.setCacheBase(cacheBase);
        return advice;
    }

    @Bean
    ReadThroughMultiCacheAdvice readThroughMultiCache(final CacheBase cacheBase) {
        final ReadThroughMultiCacheAdvice advice = new ReadThroughMultiCacheAdvice();
        advice.setCacheBase(cacheBase);
        return advice;
    }
    
    @Bean
    ReadThroughAssignCacheAdvice readThroughAssignCache(final CacheBase cacheBase) {
        final ReadThroughAssignCacheAdvice advice = new ReadThroughAssignCacheAdvice();
        advice.setCacheBase(cacheBase);
        return advice;
    }
    
    @Bean
    UpdateSingleCacheAdvice updateSingleCache(final CacheBase cacheBase) {
        final UpdateSingleCacheAdvice advice = new UpdateSingleCacheAdvice();
        advice.setCacheBase(cacheBase);
        return advice;
    }
    
    @Bean
    UpdateMultiCacheAdvice updateMultiCache(final CacheBase cacheBase) {
        final UpdateMultiCacheAdvice advice = new UpdateMultiCacheAdvice();
        advice.setCacheBase(cacheBase);
        return advice;
    }
    
    @Bean
    UpdateAssignCacheAdvice updateAssignCache(final CacheBase cacheBase) {
        final UpdateAssignCacheAdvice advice = new UpdateAssignCacheAdvice();
        advice.setCacheBase(cacheBase);
        return advice;
    }
   
    @Bean
    InvalidateSingleCacheAdvice invalidateSingleCache(final CacheBase cacheBase) {
        final InvalidateSingleCacheAdvice advice = new InvalidateSingleCacheAdvice();
        advice.setCacheBase(cacheBase);
        return advice;
    }
    
    @Bean
    InvalidateMultiCacheAdvice invalidateMultiCache(final CacheBase cacheBase) {
        final InvalidateMultiCacheAdvice advice = new InvalidateMultiCacheAdvice();
        advice.setCacheBase(cacheBase);
        return advice;
    }
    
    @Bean
    InvalidateAssignCacheAdvice invalidateAssignCache(final CacheBase cacheBase) {
        final InvalidateAssignCacheAdvice advice = new InvalidateAssignCacheAdvice();
        advice.setCacheBase(cacheBase);
        return advice;
    }
    
    @Bean
    IncrementCounterInCacheAdvice incrementCounterInCache(final CacheBase cacheBase) {
        final IncrementCounterInCacheAdvice advice = new IncrementCounterInCacheAdvice();
        advice.setCacheBase(cacheBase);
        return advice;
    }
   
    @Bean
    DecrementCounterInCacheAdvice decrementCounterInCache(final CacheBase cacheBase) {
        final DecrementCounterInCacheAdvice advice = new DecrementCounterInCacheAdvice();
        advice.setCacheBase(cacheBase);
        return advice;
    }
    
    @Bean
    ReadCounterFromCacheAdvice readCounterFromCache(final CacheBase cacheBase) {
        final ReadCounterFromCacheAdvice advice = new ReadCounterFromCacheAdvice();
        advice.setCacheBase(cacheBase);
        return advice;
    }
    
    @Bean
    UpdateCounterInCacheAdvice updateCounterInCache(final CacheBase cacheBase) {
        final UpdateCounterInCacheAdvice advice = new UpdateCounterInCacheAdvice();
        advice.setCacheBase(cacheBase);
        return advice;
    }
    
}
