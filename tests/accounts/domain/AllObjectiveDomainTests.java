/*
 * AllObjectiveDomainTests.java
 * 
 * See LICENCE file for usage and redistribution terms
 * Copyright (c) 2005 Operational Dynamics
 */
package accounts.domain;

import junit.framework.TestSuite;

/**
 * @author Andrew Cowie
 */
public class AllObjectiveDomainTests
{
	public static TestSuite suite() {
		TestSuite suite = new TestSuite("Test for accounts.domain");

		suite.addTestSuite(BasicAccountTest.class);
		suite.addTestSuite(BasicLedgerTest.class);
		suite.addTestSuite(BasicTransactionTest.class);
		suite.addTestSuite(GenericTransactionTest.class);
		suite.addTestSuite(DatestampTest.class);
		suite.addTestSuite(BasicLoanTest.class);
		suite.addTestSuite(ForeignCurrencyTest.class);

		return suite;
	}
}
