package net.nelz.simplesm.test.svc;

import net.nelz.simplesm.test.dao.*;
import org.springframework.stereotype.*;

/**
 * Copyright 2008 Widgetbox, Inc.
 * All rights reserved.
 * THIS PROGRAM IS CONFIDENTIAL AND AN UNPUBLISHED WORK AND TRADE
 * SECRET OF THE COPYRIGHT HOLDER, AND DISTRIBUTED ONLY UNDER RESTRICTION.
 */
@Service("testSvc")
public class TestSvcImpl implements TestSvc {

	private TestDAO dao;

	public void setDao(TestDAO dao) {
		this.dao = dao;
	}

	public String getDateString(final String key) {
		return this.dao.getDateString(key);
	}
}
