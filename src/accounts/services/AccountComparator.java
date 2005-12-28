/*
 * AccountComparator.java
 * 
 * See LICENCE file for usage and redistribution terms
 * Copyright (c) 2005 Operational Dynamics
 */
package accounts.services;

import generic.util.DebugException;

import java.util.Comparator;
import java.util.TreeMap;

import accounts.domain.Account;
import accounts.domain.AssetAccount;
import accounts.domain.BankAccount;
import accounts.domain.CashAccount;
import accounts.domain.CurrencyGainLossAccount;
import accounts.domain.DepreciatingAssetAccount;
import accounts.domain.EquityAccount;
import accounts.domain.ExpenseAccount;
import accounts.domain.GenericExpenseAccount;
import accounts.domain.LiabilityAccount;
import accounts.domain.LoanPayableAccount;
import accounts.domain.OwnersEquityAccount;
import accounts.domain.ProfessionalRevenueAccount;
import accounts.domain.RevenueAccount;
import accounts.domain.TaxPayableAccount;

/**
 * Sort Accounts to match our version of accounting order. Accounts, as
 * expressed in this program, have an ordering which is somewhat contrived;
 * since we deprecate acccount codes we use this Comparator to say how accounts
 * should be listed.
 */
/*
 * Move to Account? No, because it references so many deep classes.
 */
public class AccountComparator implements Comparator
{
	private final static TreeMap	lookupTable;

	static {
		final Class[] ordering = {
			AssetAccount.class,
			BankAccount.class,
			CashAccount.class,
			DepreciatingAssetAccount.class,
			LiabilityAccount.class,
			TaxPayableAccount.class,
			LoanPayableAccount.class,
			EquityAccount.class,
			OwnersEquityAccount.class,
			RevenueAccount.class,
			ProfessionalRevenueAccount.class,
			CurrencyGainLossAccount.class,
			ExpenseAccount.class,
			GenericExpenseAccount.class,
		};

		lookupTable = new TreeMap();

		for (int i = 0; i < ordering.length; i++) {
			lookupTable.put(ordering[i].getName(), new Integer(i));
		}
	}

	private final int indexOf(Object obj) {
		final String className = obj.getClass().getName();

		final Integer index = (Integer) lookupTable.get(className);

		if (index == null) {
			throw new DebugException("You are attempting to look up a " + obj.getClass().getName()
					+ " object for which we don't have an entry in " + this.getClass().getName()
					+ "'s Account lookupTable, so add it already!");
		}

		return index.intValue();
	}

	public int compare(Object o1, Object o2) {
		/*
		 * This thing only orders Accounts!
		 */
		if ((!(o1 instanceof Account)) || (!(o2 instanceof Account))) {
			throw new ClassCastException("This comparator only orders Account objects");
		}

		int i1 = indexOf(o1);
		int i2 = indexOf(o2);

		if (i1 < i2) {
			return -1;
		} else if (i1 > i2) {
			return +1;
		} else {
			Account a1 = (Account) o1;
			Account a2 = (Account) o2;
			/*
			 * Same Account subclass, so look into Account's name and order by
			 * that.
			 */
			String title1 = a1.getTitle();
			String title2 = a2.getTitle();

			return title1.compareTo(title2);
		}
	}
}
