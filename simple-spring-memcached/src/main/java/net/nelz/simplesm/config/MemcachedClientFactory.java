package net.nelz.simplesm.config;

import net.spy.memcached.*;

import java.io.*;
import java.net.*;
import java.util.*;

/**
Copyright (c) 2008, 2009  Nelson Carpentier

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
public class MemcachedClientFactory {
	private MemcachedConnectionBean bean;

	public void setBean(MemcachedConnectionBean bean) {
		this.bean = bean;
	}

	public MemcachedClientIF createMemcachedClient() throws IOException
	{
		if (this.bean == null) {
			throw new RuntimeException("The MemcachedConnectionBean must be defined!");
		}
		final List<InetSocketAddress> addrs = AddrUtil.getAddresses(this.bean.getNodeList());
		final ConnectionFactory connectionFactory = this.bean.isConsistentHashing() ?
				new KetamaConnectionFactory() : new DefaultConnectionFactory();
		final MemcachedClientIF client = new MemcachedClient(connectionFactory, addrs);
		return client;
	}
}
