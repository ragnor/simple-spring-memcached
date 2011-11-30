package com.google.code.ssm.providers;

import java.util.Collection;
import java.util.concurrent.TimeoutException;

/**
 * 
 * @author Jakub Białek
 * @since 2.0.0
 *
 */
public abstract class AbstractMemcacheClientWrapper implements CacheClient {
    
    @Override
    public void delete(Collection<String> keys) throws TimeoutException, CacheException {
        if (keys != null && keys.size() > 0) {
            for (final String key : keys) {
                if (key != null) {
                    delete(key);
                }
            }
        }
    }
    
}
