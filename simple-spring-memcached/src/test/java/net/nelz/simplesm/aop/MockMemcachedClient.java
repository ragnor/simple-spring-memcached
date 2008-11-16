package net.nelz.simplesm.aop;

import net.spy.memcached.*;
import net.spy.memcached.transcoders.*;
import org.apache.commons.lang.math.*;

import java.net.*;
import java.util.*;
import java.util.concurrent.*;

/**
 * Copyright 2008 Widgetbox, Inc.
 * All rights reserved.
 * THIS PROGRAM IS CONFIDENTIAL AND AN UNPUBLISHED WORK AND TRADE
 * SECRET OF THE COPYRIGHT HOLDER, AND DISTRIBUTED ONLY UNDER RESTRICTION.
 */
public class MockMemcachedClient implements MemcachedClientIF {

	private Map<String, Object> map = new HashMap<String, Object>();

	public Collection<SocketAddress> getAvailableServers() {
		return null;
	}

	public Collection<SocketAddress> getUnavailableServers() {
		return null;
	}

	public void setTranscoder(Transcoder<Object> objectTranscoder) {

	}

	public Transcoder<Object> getTranscoder() {
		return null;
	}

	public Future<Boolean> append(long l, String s, Object o) {
		return null;
	}

	public <T> Future<Boolean> append(long l, String s, T t, Transcoder<T> tTranscoder) {
		return null;
	}

	public Future<Boolean> prepend(long l, String s, Object o) {
		return null;
	}

	public <T> Future<Boolean> prepend(long l, String s, T t, Transcoder<T> tTranscoder) {
		return null;
	}

	public <T> Future<CASResponse> asyncCAS(String s, long l, T t, Transcoder<T> tTranscoder) {
		return null;
	}

	public Future<CASResponse> asyncCAS(String s, long l, Object o) {
		return null;
	}

	public <T> CASResponse cas(String s, long l, T t, Transcoder<T> tTranscoder) throws OperationTimeoutException {
		return null;
	}

	public CASResponse cas(String s, long l, Object o) throws OperationTimeoutException {
		return null;
	}

	public <T> Future<Boolean> add(String s, int i, T t, Transcoder<T> tTranscoder) {
		return null;
	}

	public Future<Boolean> add(String s, int i, Object o) {
		return null;
	}

	public <T> Future<Boolean> set(String s, int i, T t, Transcoder<T> tTranscoder) {
		return null;
	}

	public Future<Boolean> set(String s, int i, Object o) {
		return null;
	}

	public <T> Future<Boolean> replace(String s, int i, T t, Transcoder<T> tTranscoder) {
		return null;
	}

	public Future<Boolean> replace(String s, int i, Object o) {
		return null;
	}

	public <T> Future<T> asyncGet(String s, Transcoder<T> tTranscoder) {
		return null;
	}

	public Future<Object> asyncGet(String s) {
		return null;
	}

	public <T> Future<CASValue<T>> asyncGets(String s, Transcoder<T> tTranscoder) {
		return null;
	}

	public Future<CASValue<Object>> asyncGets(String s) {
		return null;
	}

	public <T> CASValue<T> gets(String s, Transcoder<T> tTranscoder) throws OperationTimeoutException {
		return null;
	}

	public CASValue<Object> gets(String s) throws OperationTimeoutException {
		return null;
	}

	public <T> T get(String s, Transcoder<T> tTranscoder) throws OperationTimeoutException {
		return null;
	}

	public Object get(String s) throws OperationTimeoutException {
		return null;
	}

	public <T> Future<Map<String, T>> asyncGetBulk(Collection<String> strings, Transcoder<T> tTranscoder) {
		return null;
	}

	public Future<Map<String, Object>> asyncGetBulk(Collection<String> strings) {
		return null;
	}

	public <T> Future<Map<String, T>> asyncGetBulk(Transcoder<T> tTranscoder, String... strings) {
		return null;
	}

	public Future<Map<String, Object>> asyncGetBulk(String... strings) {
		return null;
	}

	public <T> Map<String, T> getBulk(Collection<String> strings, Transcoder<T> tTranscoder) throws OperationTimeoutException {
		return null;
	}

	public Map<String, Object> getBulk(Collection<String> strings) throws OperationTimeoutException {
		final Map<String, Object> results = new HashMap<String, Object>();
		for (final String key : strings) {
			final Object result = map.get(key);
			if (result != null) {
				results.put(key, result);
			} else if (RandomUtils.nextBoolean()) {
				results.put(key, result);
			}
		}
		return results;
	}

	public <T> Map<String, T> getBulk(Transcoder<T> tTranscoder, String... strings) throws OperationTimeoutException {
		return null;
	}

	public Map<String, Object> getBulk(String... strings) throws OperationTimeoutException {
		return null;
	}

	public Map<SocketAddress, String> getVersions() {
		return null;
	}

	public Map<SocketAddress, Map<String, String>> getStats() {
		return null;
	}

	public long incr(String s, int i) throws OperationTimeoutException {
		return 0;
	}

	public long decr(String s, int i) throws OperationTimeoutException {
		return 0;
	}

	public long incr(String s, int i, long l) throws OperationTimeoutException {
		return 0;
	}

	public long decr(String s, int i, long l) throws OperationTimeoutException {
		return 0;
	}

	public Future<Boolean> delete(String s, int i) {
		return null;
	}

	public Future<Boolean> delete(String s) {
		return null;
	}

	public Future<Boolean> flush(int i) {
		return null;
	}

	public Future<Boolean> flush() {
		return null;
	}

	public void shutdown() {

	}

	public boolean shutdown(long l, TimeUnit timeUnit) {
		return false;
	}

	public boolean waitForQueues(long l, TimeUnit timeUnit) {
		return false;
	}
}
