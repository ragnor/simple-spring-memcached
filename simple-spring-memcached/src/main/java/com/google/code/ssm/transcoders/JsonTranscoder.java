/*
 * Copyright (c) 2010-2014 Jakub Białek
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

package com.google.code.ssm.transcoders;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import com.google.code.ssm.json.Holder;
import com.google.code.ssm.providers.CacheTranscoder;
import com.google.code.ssm.providers.CachedObject;
import com.google.code.ssm.providers.CachedObjectImpl;

/**
 * 
 * Transcoder responsible to decode and encode objects from/to JSON format.
 * 
 * @author Jakub Białek
 * @since 2.0.0
 * 
 */
@ToString
@EqualsAndHashCode
public class JsonTranscoder implements CacheTranscoder { // NO_UCD

    private static final Logger LOGGER = LoggerFactory.getLogger(JsonTranscoder.class);

    private static final int JSON_SERIALIZED = 8; // json format

    @Getter
    private final ObjectMapper mapper;

    public JsonTranscoder(final ObjectMapper mapper) {
        Assert.notNull(mapper, "'mapper' is required and cannot be null");

        this.mapper = mapper;
    }

    public boolean asyncDecode(final CachedObject data) {
        return false;
    }

    @Override
    public Object decode(final CachedObject data) {
        if ((data.getFlags() & JSON_SERIALIZED) == 0) {
            LOGGER.warn("Cannot decode cached data {} using json transcoder", data);
            throw new RuntimeException("Cannot decode cached data using json transcoder");
        }

        ByteArrayInputStream bais = new ByteArrayInputStream(data.getData());

        try {
            return mapper.readValue(bais, Holder.class).getValue();
        } catch (IOException e) {
            LOGGER.warn(String.format("Error deserializing cached data %s", data.toString()), e);
            throw new RuntimeException(e);
        } finally {
            try {
                bais.close();
            } catch (IOException e) {
                LOGGER.warn("Error while closing stream", e);
            }
        }
    }

    @Override
    public CachedObject encode(final Object o) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try {
            mapper.writeValue(baos, new Holder(o));
            return new CachedObjectImpl(JSON_SERIALIZED, baos.toByteArray());
        } catch (IOException e) {
            LOGGER.warn(String.format("Error serializing object %s", o), e);
            throw new RuntimeException(e);
        } finally {
            try {
                baos.close();
            } catch (IOException e) {
                LOGGER.warn("Error while closing stream", e);
            }
        }
    }

    public int getMaxSize() {
        return CachedObject.MAX_SIZE;
    }

}
