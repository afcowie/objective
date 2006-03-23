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
import accounts.persistence.RollbackDb4oTest;
import accounts.persistence.UnitOfWorkTest;
import accounts.services.BasicCommandTest;
import accounts.services.CommandsVsUnitsOfWorkTest;
import accounts.services.ComparatorsDetailedTest;
import accounts.services.ComparatorsInTreeSetTest;
import accounts.services.DatafileTest;
import accounts.services.EntityCommandTest;
import accounts.services.IdentifierCommandsTest;
import accounts.services.PostTransactionCommandTest;
import country.au.services.AustralianPayrollTaxCalculatorTest;

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

		// persistence
		suite.addTestSuite(BasicDb4oTest.class);
		suite.addTestSuite(RollbackDb4oTest.class);
		suite.addTestSuite(UnitOfWorkTest.class);

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
		suite.addTestSuite(DatafileTest.class);
		suite.addTestSuite(BasicCommandTest.class);
		suite.addTestSuite(CommandsVsUnitsOfWorkTest.class);
		suite.addTestSuite(PostTransactionCommandTest.class);
		suite.addTestSuite(ComparatorsDetailedTest.class);
		suite.addTestSuite(ComparatorsInTreeSetTest.class);
		suite.addTestSuite(EntityCommandTest.class);
		suite.addTestSuite(IdentifierCommandsTest.class);
		suite.addTestSuite(AustralianPayrollTaxCalculatorTest.class);

		// client
		suite.addTestSuite(Db4oActivationTest.class);

		return suite;
	}
}