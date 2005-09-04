/*
 * Books.java
 * 
 * See LICENCE file for usage and redistribution terms
 * Copyright (c) 2005 Operational Dynamics
 */
package accounts.domain;

import java.io.PrintWriter;
import java.util.Iterator;
import java.util.Set;

/**
 * Root object of a set of accounts.
 * <P>
 * There are certain known factors about a set of books ("Currency Gain/Loss"
 * for instance), and so rather than generically floundering, we have direct
 * references to them here.
 * 
 * Someday, subclassing this will allow RawBooks and CookedBooks! (credit to pd
 * for that one).
 * 
 * @author Andrew Cowie
 * @author Paul Drain
 */
public class Books
{
	/*
	 * Instance variables ---------------------------------
	 */

	/**
	 * The date which these books were openned, used for bounds checking and
	 * date acceleration. Should be used as the date of the Openning Balance
	 * transaction? TODO
	 */
	protected Datestamp			_dateOfInception	= null;

	/**
	 * The entire Set of Account objects. At Books level, Accounts are all
	 * treated in common, be they Assets, Liabilities, etc. Utility methods
	 * (TODO which may move) are provided to pull accounts from those individual
	 * subclasses.
	 */
	protected Set				_accounts			= null;

	protected Set				_workers			= null;

	protected Set				_currencies			= null;

	/*
	 * Convenience references -----------------------------
	 */

	protected RevenueAccount	_gainLoss			= null;
	protected EquityAccount		_retainedEarnings	= null;

	/**
	 * The home currency that the set of books is denomenated in.
	 */
	protected Currency			_homeCurrency = null;

	/**
	 * @return Returns the entire Accounts Set.
	 */
	public Set getAccountsSet() {
		return _accounts;
	}

	public void setAccountsSet(Set accounts) {
		if (accounts == null) {
			throw new IllegalArgumentException("Can't make a null Set the accounts held by this Books object!");
		}
		_accounts = accounts;
	}

	/**
	 * Get the currency gain/loss Account.
	 */
	public RevenueAccount getGainLossAccount() {
		return _gainLoss;
	}

	/**
	 * Set the Account that will accumulate currency gains and currency losses.
	 * We have arbitrarily (but somewhat in line with common practice) chosen to
	 * represent this as a revenue account, ie, currency gain is possitive,
	 * currency loss is negative.
	 */
	public void setGainLossAccount(RevenueAccount gainLoss) {
		this._gainLoss = gainLoss;
	}

	/**
	 * Get the retained earnings Account.
	 */
	public EquityAccount getRetainedEarningsAccount() {
		return _retainedEarnings;
	}

	/**
	 * Set the Account that will be used to hold the retained earnings of the
	 * business.
	 */
	public void setRetainedEarningsAccount(EquityAccount retainedEarnings) {
		this._retainedEarnings = retainedEarnings;
	}

	/**
	 * Get the Set of Workers (Employees and Subcontractors) associated with the
	 * business represetned by these Books.
	 */
	public Set getWorkers() {
		return _workers;
	}

	/**
	 * Set the Workers (Set of Employees and Subcontractors) associated with the
	 * business represented by these Books.
	 */
	public void setWorkers(Set workers) {
		this._workers = workers;
	}

	/**
	 * Get the Set of Currency objects, which was populated by
	 * {@link AddCurrencyCommand}s
	 */
	public Set getCurrencySet() {
		return _currencies;
	}

	/**
	 * Used to initialize the Set which holds Currency Objects for this set of
	 * books.
	 * 
	 * @see InitBooksCommand
	 */
	public void setCurrencySet(Set currencies) {
		_currencies = currencies;
	}

	/*
	 * Output ---------------------------------------------
	 */

	/**
	 * Top level call to send the tree this set of books represnts to the given
	 * Writer (ie, in text form to console).
	 */
	public void toOutput(PrintWriter out) {
		out.println("Root:");

		Iterator iter = _accounts.iterator();
		while (iter.hasNext()) {
			Account acct = (Account) iter.next();

			acct.toOutput(out);
		}
	}

	public Currency getHomeCurrency() {
		return _homeCurrency;
	}

	/**
	 * 
	 * 
	 * @param home
	 */
	public void setHomeCurrency(Currency home) {
		if (home == null) {
			throw new IllegalArgumentException();
		}
		if (_homeCurrency != null) {
			throw new UnsupportedOperationException("Can't change the home currency once its set!");
		}
		_homeCurrency = home;
	}

}

// /**
// * @return Returns the liabilities.
// */
// public Set getAssetAccounts() {
// LinkedHashSet assets = new LinkedHashSet();
//		
// Iterator iter = _accounts.iterator();
// while (iter.hasNext()) {
// Account a = (Account) iter.next();
// if (a instanceof AssetAccount) {
// assets.add(a);
// }
// }
// return assets;
// }
//
// /**
// * @return Returns the liabilities.
// */
// public Set getLiabilityAccounts() {
// LinkedHashSet liabilities = new LinkedHashSet();
//		
// Iterator iter = _accounts.iterator();
// while (iter.hasNext()) {
// Account a = (Account) iter.next();
// if (a instanceof LiabilityAccount) {
// liabilities.add(a);
// }
// }
// return liabilities;
// }
//
// /**
// * @return Returns a Set of the EquityAccounts.
// */
// public Set getEquityAccounts() {
// LinkedHashSet equities = new LinkedHashSet();
//		
// Iterator iter = _accounts.iterator();
// while (iter.hasNext()) {
// Account a = (Account) iter.next();
// if (a instanceof EquityAccount) {
// equities.add(a);
// }
// }
//
// return equities;
// }
//
// /**
// * @return Returns the revenues.
// */
// public Set getRevenueAccounts() {
// LinkedHashSet revenues = new LinkedHashSet();
//		
// Iterator iter = _accounts.iterator();
// while (iter.hasNext()) {
// Account a = (Account) iter.next();
// if (a instanceof RevenueAccount) {
// revenues.add(a);
// }
// }
//
// return revenues;
// }
//
//
// /**
// * @return Returns the expenses.
// */
// public Set getExpenseAccounts() {
// LinkedHashSet expenses = new LinkedHashSet();
//		
// Iterator iter = _accounts.iterator();
// while (iter.hasNext()) {
// Account a = (Account) iter.next();
// if (a instanceof ExpenseAccount) {
// expenses.add(a);
// }
// }
//
// return expenses;
// }

// /**
// *
// * @return a Set with all the <b>current</b> Accounts presently in the
// * Books.
// */
// public Set getAllAccounts() {
// int total = 0;
//
// total += _assets.size();
// total += _liabilities.size();
// total += _equities.size();
// total += _revenues.size();
// total += _expenses.size();
//
// LinkedHashSet all = new LinkedHashSet(total);
//
// all.addAll(_assets);
// all.addAll(_liabilities);
// all.addAll(_equities);
// all.addAll(_revenues);
// all.addAll(_expenses);
//
// return all;
// }
