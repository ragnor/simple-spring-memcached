package net.nelz.simplesm.annotations;

import java.lang.annotation.*;

/**
 * Copyright 2008 Widgetbox, Inc.
 * All rights reserved.
 * THIS PROGRAM IS CONFIDENTIAL AND AN UNPUBLISHED WORK AND TRADE
 * SECRET OF THE COPYRIGHT HOLDER, AND DISTRIBUTED ONLY UNDER RESTRICTION.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface SSMIndividual {
	public static final String DEFAULT_STRING = "[unassigned]";
 	String namespace() default DEFAULT_STRING;
	int keyIndex() default 0;
	int expiration() default 0;
}
