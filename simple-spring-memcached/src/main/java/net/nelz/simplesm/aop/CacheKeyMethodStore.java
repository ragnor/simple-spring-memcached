package net.nelz.simplesm.aop;

import java.lang.reflect.*;

/**
 * Copyright 2008 Widgetbox, Inc.
 * All rights reserved.
 * THIS PROGRAM IS CONFIDENTIAL AND AN UNPUBLISHED WORK AND TRADE
 * SECRET OF THE COPYRIGHT HOLDER, AND DISTRIBUTED ONLY UNDER RESTRICTION.
 */
public interface CacheKeyMethodStore {
	public void add(Class key, Method value);

	public Method find(Class key);
}
