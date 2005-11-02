/*
 * AllPersistenceTests.java
 * 
 * See LICENCE file for usage and redistribution terms
 * Copyright (c) 2005 Operational Dynamics
 */
package accounts.persistence;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * @author Andrew Cowie
 */
public class AllPersistenceTests
{
	public static Test suite() {
		TestSuite suite = new TestSuite("Test for accounts.persistence");

		suite.addTestSuite(BasicDb4oTest.class);
		suite.addTestSuite(Db4oSetTest.class);

		return suite;
	}
}