package com.google.code.ssm.transcoders;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import com.google.code.ssm.providers.CachedObject;
import com.google.code.ssm.providers.CachedObjectImpl;
import com.google.code.ssm.providers.CacheTranscoder;

import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Copyright (c) 2010, 2011 Jakub Białek
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
 * Transcoder responsible to decode and encode between given type and JSON format.
 * 
 * @author Jakub Białek
 * 
 * @param <T>
 *            the type of class to transcode
 */
public class JsonTranscoder<T> implements CacheTranscoder<T> {

    private static final Logger LOGGER = LoggerFactory.getLogger(JsonTranscoder.class);

    private static final int JSON_SERIALIZED = 8; // json format

    private CacheTranscoder<? super T> defaultTranscoder;

    private ObjectMapper mapper;

    private Class<T> clazz;

    public JsonTranscoder(Class<T> clazz, ObjectMapper mapper, CacheTranscoder<? super T> defaultTranscoder) {
        this.clazz = clazz;
        this.mapper = mapper;
        this.defaultTranscoder = defaultTranscoder;
    }

    public boolean asyncDecode(CachedObject data) {
        return false;
    }

    @SuppressWarnings("unchecked")
    public T decode(CachedObject data) {
        if ((data.getFlags() & JSON_SERIALIZED) == 0) {
            return (T) defaultTranscoder.decode(data);
        }

        ByteArrayInputStream bais = new ByteArrayInputStream(data.getData());

        try {
            return mapper.readValue(bais, clazz);
        } catch (IOException e) {
            LOGGER.warn(String.format("Error deserializing object to class %s", clazz.getName()), e);
            throw new RuntimeException(e);
        } finally {
            try {
                bais.close();
            } catch (IOException e) {

            }
        }
    }

    public CachedObject encode(T o) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try {
            mapper.writeValue(baos, o);
            return new CachedObjectImpl(JSON_SERIALIZED, baos.toByteArray());
        } catch (IOException e) {
            LOGGER.warn(String.format("Error serializing object %s of class %s", o, clazz.getName()), e);
            throw new RuntimeException(e);
        } finally {
            try {
                baos.close();
            } catch (IOException e) {

            }
        }
    }

    public int getMaxSize() {
        return CachedObject.MAX_SIZE;
    }

}
