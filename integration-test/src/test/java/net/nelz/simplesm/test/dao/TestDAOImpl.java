package net.nelz.simplesm.test.dao;

import net.nelz.simplesm.annotations.*;
import org.springframework.stereotype.*;

import java.util.*;

/**
 * Copyright 2008 Widgetbox, Inc.
 * All rights reserved.
 * THIS PROGRAM IS CONFIDENTIAL AND AN UNPUBLISHED WORK AND TRADE
 * SECRET OF THE COPYRIGHT HOLDER, AND DISTRIBUTED ONLY UNDER RESTRICTION.
 */
@Repository("testDao")
public class TestDAOImpl implements TestDAO {

	@SSMIndividual(namespace = "Alpha", keyIndex = 0, expiration = 30)
	public String getDateString(final String key) {
		final Date now = new Date();
		try {
			Thread.sleep(1000);
		} catch (InterruptedException ex) {}
		return now.toString();
	}

}
