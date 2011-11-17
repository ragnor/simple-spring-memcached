package net.nelz.simplesm.aop;

import static org.mockito.Mockito.when;
import net.nelz.simplesm.api.KeyProvider;
import net.nelz.simplesm.providers.MemcacheClient;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * Copyright (c) 2011 Jakub Białek
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
 * @author Jakub Białek
 * 
 */
@RunWith(Parameterized.class)
public abstract class AbstractCacheTest<T extends CacheBase> {

    @Mock
    protected MemcacheClient client;

    @Mock
    protected KeyProvider keyProvider;

    @Mock
    protected CacheKeyMethodStore methodStore;

    @Mock
    protected ProceedingJoinPoint pjp;

    @Mock
    protected MethodSignature signature;
    
    protected T advice;

    protected final boolean isValid;
    
    protected final String methodName;

    protected final Class<?>[] paramTypes;

    protected final Object[] params;

    protected String cacheKey;
    
    protected AbstractCacheTest(boolean isValid, String methodName, Class<?>[] paramTypes, Object[] params, String cacheKey) {
        this.isValid = isValid;
        this.methodName = methodName;
        this.paramTypes = paramTypes;
        this.params = params;
        this.cacheKey = cacheKey;
    }
    
    public void setUp(Object testService) {
        MockitoAnnotations.initMocks(this);
        advice = createAdvice();
        advice.setCache(client);
        advice.setDefaultKeyProvider(keyProvider);
        advice.setMethodStore(methodStore);
        
        when(signature.getName()).thenReturn(methodName);
        when(signature.getParameterTypes()).thenReturn(paramTypes);
        when(pjp.getSignature()).thenReturn(signature);
        when(pjp.toShortString()).thenReturn(methodName);
        when(pjp.getArgs()).thenReturn(params);
        
        when(signature.getDeclaringType()).thenReturn(testService.getClass());
        when(pjp.getTarget()).thenReturn(testService);
        
        if (cacheKey == null) {
            cacheKey = getKey(getNamespace(), params);
        }
        
    } 
    
    protected String getKey(String namespace, Object... params) {
        StringBuilder sb = new StringBuilder(namespace);
        sb.append(":");
        for (Object param : params) {
            sb.append(param.toString());
            sb.append("/");
        }

        sb.deleteCharAt(sb.length() - 1);
        return sb.toString();
    }
    
    protected abstract T createAdvice();
    
    protected abstract String getNamespace();
    
}
