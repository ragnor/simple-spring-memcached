package net.nelz.simplesm.aop;

/**
 * Copyright 2008 Widgetbox, Inc.
 * All rights reserved.
 * THIS PROGRAM IS CONFIDENTIAL AND AN UNPUBLISHED WORK AND TRADE
 * SECRET OF THE COPYRIGHT HOLDER, AND DISTRIBUTED ONLY UNDER RESTRICTION.
 */
public class PertinentNegativeNull {
	public int hashCode() {
		return 1;
	}

	public boolean equals(final Object obj) {
		if (obj == null) { return false;}
		return (obj instanceof PertinentNegativeNull);
	}
}
