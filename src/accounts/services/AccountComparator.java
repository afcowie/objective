/*
 * AccountComparator.java
 * 
 * See LICENCE file for usage and redistribution terms
 * Copyright (c) 2005-2006 Operational Dynamics
 */
package accounts.services;

import generic.util.DebugException;

import java.util.Comparator;
import java.util.TreeMap;

import accounts.domain.Account;
import accounts.domain.AccountsPayable;
import accounts.domain.AccountsReceivable;
import accounts.domain.AssetAccount;
import accounts.domain.BankAccount;
import accounts.domain.CashAccount;
import accounts.domain.CreditPositiveAccount;
import accounts.domain.CurrencyGainLossAccount;
import accounts.domain.DebitPositiveAccount;
import accounts.domain.DepreciatingAssetAccount;
import accounts.domain.EquityAccount;
import accounts.domain.ExpenseAccount;
import accounts.domain.GenericExpenseAccount;
import accounts.domain.LiabilityAccount;
import accounts.domain.LoanPayableAccount;
import accounts.domain.OwnersEquityAccount;
import accounts.domain.PayrollTaxPayableAccount;
import accounts.domain.ProfessionalRevenueAccount;
import accounts.domain.RevenueAccount;
import accounts.domain.SalesTaxPayableAccount;
import accounts.domain.ReimbursableExpensesPayableAccount;

/**
 * Sort Accounts to match our version of accounting order. Accounts, as
 * expressed in this program, have an ordering which is somewhat contrived;
 * since we deprecate acccount codes we use this Comparator to say how accounts
 * should be listed.
 * <p>
 * Although Account, DebitPositiveAccount and CreditPositiveAccount are included
 * in the ordering table - you really ought only to be comparing the "concrete"
 * subclasses of Account.
 */
/*
 * Move to Account? No, because it references so many deep classes.
 */
public class AccountComparator implements Comparator
{
	private final static TreeMap	lookupTable;

	static {
		final Class[] ordering = {
			Account.class,
			DebitPositiveAccount.class,
			CreditPositiveAccount.class,

			AssetAccount.class,
			BankAccount.class,
			CashAccount.class,
			AccountsReceivable.class,
			DepreciatingAssetAccount.class,

			LiabilityAccount.class,
			AccountsPayable.class,
			SalesTaxPayableAccount.class,
			PayrollTaxPayableAccount.class,
			ReimbursableExpensesPayableAccount.class,
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
			 * Same Account subclass, so look into Account's code and order by
			 * it. Note that we generally aren't using this at the moment, so
			 * you're expected to hit nulls, but this is supported for future
			 * alternate ordering. TODO perhaps this should rise above the
			 * result of the indexOf call?
			 */
			String code1 = a1.getCode();
			String code2 = a2.getCode();

			int codeCmp;
			if ((code1 == null) || (code2 == null)) {
				// force it to ignore this comparison and move to the
				// next
				codeCmp = 0;
			} else {
				codeCmp = code1.compareTo(code2);
			}

			if (codeCmp != 0) {
				return codeCmp;
			} else {
				/*
				 * Same (or as expected and more likely, null) code, so look
				 * into Account's name and order by that.
				 */
				String title1 = a1.getTitle();
				String title2 = a2.getTitle();

				int titleCmp;
				if ((title1 == null) || (title2 == null)) {
					// force it to ignore this comparison and move to the
					// next
					titleCmp = 0;
				} else {
					titleCmp = title1.compareTo(title2);
				}

				if (titleCmp != 0) {
					return titleCmp;
				} else {
					/*
					 * At this point, go to Object's hashCode. Do not expect to
					 * get this deep in normal usage.
					 */
					int hash1 = a1.hashCode();
					int hash2 = a2.hashCode();

					if (hash1 < hash2) {
						return -1;
					} else if (hash1 > hash2) {
						return +1;
					} else {
						return 0;
					}
				}
			}
		}
	}
}
