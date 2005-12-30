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
 * Accounts.
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
	/**
	 * The date of the Entry. This should be set by (or, at least, will
	 * certainly be replaced by) the Transaction to which it belongs on commit
	 * via a PostTransactionCommand.
	 */
	private Datestamp	date				= null;
	/**
	 * The value of the Entry
	 */
	private Amount		amount				= null;
	/**
	 * The ledger to which this entry [will be] added.
	 */
	private Ledger		parentLedger		= null;
	/**
	 * The Transaction which this entry is a part of.
	 */
	private Transaction	parentTransaction	= null;

	public Entry() {
		/*
		 * default for searches...
		 */
	}

	/**
	 * Construct an Entry specifiying the amount and the account to which it
	 * will be applied. Note that the account will not have it's addEntry()
	 * method called until a PostTransactionCommand is executed for a
	 * transaction holding this Entry object.
	 * 
	 * @param value
	 *            The Amount (be it Debit or Credit) of this Entry.
	 * @param ledger
	 *            The Ledger (which, in turn is of an Account) to which this
	 *            Entry [will be] assinged.
	 */
	public Entry(Amount value, Ledger ledger) {
		this.amount = value;
		this.parentLedger = ledger;
		// this.date = new Datestamp();
		// date.setAsToday();

	}

	/*
	 * Getters and Setters --------------------------------
	 */

	/**
	 * Get the date of the entry (transaction).
	 */
	public Datestamp getDate() {
		return date;
	}

	/**
	 * Set the datestamp of the entry (transaction).
	 */
	public void setDate(Datestamp date) {
		this.date = date;

	}

	/**
	 * Get the amount (value) this entry describes.
	 */
	public Amount getAmount() {
		return amount;
	}

	public void setAmount(Amount value) {
		this.amount = value;
	}

	public Ledger getParentLedger() {
		return parentLedger;
	}

	public void setParentLedger(Ledger parent) {
		this.parentLedger = parent;
	}

	public Transaction getParentTransaction() {
		return parentTransaction;
	}

	public void setParentTransaction(Transaction parent) {
		parentTransaction = parent;
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
		buf.append(amount.getValue());
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
	 * showTransaction parameter.
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
		String idText = null;
		String descText = null;
		String dateText = null;
		// final int MAXDATELEN = 10;
		final int MAXIDLEN = 8;
		final int MAXDESCLEN = 30;
		final int MAXAMOUNTLEN = 15;

		if (showTransaction) {
			idText = parentTransaction.getIdentifier();
			descText = parentTransaction.getDescription();
		} else {
			idText = parentLedger.getParentAccount().getCode();
			descText = parentLedger.getParentAccount().getTitle() + " - " + parentLedger.getName();
		}

		if (idText == null) {
			idText = "";
		}
		if (descText == null) {
			descText = "<null>";
		}
		if (date == null) {
			dateText = "No date  ";
		} else {
			dateText = date.toString();
		}

		out.print(dateText);
		out.print(" ");
		out.print(pad(idText, MAXIDLEN, true));
		out.print(" ");
		out.print(pad(descText, MAXDESCLEN, false));
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