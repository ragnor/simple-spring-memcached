package net.nelz.simplesm.aop;

import static org.testng.AssertJUnit.*;
import org.testng.annotations.*;

/**
 * Copyright 2008 Widgetbox, Inc.
 * All rights reserved.
 * THIS PROGRAM IS CONFIDENTIAL AND AN UNPUBLISHED WORK AND TRADE
 * SECRET OF THE COPYRIGHT HOLDER, AND DISTRIBUTED ONLY UNDER RESTRICTION.
 */
public class PertinentNegativeNullTest {

	@Test
	public void testNull() {
		final PertinentNegativeNull pnn = new PertinentNegativeNull();
		assertFalse(pnn.equals(null));
		assertTrue(pnn.equals(new PertinentNegativeNull()));
		assertEquals(pnn.hashCode(), new PertinentNegativeNull().hashCode());
	}
}
