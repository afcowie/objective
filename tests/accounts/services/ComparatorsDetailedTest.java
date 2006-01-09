/*
 * ComparatorTest.java
 * 
 * See LICENCE file for usage and redistribution terms
 * Copyright (c) 2006 Operational Dynamics
 */
package accounts.services;

import accounts.domain.Account;
import accounts.domain.AccountsPayable;
import accounts.domain.CashAccount;
import accounts.domain.CreditPositiveAccount;
import accounts.domain.DebitPositiveAccount;
import junit.framework.TestCase;

/**
 * TestCase file for tests to make sure the Comparators are working. Bad form,
 * as these weren't written before or during the various Comparators, but gives
 * us somewhere to put tests that check specific exceptional cases.
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
		 * a1 should be AFTER a2, but if the titles only are compared lexically,
		 * then this will fail.
		 */
		assertTrue(cmp.compare(a1, a2) > 0);

		a1 = new CreditPositiveAccount();
		a2 = new DebitPositiveAccount();

		assertTrue(cmp.compare(a1, a2) > 0);

		a1 = new AccountsPayable("Trade Creditors");
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
		 * Do it both ways; whatever hashCode returned, it should be consistent
		 * ordering.
		 */
		assertFalse(a1.hashCode() == a2.hashCode());

		assertTrue(cmp.compare(a1, a2) != 0);
	}

}
