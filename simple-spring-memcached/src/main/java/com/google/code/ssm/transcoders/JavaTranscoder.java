/*
 * Copyright (c) 2012 Jakub Białek
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
import java.io.Closeable;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.code.ssm.providers.CacheTranscoder;
import com.google.code.ssm.providers.CachedObject;
import com.google.code.ssm.providers.CachedObjectImpl;

/**
 * 
 * Transcoder responsible to decode and encode objects using default java serialization/deserialization. Before storing
 * data if size of data is bigger than defined {@link JavaTranscoder#setCompressionThreshold(int)} those data are
 * compressed using GZIP. This transcoder is similar to SerializingTranscoder in xmemcached or spymemcached.
 * 
 * @author Jakub Białek
 * @since 3.0.0
 * 
 */
@ToString
@EqualsAndHashCode
public class JavaTranscoder implements CacheTranscoder { // NO_UCD

    /**
     * Default compression threshold value.
     */
    public static final int DEFAULT_COMPRESSION_THRESHOLD = 16384;

    public static final String DEFAULT_CHARSET = "UTF-8";

    private static final Logger LOGGER = LoggerFactory.getLogger(JavaTranscoder.class);

    // General flags
    private static final int SERIALIZED = 1;
    private static final int COMPRESSED = 2;

    @Getter
    @Setter
    private int compressionThreshold = DEFAULT_COMPRESSION_THRESHOLD;

    @Override
    public Object decode(final CachedObject d) {
        byte[] data = d.getData();

        if ((d.getFlags() & COMPRESSED) != 0) {
            data = decompress(d.getData());
        }

        if ((d.getFlags() & SERIALIZED) != 0 && data != null) {
            return deserialize(data);
        } else {
            LOGGER.warn("Cannot decode cached data {} using java transcoder", data);
            throw new RuntimeException("Cannot decode cached data using java transcoder");
        }
    }

    @Override
    public CachedObject encode(final Object o) {
        byte[] data = serialize(o);
        int flags = SERIALIZED;

        if (data.length > getCompressionThreshold()) {
            byte[] compressed = compress(data);
            if (compressed.length < data.length) {
                LOGGER.debug("Compressed {} from {} to {}", new Object[] { o.getClass().getName(), data.length, compressed.length });
                data = compressed;
                flags |= COMPRESSED;
            } else {
                LOGGER.info("Compression increased the size of {} from {} to {}", new Object[] { o.getClass().getName(), data.length,
                        compressed.length });
            }
        }
        return new CachedObjectImpl(flags, data);
    }

    /**
     * Serialize object using java serialization.
     * 
     * @param o
     *            object to serialize
     * @return serialized data
     */
    protected byte[] serialize(final Object o) {
        if (o == null) {
            throw new NullPointerException("Can't serialize null");
        }
        byte[] data = null;
        ByteArrayOutputStream bos = null;
        ObjectOutputStream os = null;

        try {
            bos = new ByteArrayOutputStream();
            os = new ObjectOutputStream(bos);
            os.writeObject(o);
            os.close();
            bos.close();
            data = bos.toByteArray();
        } catch (IOException e) {
            throw new IllegalArgumentException("Non-serializable object", e);
        } finally {
            close(os);
            close(bos);
        }

        return data;
    }

    /**
     * Deserialize given stream using java deserialization.
     * 
     * @param in
     *            data to deserialize
     * @return deserialized object
     */
    protected Object deserialize(final byte[] in) {
        Object o = null;
        ByteArrayInputStream bis = null;
        ObjectInputStream is = null;

        try {
            if (in != null) {
                bis = new ByteArrayInputStream(in);
                is = new ObjectInputStream(bis);
                o = is.readObject();
                is.close();
                bis.close();
            }
        } catch (IOException e) {
            LOGGER.warn(String.format("Caught IOException decoding %d bytes of data", in.length), e);
        } catch (ClassNotFoundException e) {
            LOGGER.warn(String.format("Caught CNFE decoding %d bytes of data", in.length), e);
        } finally {
            close(is);
            close(bis);
        }

        return o;
    }

    /**
     * Compress the given array of bytes.
     * 
     * @param in
     *            data to compress
     */
    protected byte[] compress(final byte[] in) {
        if (in == null) {
            throw new NullPointerException("Can't compress null");
        }

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        GZIPOutputStream gz = null;

        try {
            gz = new GZIPOutputStream(bos);
            gz.write(in);
        } catch (IOException e) {
            throw new RuntimeException("IO exception compressing data", e);
        } finally {
            close(gz);
            close(bos);
        }

        return bos.toByteArray();
    }

    /**
     * Decompress the given array of bytes.
     * 
     * @param in
     *            data to decompress
     * @return null if the bytes cannot be decompressed
     */
    protected byte[] decompress(final byte[] in) {
        if (in == null) {
            return null;
        }

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ByteArrayInputStream bis = new ByteArrayInputStream(in);
        GZIPInputStream gis = null;

        try {
            gis = new GZIPInputStream(bis);

            byte[] buf = new byte[8192];
            int r = -1;
            while ((r = gis.read(buf)) > 0) {
                bos.write(buf, 0, r);
            }

            return bos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("IO exception decompressing data", e);
        } finally {
            close(gis);
            close(bis);
            close(bos);
        }
    }

    protected void close(final Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (Exception e) {
                LOGGER.info(String.format("Unable to close %s", closeable), e);
            }
        }
    }

}
