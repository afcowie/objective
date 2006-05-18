/*
 * AllTests.java
 * 
 * See LICENCE file for usage and redistribution terms
 * Copyright (c) 2005-2006 Operational Dynamics
 */

import generic.domain.DomainObjectTest;
import generic.junit.VerboseTestRunner;
import generic.persistence.BasicDb4oTest;
import generic.persistence.EngineTest;
import generic.persistence.ExposeDb4oInterfaceTest;
import generic.persistence.RollbackDb4oTest;
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
import accounts.services.BasicCommandTest;
import accounts.services.BasicFinderTest;
import accounts.services.BooksSetByCommandsTest;
import accounts.services.ComparatorsDetailedTest;
import accounts.services.ComparatorsInTreeSetTest;
import accounts.services.EntityCommandTest;
import accounts.services.IdentifierCommandsTest;
import accounts.services.TransactionCommandsTest;
import accounts.services.WorkerCommandsTest;
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
		suite.addTestSuite(ExposeDb4oInterfaceTest.class);

		// domain
		suite.addTestSuite(DomainObjectTest.class);
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
		suite.addTestSuite(TransactionCommandsTest.class);
		suite.addTestSuite(ComparatorsDetailedTest.class);
		suite.addTestSuite(ComparatorsInTreeSetTest.class);
		suite.addTestSuite(BasicFinderTest.class);
		suite.addTestSuite(EntityCommandTest.class);
		suite.addTestSuite(IdentifierCommandsTest.class);
		suite.addTestSuite(AustralianPayrollTaxTest.class);
		suite.addTestSuite(WorkerCommandsTest.class);

		// client
		suite.addTestSuite(Db4oActivationTest.class);

		return suite;
	}
}