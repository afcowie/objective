/*
 * AllClientTests.java
 * 
 * See LICENCE file for usage and redistribution terms
 * Copyright (c) 2005 Operational Dynamics
 */
package accounts.client;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * @author Andrew Cowie
 */
public class AllClientTests
{
	public static Test suite() {
		TestSuite suite = new TestSuite("Test for accounts.client");

		suite.addTestSuite(Db4oActivationTest.class);

		return suite;
	}
}
