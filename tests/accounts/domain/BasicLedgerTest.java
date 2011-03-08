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

import java.util.Set;

import objective.domain.Credit;
import objective.domain.CreditPositiveLedger;
import objective.domain.Debit;
import objective.domain.DebitPositiveLedger;
import objective.domain.Ledger;

import junit.framework.TestCase;

/**
 * @author Andrew Cowie
 */
public class BasicLedgerTest extends TestCase
{
    public final void testNoArgConstructor() {
        Ledger ledger = new Ledger();
        /*
         * Here just to warn of a behaviour change...
         */
        assertNull("You changed the default null behaviour of Ledger, didn't you?", ledger.getEntries());

        /*
         * Calls calculateBalance(), so after this, the Ledger's internal
         * Amount should be not null, though the Entry Set should be.
         */
        assertNotNull(ledger.getBalance());
        assertEquals("0.00", ledger.getBalance().getValue());
        assertNull("You changed the default null behaviour of Ledger, didn't you?", ledger.getEntries());

    }

    public final void testAddEntry() {
        Ledger ledger = new Ledger();

        /*
         * Safety check.
         */
        try {
            ledger.addEntry(null);
            fail("Should have thrown IllegalArgumentException");
        } catch (IllegalArgumentException iae) {
            // good
        }

        /*
         * Recall real Entries are either Debit or Credit, not both or
         * neither. So arbitrarily pick one.
         */
        Debit first = new Debit(new Amount("49.95"), null);

        /*
         * Another safety check - no operations on raw ledgers!
         */
        try {
            ledger.addEntry(first);
            fail("Should have thrown an UnsupportedOperationException");
        } catch (UnsupportedOperationException uoe) {
        }

        /*
         * Ok, now replace it with a real ledger.
         */
        ledger = new DebitPositiveLedger();

        /*
         * In this case, addEntry() has been not been called before, so
         * neither has calculateBalance. THIS should instantiate the internal
         * Set AND call calculateBalance()
         */
        ledger.addEntry(first);
        assertNotNull(ledger.getEntries());
        assertEquals("49.95", ledger.getBalance().getValue());

        Debit second = new Debit(new Amount("20.00"), null);
        ledger.addEntry(second);
        assertEquals("69.95", ledger.getBalance().getValue());

        Set entries = ledger.getEntries();

        assertEquals(2, entries.size());
    }

    public final void testDebitsAndCreditEntryRecognition() {
        Ledger ledger = new CreditPositiveLedger();
        Debit oneSide = new Debit(new Amount("15.00"), null);
        Credit otherSide = new Credit(new Amount("15.00"), null);

        ledger.addEntry(oneSide);
        assertEquals("-15.00", ledger.getBalance().getValue());

        ledger.addEntry(otherSide);
        assertEquals("0.00", ledger.getBalance().getValue());
    }

    public final void testRemoveEntry() {
        Ledger ledger = new DebitPositiveLedger();

        Debit oneSideA = new Debit(new Amount("19.50"), null);
        Debit oneSideB = new Debit(new Amount("30.45"), null);
        Credit otherSideA = new Credit(new Amount("40.00"), null);
        Credit otherSideB = new Credit(new Amount("9.95"), null);

        ledger.addEntry(oneSideA);
        ledger.addEntry(oneSideB);
        ledger.addEntry(otherSideA);
        ledger.addEntry(otherSideB);

        Set entries = ledger.getEntries();

        assertEquals("0.00", ledger.getBalance().getValue());
        assertEquals(4, entries.size());

        try {
            ledger.removeEntry(null);
            fail("Removing null should have thrown IllegalArgumentException");
        } catch (IllegalArgumentException iae) {
            // good
        }

        ledger.removeEntry(otherSideB);

        // 0 - -995 = 995
        assertEquals(995, ledger.getBalance().getNumber());
        assertEquals(3, entries.size());

        try {
            ledger.removeEntry(otherSideB);
            fail("removing an Entry that isn't in the Ledger should throw IllegalStateException");
        } catch (IllegalStateException ise) {
            // good
        }

        ledger.removeEntry(oneSideB);

        // 995 - 3045 = -2050
        assertEquals(-2050, ledger.getBalance().getNumber());
        assertEquals(2, entries.size());

        ledger.removeEntry(oneSideA);

        // -2050 - 1950 = -4000
        assertEquals(-4000, ledger.getBalance().getNumber());
        assertEquals(1, entries.size());

        ledger.removeEntry(otherSideA);

        // -4000 - -4000 = 0
        assertEquals(0, ledger.getBalance().getNumber());
        assertEquals(0, entries.size());
    }

    public final void testUpdateEntry() {
        Debit one = new Debit(new Amount("50"), null);
        DebitPositiveLedger pos = new DebitPositiveLedger();

        pos.addEntry(one);
        assertEquals(5000, pos.getBalance().getNumber());

        Amount a = one.getAmount();
        a.setValue("42");

        /*
         * ie, not 42, because we haven't told the Ledger to reset itself
         */
        assertEquals(5000, pos.getBalance().getNumber());

        pos.updateEntry(one);
        assertEquals(4200, pos.getBalance().getNumber());
    }
}
