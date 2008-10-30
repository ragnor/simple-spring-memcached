package net.nelz.simplesm.test;

import static org.testng.AssertJUnit.*;
import net.nelz.simplesm.test.svc.*;
import org.springframework.context.*;
import org.springframework.context.support.*;
import org.testng.annotations.*;

/**
 * Copyright 2008 Widgetbox, Inc.
 * All rights reserved.
 * THIS PROGRAM IS CONFIDENTIAL AND AN UNPUBLISHED WORK AND TRADE
 * SECRET OF THE COPYRIGHT HOLDER, AND DISTRIBUTED ONLY UNDER RESTRICTION.
 */
public class RunTest {

	private ApplicationContext context;

	@BeforeClass
	public void beforeClass() {
		context = new ClassPathXmlApplicationContext("/test-context.xml");
	}

	@Test
	public void testIndividual() {
		final TestSvc test = (TestSvc) context.getBean("testSvc");

		final String s1 = test.getDateString("bubba");
		for (int ix = 0; ix < 10; ix++) {
			assertEquals(String.format("Cache didn't seem to bring back [%s] as expectd.", s1), s1, test.getDateString("bubba"));
		}
		fail("FINISH WRITING TEST!!!");
	}
}
