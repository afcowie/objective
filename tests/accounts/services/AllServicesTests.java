/*
 * AllServicesTests.java
 * 
 * See LICENCE file for usage and redistribution terms
 * Copyright (c) 2005 Operational Dynamics
 */
package accounts.services;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * @author Andrew Cowie
 */
public class AllServicesTests
{
	public static Test suite() {
		TestSuite suite = new TestSuite("Test for accounts.services");

		suite.addTestSuite(DatafileTest.class);
		suite.addTestSuite(BasicCommandTest.class);
		suite.addTestSuite(TransactionCommandTest.class);

		return suite;
	}
}
