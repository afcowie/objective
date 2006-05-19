/*
 * Entry.java
 * 
 * See LICENCE file for usage and redistribution terms
 * Copyright (c) 2005-2006 Operational Dynamics
 */
package accounts.domain;

import generic.domain.DomainObject;
import generic.domain.Normal;
import generic.util.DebugException;

/**
 * An entry in an account. These are formed in balanced (total Debits = total
 * Credits) transactions. Entries form a bridge between Transactions and
 * Accounts.
 * <P>
 * Note that an Entry's datestamp is shared with a transaction, so adding an
 * entry to a transaction will cause it to adopt that Transaction's Datestamp
 * object if one is set.
 * 
 * @author Andrew Cowie
 */
public class Entry extends DomainObject implements Normal
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
	 * Construct an Entry specifiying the amount and the Ledger to which it will
	 * be applied. Note that the Ledger will not have it's addEntry() method
	 * called until a PostTransactionCommand is executed for a transaction
	 * holding this Entry object.
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
		StringBuffer buf = new StringBuffer();
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
}