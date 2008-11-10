package net.nelz.simplesm.aop;

import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.*;

/**
 * Copyright 2008 Widgetbox, Inc.
 * All rights reserved.
 * THIS PROGRAM IS CONFIDENTIAL AND AN UNPUBLISHED WORK AND TRADE
 * SECRET OF THE COPYRIGHT HOLDER, AND DISTRIBUTED ONLY UNDER RESTRICTION.
 */
public class CacheKeyMethodStoreImpl implements CacheKeyMethodStore {
	final Map<Class, Method> map = new ConcurrentHashMap<Class, Method>();

	public void add(final Class key, final Method value) {
		map.put(key, value);
	}

	public Method find(final Class key) {
		return map.get(key);
	}
}
