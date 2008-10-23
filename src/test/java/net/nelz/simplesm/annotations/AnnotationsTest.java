package net.nelz.simplesm.annotations;

import static org.testng.AssertJUnit.*;
import org.testng.annotations.*;

import java.lang.annotation.*;
import java.lang.reflect.*;

/**
 * Copyright 2008 Widgetbox, Inc.
 * All rights reserved.
 * THIS PROGRAM IS CONFIDENTIAL AND AN UNPUBLISHED WORK AND TRADE
 * SECRET OF THE COPYRIGHT HOLDER, AND DISTRIBUTED ONLY UNDER RESTRICTION.
 */
public class AnnotationsTest {

	@Test
	public void testIndividual() throws Exception {
		final Method method = RandomClass.class.getMethod("getName", null);
		final Annotation[] annotations = method.getDeclaredAnnotations();

		assertEquals(SSMIndividual.class, annotations[0].annotationType());
	}

	private static class RandomClass {
		private String name = "RandomClass";

		@SSMIndividual
		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}
	}
}
