/*
 * BasicAccountTest.java
 * 
 * See LICENCE file for usage and redistribution terms
 * Copyright (c) 2005 Operational Dynamics
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
