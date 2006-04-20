/*
 * AllTests.java
 * 
 * See LICENCE file for usage and redistribution terms
 * Copyright (c) 2005-2006 Operational Dynamics
 */

import generic.junit.VerboseTestRunner;
import junit.framework.Test;
import junit.framework.TestSuite;
import accounts.client.Db4oActivationTest;
import accounts.domain.BasicAccountTest;
import accounts.domain.BasicLedgerTest;
import accounts.domain.BasicLoanTest;
import accounts.domain.BasicTransactionTest;
import accounts.domain.DatestampTest;
import accounts.domain.ForeignCurrencyTest;
import accounts.domain.GenericTransactionTest;
import accounts.domain.IdentifierTest;
import accounts.persistence.BasicDb4oTest;
import accounts.persistence.EngineTest;
import accounts.persistence.ExposeDb4oQueryInterfaceTest;
import accounts.persistence.RollbackDb4oTest;
import accounts.services.BasicCommandTest;
import accounts.services.BooksSetByCommandsTest;
import accounts.services.ComparatorsDetailedTest;
import accounts.services.ComparatorsInTreeSetTest;
import accounts.services.EntityCommandTest;
import accounts.services.IdentifierCommandsTest;
import accounts.services.PostTransactionCommandTest;
import country.au.services.AustralianPayrollTaxTest;

/**
 * @author Andrew Cowie
 */
public class UnitTests
{
	public static void main(String[] args) {
		VerboseTestRunner.run(suite());
	}

	public static Test suite() {
		TestSuite suite = new TestSuite("All Unit Tests for ObjectiveAccounts");

		// persistence
		suite.addTestSuite(BasicDb4oTest.class);
		suite.addTestSuite(RollbackDb4oTest.class);
		suite.addTestSuite(EngineTest.class);
		suite.addTestSuite(ExposeDb4oQueryInterfaceTest.class);

		// domain
		suite.addTestSuite(BasicAccountTest.class);
		suite.addTestSuite(BasicLedgerTest.class);
		suite.addTestSuite(BasicTransactionTest.class);
		suite.addTestSuite(GenericTransactionTest.class);
		suite.addTestSuite(DatestampTest.class);
		suite.addTestSuite(BasicLoanTest.class);
		suite.addTestSuite(ForeignCurrencyTest.class);
		suite.addTestSuite(IdentifierTest.class);

		// services
		suite.addTestSuite(BasicCommandTest.class);
		suite.addTestSuite(BooksSetByCommandsTest.class);
		suite.addTestSuite(PostTransactionCommandTest.class);
		suite.addTestSuite(ComparatorsDetailedTest.class);
		suite.addTestSuite(ComparatorsInTreeSetTest.class);
		suite.addTestSuite(EntityCommandTest.class);
		suite.addTestSuite(IdentifierCommandsTest.class);
		suite.addTestSuite(AustralianPayrollTaxTest.class);

		// client
		suite.addTestSuite(Db4oActivationTest.class);

		return suite;
	}
}