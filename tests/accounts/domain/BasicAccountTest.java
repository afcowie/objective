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

/**
 * Basic tests of Account class
 * 
 * @author Andrew Cowie
 */
public class BasicAccountTest extends TestCase
{
    public final void testIsDebitPositive() {
        Account acct = new Account();
        try {
            assertFalse(acct.isDebitPositive());
            fail("Should have thrown UnsupportedOperationException");
        } catch (UnsupportedOperationException uoe) {
        }

        try {
            assertFalse(acct.isCreditPositive());
            fail("Should have thrown UnsupportedOperationException");
        } catch (UnsupportedOperationException uoe) {
        }

        Account drPosAcct = new DebitPositiveAccount();
        assertTrue(drPosAcct.isDebitPositive());
        assertFalse(drPosAcct.isCreditPositive());

    }

    public final void testIsCreditPositive() {
        Account crPosAcct = new CreditPositiveAccount();
        assertTrue(crPosAcct.isCreditPositive());
        assertFalse(crPosAcct.isDebitPositive());
    }

    public final void testSetTitle() {
        Account acct = new Account();

        String TITLE = "Currency Gain/Loss";
        acct.setTitle(TITLE);
        assertEquals(TITLE, acct.getTitle());
    }

    public final void testCodeValidation() {
        Account acct = new Account();
        try {
            acct.setCode("4-9999");
        } catch (IllegalArgumentException iae) {
            fail("It threw an IllegalArgumentException. It shouldn't have!");
        }
        assertEquals("4-9999", acct.getCode());

        /*
         * right length, but missing '-'
         */
        try {
            acct.setCode("499999");
            fail("It should have thrown a IllegalArgumentException.");
        } catch (IllegalArgumentException iae) {
            //
        } catch (Exception e) {
            fail("It threw something, but not what was expected: " + e);
        }

        try {
            /*
             * wrong length, but '-' in right place.
             */
            acct.setCode("4-999");
            fail("It should have thrown a IllegalArgumentException.");
        } catch (IllegalArgumentException iae) {
            //
        } catch (Exception e) {
            fail("It threw something, but not what was expected: " + e);
        }

    }

    // public final void
}
