package net.nelz.simplesm.config;

import net.spy.memcached.*;
import static org.testng.AssertJUnit.*;
import org.testng.annotations.*;

import java.io.*;

/**
Copyright (c) 2008  Nelson Carpentier

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
 */
public class MemcachedClientFactoryTest {

	@Test
	public void testCreateClientException() throws IOException {
		final MemcachedClientFactory factory = new MemcachedClientFactory();
		try {
			factory.createMemcachedClient();
			fail("Expected Exception.");
		} catch (RuntimeException ex) {
			assertTrue(true);
		}
	}

	@Test
	public void testCreateClient() throws IOException {
		final MemcachedConnectionBean bean = new MemcachedConnectionBean();
		bean.setConsistentHashing(false);
		bean.setNodeList("127.0.0.1:11211");
		final MemcachedClientFactory factory = new MemcachedClientFactory();
		factory.setBean(bean);

		MemcachedClientIF cache = factory.createMemcachedClient();
		assertNotNull(cache);

		bean.setConsistentHashing(true);
		cache = factory.createMemcachedClient();
		assertNotNull(cache);
	}
}
