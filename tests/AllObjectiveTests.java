

import accounts.domain.AllObjectiveDomainTests;
import accounts.persistence.AllObjectivePersistenceTests;
import accounts.services.AllObjectiveServicesTests;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * @author Andrew Cowie
 */
public class AllObjectiveTests
{

	public static void main(String[] args) {
		junit.textui.TestRunner.run(suite());
	}

	public static Test suite() {
		TestSuite suite = new TestSuite("All Unit Test for ObjectiveAccounts");

		suite.addTest(AllObjectiveDomainTests.suite());
		suite.addTest(AllObjectivePersistenceTests.suite());
		suite.addTest(AllObjectiveServicesTests.suite());

		return suite;
	}
}