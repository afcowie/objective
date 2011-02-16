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
package accounts.domain;

import junit.framework.TestCase;

public class BasicLoanTest extends TestCase
{

    /*
     * Test method for 'accounts.domain.LoanLedger.setStartDate(Datestamp)'
     */
    final public void testBasicSetters() {
        Loan loan = new Loan();
        loan.setStartDate(new Datestamp("20 Jul 05"));
        assertEquals("20 Jul 05", loan.getStartDate().toString());

        loan.setPeriodsPerYear(4); // quarterly
        loan.setNumPeriods(12); // 3 years
        assertEquals(4, loan.getPeriodsPerYear());
        assertEquals(12, loan.getNumPeriods());

        loan.calculateDueDate();
        Datestamp due = loan.getDueDate();
        assertNotNull(due);
        assertEquals("19 Jul 08", due.toString()); // TODO VERIFY!!!

    }

    final public void testSetNumYears() {
        Loan loan = new Loan();

        /*
         * Should be unset...
         */
        assertEquals(0, loan.getNumPeriods());

        try {
            loan.setNumYears(3);
            fail("Should have thrown an exception");
        } catch (IllegalStateException ise) {
        }

        loan.setPeriodsPerYear(4);
        loan.setNumYears(3);

        assertEquals(12, loan.getNumPeriods());
    }

    /*
     * Test method for 'accounts.domain.LoanLedger.setInterestRate(String)'
     */
    public void testSetInterestRate() {

    }

    /*
     * Test method for 'accounts.domain.LoanLedger.calculateDueDate()'
     */
    final public void testOddDueDate() {
        Loan loan = new Loan();
        loan.setStartDate(new Datestamp("1 Jan 05"));
        loan.setNumPeriods(2);
        loan.setPeriodsPerYear(4);
        loan.calculateDueDate();
        /*
         * FIXME... is that really 1/2 way? Check a diary.
         */
        assertEquals("02 Jul 05", loan.getDueDate().toString());
    }

    /*
     * All in one calls calculateDueDate().
     */
    final public void testExpressConstructor() {
        Loan loan = new Loan("Test Loan", new Datestamp("1 Jan 01"), 4, 4);
        assertEquals("01 Jan 02", loan.getDueDate().toString());
    }
}
