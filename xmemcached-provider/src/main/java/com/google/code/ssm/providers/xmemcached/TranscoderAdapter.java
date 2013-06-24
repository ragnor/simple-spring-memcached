/*
 * Copyright (c) 2010-2013 Jakub Białek
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

package com.google.code.ssm.providers.xmemcached;

import net.rubyeye.xmemcached.transcoders.CachedData;
import net.rubyeye.xmemcached.transcoders.CompressionMode;
import net.rubyeye.xmemcached.transcoders.Transcoder;

import com.google.code.ssm.providers.CacheTranscoder;
import com.google.code.ssm.providers.CachedObject;

/**
 * 
 * @author Jakub Białek
 * @since 2.0.0
 * 
 */
class TranscoderAdapter implements Transcoder<Object> {

    private final CacheTranscoder transcoder;

    TranscoderAdapter(final CacheTranscoder transcoder) {
        this.transcoder = transcoder;
    }

    @Override
    public Object decode(final CachedData d) {
        return transcoder.decode(new CachedObjectWrapper(d));
    }

    @Override
    public CachedData encode(final Object o) {
        CachedObject cachedObject = transcoder.encode(o);
        return new CachedData(cachedObject.getFlags(), cachedObject.getData());
    }

    @Override
    public boolean isPackZeros() {
        throw new UnsupportedOperationException("TranscoderAdapter doesn't support pack zeros");
    }

    @Override
    public boolean isPrimitiveAsString() {
        return false;
    }

    @Override
    public void setCompressionThreshold(final int compressionThreshold) {
        throw new UnsupportedOperationException("TranscoderAdapter doesn't support compression threshold");
    }

    @Override
    public void setPackZeros(final boolean packZeros) {
        throw new UnsupportedOperationException("TranscoderAdapter doesn't support pack zeros");
    }

    @Override
    public void setPrimitiveAsString(final boolean primitiveAsString) {
        throw new UnsupportedOperationException("TranscoderAdapter doesn't support primitive as string");
    }

    @Override
    public void setCompressionMode(final CompressionMode compressionMode) {
        throw new UnsupportedOperationException("TranscoderAdapter doesn't support compression mode");
    }

}
