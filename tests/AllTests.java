

import accounts.domain.AllDomainTests;
import accounts.persistence.AllPersistenceTests;
import accounts.services.AllServicesTests;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * @author Andrew Cowie
 */
public class AllTests
{

	public static void main(String[] args) {
		junit.textui.TestRunner.run(suite());
	}

	public static Test suite() {
		TestSuite suite = new TestSuite("All Unit Tests for ObjectiveAccounts");

		suite.addTest(AllDomainTests.suite());
		suite.addTest(AllPersistenceTests.suite());
		suite.addTest(AllServicesTests.suite());

		return suite;
	}
}