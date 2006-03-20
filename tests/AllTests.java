/*
 * AllTests.java
 * 
 * See LICENCE file for usage and redistribution terms
 * Copyright (c) 2005-2006 Operational Dynamics
 */

import generic.junit.VerboseTestRunner;
import junit.framework.Test;
import junit.framework.TestSuite;
import accounts.client.AllClientTests;
import accounts.domain.AllDomainTests;
import accounts.persistence.AllPersistenceTests;
import accounts.services.AllServicesTests;

/**
 * @author Andrew Cowie
 */
public class AllTests
{
	public static void main(String[] args) {
		VerboseTestRunner.run(suite());
	}

	public static Test suite() {
		TestSuite suite = new TestSuite("All Unit Tests for ObjectiveAccounts");

		suite.addTest(AllPersistenceTests.suite());
		suite.addTest(AllDomainTests.suite());
		suite.addTest(AllServicesTests.suite());
		suite.addTest(AllClientTests.suite());

		return suite;
	}
}