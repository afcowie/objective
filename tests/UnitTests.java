/*
 * ObjectiveAccounts, accounting for small professional services firms.
 *
 * Copyright © 2005-2011 Operational Dynamics Consulting, Pty Ltd
 *
 * The code in this file, and the program it is a part of, is made available
 * to you by its authors as open source software: you can redistribute it
 * and/or modify it under the terms of the GNU General Public License version
 * 2 ("GPL") as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GPL for more details.
 *
 * You should have received a copy of the GPL along with this program. If not,
 * see http://www.gnu.org/licenses/. The authors of this program may be
 * contacted via http://research.operationaldynamics.com/projects/objective/.
 */

import generic.junit.VerboseTestRunner;
import junit.framework.Test;
import junit.framework.TestSuite;
import objective.domain.ValidateAccountBasics;
import objective.domain.ValidateAmount;
import objective.domain.ValidateDatestamp;
import objective.domain.ValidateForeignCurrency;

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

        // domain
        suite.addTestSuite(ValidateAmount.class);
        suite.addTestSuite(ValidateForeignCurrency.class);

        suite.addTestSuite(ValidateAccountBasics.class);
        suite.addTestSuite(ValidateDatestamp.class);

        // services

        // client

        return suite;
    }
}
