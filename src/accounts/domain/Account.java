/*
 * Account.java
 * 
 * See LICENCE file for usage and redistribution terms
 * Copyright (c) 2005 Operational Dynamics
 */
package accounts.domain;

import java.io.PrintWriter;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Fundamental grouping. An Account consists of metadata for organizing, and one
 * or more actual Ledgers representing actual T accounts.
 * 
 * @author Andrew Cowie
 */
public class Account
{
	/*
	 * Instance variables ---------------------------------
	 */
	protected String			title	= null;
	protected String			code	= null;
	private Set					ledgers	= null;

	/*
	 * Cached values --------------------------------------
	 */
	private transient Amount	balance	= null;

	/*
	 * Constructors ---------------------------------------
	 */

	public Account() {
		// only for creating search prototypes
	}

	public Account(String title) {
		setTitle(title);
	}

	/*
	 * Utility methods ------------------------------------
	 */

	/**
	 * Add a Ledger to the Account
	 */
	public void addLedger(Ledger ledger) {
		/*
		 * validation
		 */
		if (ledger == null) {
			throw new NullPointerException("attempted to add a null Ledger!");
		}

		if (!((ledger instanceof DebitPositiveLedger) || (ledger instanceof CreditPositiveLedger))) {
			throw new IllegalArgumentException("attempted to add a Ledger which is neither Debit nor Credit Postive!");
		}
		/*
		 * setup
		 */
		if (ledgers == null) {
			ledgers = new LinkedHashSet();
		}
		/*
		 * action
		 */
		ledgers.add(ledger);
		ledger.setParentAccount(this);

		if (balance == null) {
			balance = (Amount) ledger.getBalance().clone();
		} else {
			addToBalance(ledger);
		}
	}

	/**
	 * Calculate the balance of the Account, a transient quantity. TODO This may
	 * need to be made public to allow updates.
	 * 
	 */
	protected void calculateBalance() {
		balance = new Amount("0");
		if (ledgers == null) {
			return;
		}

		Iterator iter = ledgers.iterator();
		while (iter.hasNext()) {
			Ledger ledger = (Ledger) iter.next();
			addToBalance(ledger);
		}
	}

	/**
	 * Dig into a Ledger and add it's balance taking into account Debit- or
	 * Credit-ness.
	 */
	private void addToBalance(Ledger ledger) {
		if (ledger == null) {
			throw new IllegalStateException("How did you get a null Ledger into ledgers?");
		}
		if (this instanceof DebitPositiveAccount) {
			if (ledger instanceof DebitPositiveLedger) {
				balance.incrementBy(ledger.getBalance());
			} else {
				balance.decrementBy(ledger.getBalance());
			}
		} else if (this instanceof CreditPositiveAccount) {
			if (ledger instanceof CreditPositiveLedger) {
				balance.incrementBy(ledger.getBalance());
			} else {
				balance.decrementBy(ledger.getBalance());
			}
		} else {
			throw new IllegalStateException(
					"You're working with a raw Account which is neither Debit nor Credit Positive, so you can't get a balance of it!");
		}
	}

	public boolean isDebitPositive() {
		throw new UnsupportedOperationException();
	}

	public boolean isCreditPositive() {
		throw new UnsupportedOperationException();
	}

	/*
	 * Getters and Setters --------------------------------
	 */

	/**
	 * @return the Set of Ledger objects that comprise this Account.
	 */
	public Set getLedgers() {
		return ledgers;
	}

	/**
	 * Replace the Set of Ledger objects attached to this Account. Recalculates
	 * the internal balance. This may be unnecessary as the only real usage of
	 * this is to replace an internal ledgers Set representation with some other
	 * implementation of Set which would be based on the same Ledger objects,
	 * but as ever don't trust the data...
	 * @deprecated
	 */
	public void setLedgers(Set ledgers) {
		this.ledgers = ledgers;
		calculateBalance();
	}

	public Amount getBalance() {
		return balance;
	}

	/**
	 * Get the title (name) of the account.
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * Set the title (name) of the account.
	 */
	public void setTitle(String title) {
		this.title = title;
	}

	/**
	 * Get the account code
	 * 
	 * @return the account code, as a String.
	 */
	public String getCode() {
		return code;
	}

	/**
	 * Set the account's "numeric" code. TODO I've enforecd the basic Australian
	 * account code form here, but this could and should be made switchable or
	 * generic to support others use.
	 * 
	 * @param code
	 *            Is expected to be numeric, but is stored as a String to
	 *            support codes of the form "4-1200"
	 */
	public void setCode(String code) throws IllegalArgumentException {
		if (code.charAt(1) != '-') {
			throw new IllegalArgumentException("second character of the account code needs to be a '-' character");
		}
		if (code.length() != 6) {
			throw new IllegalArgumentException("account code needs to be 6 characters long, in the form 'x-yyyy'");
		}
		this.code = code;
	}

	/*
	 * Output ---------------------------------------------
	 */

	/**
	 * Print a formatted text version of this Account.
	 * 
	 * @param out
	 *            OutputWriter you want to print to.
	 */
	public void toOutput(PrintWriter out) {
		out.println();
		out.print("Account: \"" + title + "\"");
		if (code != null) {
			out.print(" [" + code + "]");
		}
		out.println(" (" + getClassString() + ")");

		if (ledgers == null) {
			out.println("<no ledgers>");
			return;
		}
		Iterator iter = ledgers.iterator();
		while (iter.hasNext()) {
			Ledger ledger = (Ledger) iter.next();

			if ((ledgers.size() == 1) && (ledger.getName() == null)) {
				out.print("Ledger:  (un-named)");
			} else {
				out.print("Ledger:  \"" + ledger.getName() + "\"");
			}
			out.println(" (" + ledger.getClassString() + ")");

			ledger.toOutput(out);
		}
	}

	public String getClassString() {
		return "Account";
	}

	/**
	 * Yes, colour is spelled with a u... but in GTK it's spelled color. Fine.
	 */
	public String getColor() {
		if (this instanceof DebitPositiveAccount) {
			return Debit.COLOR;
		} else if (this instanceof CreditPositiveAccount) {
			return Credit.COLOR;
		} else {
			return "";
		}
	}
}