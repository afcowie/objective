/*
 * Entry.java
 * 
 * See LICENCE file for usage and redistribution terms
 * Copyright (c) 2005 Operational Dynamics
 */
package accounts.domain;

import generic.util.DebugException;

import java.io.PrintWriter;

/**
 * An entry in an account. These are formed in balanced (total Debits = total
 * Credits) transactions. Entries form a bridge between Transactions and
 * accounts.
 * <P>
 * Note that an Entry's datestamp is shared with a transaction, so adding an
 * entry to a transaction will cause it to adopt that transactions Datestamp
 * object if one is set.
 * 
 * @author Andrew Cowie
 */
public class Entry
{
	/*
	 * Instance variables ---------------------------------
	 */
	private Datestamp	_date				= null;
	private Amount		_amount				= null;

	/*
	 * Navigation references ------------------------------
	 */
	private Account		_parentAccount		= null;
	// TODO what about Ledger?
	private Transaction	_parentTransaction	= null;

	public Entry() {
		/*
		 * default for searches...
		 */
	}

	/**
	 * Quickly construct a simple entry. The entry's date will be set to today.
	 * 
	 * @param amount
	 */
	public Entry(Amount amount) {
		_amount = amount;
		_date = new Datestamp();
		_date.setAsToday();

	}

	/**
	 * Full constructor; correctly specifying all the necessary attributes will
	 * give you a complete Entry.
	 * 
	 * @param amount
	 * @param date
	 * @param transaction
	 * @param account
	 */
	// public Entry(Amount amount, Datestamp date, Transaction transaction,
	// Account account) {
	// _amount = amount;
	// _date = date;
	// _parentTransaction = transaction;
	// _parentAccount = account;
	// }
	
	/*
	 * Getters and Setters --------------------------------
	 */

	/**
	 * Get the date of the entry (transaction).
	 */
	public Datestamp getDate() {
		return _date;
	}

	/**
	 * Set the datestamp of the entry (transaction).
	 */
	public void setDate(Datestamp date) {
		this._date = date;
		
	}

	/**
	 * Get the amount (value) this entry describes.
	 */
	public Amount getAmount() {
		return _amount;
	}

	public void setAmount(Amount value) {
		_amount = value;
	}

	public Account getParentAccount() {
		return _parentAccount;
	}

	public void setParentAccount(Account account) {
		_parentAccount = account;
	}

	public Transaction getParentTransaction() {
		return _parentTransaction;
	}

	public void setParentTransaction(Transaction parent) {
		_parentTransaction = parent;
	}

	/*
	 * Output ---------------------------------------------
	 */

	/**
	 * Prints the value of this entry, with an indication of whether it is a
	 * Debit or Credit value.
	 */
	public String toString() {
		// TODO Are Entries only in native currency?
		StringBuffer buf = new StringBuffer('$');
		buf.append(_amount.getValue());
		buf.append(' ');

		if (this instanceof Debit) {
			buf.append("DR");
		} else if (this instanceof Credit) {
			buf.append("CR");
		} else {
			throw new DebugException("huh?");
		}
		return buf.toString();
	}

	/**
	 * Print a formatted text version of this Entry on a single line. As entries
	 * have a two-way relation between Ledgers and Transactions, it either
	 * displays the parent transaction, or the parent account, depending on the
	 * FIXME parameter.
	 * 
	 * @param out
	 *            OutputWriter you want to print to (presumably passed in a
	 *            cascade)
	 * @param showTransaction
	 *            Do you want to pull the parent Transaction's description into
	 *            the output? If not, it will use the parent Account's title.
	 * 
	 */
	public void toOutput(PrintWriter out, boolean showTransaction) {
		String id = null;
		String desc = null;
		// final int MAXDATELEN = 10;
		final int MAXIDLEN = 10;
		final int MAXDESCLEN = 30;
		final int MAXAMOUNTLEN = 15;

		if (showTransaction) {
			id = _parentTransaction.getIdentifier();
			desc = _parentTransaction.getDescription();
		} else {
			id = _parentAccount.getCode();
			desc = _parentAccount.getTitle();
		}

		if (id == null) {
			id = "";
		}
		if (desc == null) {
			desc = "";
		}

		out.print(_date.toString());

		out.print(pad(id, MAXIDLEN, true));
		out.print(pad(desc, MAXDESCLEN, false));
		/*
		 * Debit
		 */
		if (this instanceof Debit) {
			out.print(pad(toString(), MAXAMOUNTLEN, true));
		} else {
			out.print(pad("", MAXAMOUNTLEN, true));
		}

		if (this instanceof Credit) {
			out.print(pad(toString(), MAXAMOUNTLEN, true));
		} else {
			out.print(pad("", MAXAMOUNTLEN, true));
		}
		out.println();
	}

	/**
	 * @param str
	 *            the String to pad.
	 * @param width
	 *            maximum length of the padded result.
	 * @param right
	 *            if true, right justify. Otherwise, normal left justification.
	 * @return the padded String.
	 */
	private String pad(String str, int width, boolean right) {
		String trimmed = null;
		/*
		 * crop
		 */
		int len = str.length();
		if (len > width) {
			trimmed = str.substring(0, width);
			len = width;
		} else {
			trimmed = str;
		}
		int spaces = width - len;

		/*
		 * pad
		 */
		StringBuffer buf = new StringBuffer("");
		if (!right) {
			buf.append(trimmed);
		}
		for (int i = 0; i < spaces; i++) {
			buf.append(" ");
		}
		if (right) {
			buf.append(trimmed);
		}
		return buf.toString();
	}
}