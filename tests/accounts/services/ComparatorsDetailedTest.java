/*
 * ObjectiveAccounts, accounting for small professional services firms.
 *
 * Copyright © 2006-2011 Operational Dynamics Consulting, Pty Ltd
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
package accounts.services;

import objective.domain.Account;
import objective.domain.AccountsPayableAccount;
import objective.domain.CashAccount;
import objective.domain.CreditPositiveAccount;
import objective.domain.DebitPositiveAccount;
import objective.services.AccountComparator;
import junit.framework.TestCase;

/**
 * TestCase file for tests to make sure the Comparators are working. Bad form,
 * as these weren't written before or during the various Comparators, but
 * gives us somewhere to put tests that check specific exceptional cases.
 * 
 * @author Andrew Cowie
 */
public class ComparatorsDetailedTest extends TestCase
{
    public final void testAccountComparator() {
        Account a1 = new Account("First");
        Account a2 = new Account("Second");

        AccountComparator cmp = new AccountComparator();

        /*
         * lexically, a1 should be before a2
         */
        assertTrue(cmp.compare(a1, a2) < 0);
    }

    public final void testAccountComparatorConcreteness() {
        Account a1 = new CreditPositiveAccount("1ne");
        Account a2 = new DebitPositiveAccount("2wo");

        AccountComparator cmp = new AccountComparator();

        /*
         * a1 should be AFTER a2, but if the titles only are compared
         * lexically, then this will fail.
         */
        assertTrue(cmp.compare(a1, a2) > 0);

        a1 = new CreditPositiveAccount();
        a2 = new DebitPositiveAccount();

        assertTrue(cmp.compare(a1, a2) > 0);

        a1 = new AccountsPayableAccount("Trade Creditors");
        a2 = new CashAccount("Petty Cash", "Beach Kisok");

        /*
         * a1 should be after a2, but this will fail if the current type of a1
         * (as opposed to instanceof) is used.
         */
        assertTrue(cmp.compare(a1, a2) > 0);
    }

    public final void testAccountComparatorOnCode() {
        Account a1 = new Account("Test");
        Account a2 = new Account("Test");

        a1.setCode("1-2000");
        a2.setCode("1-2001");

        AccountComparator cmp = new AccountComparator();

        /*
         * a1 should be BEFORE a2
         */
        assertTrue(cmp.compare(a1, a2) < 0);
    }

    public final void testAccountComparatorOnHash() {
        Account a1 = new Account("Test");
        Account a2 = new Account("Test");

        a1.setCode("1-2000");
        a2.setCode("1-2000");

        AccountComparator cmp = new AccountComparator();

        /*
         * Do it both ways; whatever hashCode returned, it should be
         * consistent ordering.
         */
        assertFalse(a1.hashCode() == a2.hashCode());

        assertTrue(cmp.compare(a1, a2) != 0);
    }

}
