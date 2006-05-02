/*
 * TransactionCommand.java
 * 
 * See LICENCE file for usage and redistribution terms
 * Copyright (c) 2006 Operational Dynamics
 * 
 * Code extracted from accounts.services.PostTransactionCommand.java
 * Copyright (c) 2005-2006 Operational Dynamics
 */
package accounts.services;

import generic.util.DebugException;

import java.util.Iterator;
import java.util.Set;

import accounts.domain.Entry;
import accounts.domain.Transaction;

/**
 * Validate a Transaction for persistence to the database. This abstract class
 * carries out state validation and guard checks to get this common code out of
 * the Post, Update, Delete subclasses.
 * 
 * @author Andrew Cowie
 */
public abstract class TransactionCommand extends Command
{
	protected Transaction	transaction	= null;

	/**
	 * Create a new PostTransactionCommand, specifying:
	 * 
	 * @param t
	 *            the Transaction to be persisted.
	 */
	public TransactionCommand(Transaction t) {
		if (t == null) {
			throw new IllegalArgumentException("Null Transaction passed!");
		}
		this.transaction = t;
	}

	/**
	 * Carry out validation. To be called by subclass's action() methods. Don't
	 * validate parent Ledgers containing Entries - addEntry() is to be called
	 * by the implementing subclass of TransactionCommand after the call to this
	 * validate() method.
	 */
	protected void validate() throws CommandNotReadyException {
		Set entries = transaction.getEntries();
		if (entries == null) {
			throw new CommandNotReadyException("Transaction passed has no Entries!");
		}

		Iterator iter = entries.iterator();
		while (iter.hasNext()) {
			Entry e = (Entry) iter.next();
			if (e == null) {
				throw new DebugException("How the heck did a null Entry end up in the Transaction?");
			}
			if (e.getParentLedger() == null) {
				throw new CommandNotReadyException("Entry " + e.toString() + " parent Ledger not set!");
			}
			if (e.getParentTransaction() == null) {
				throw new CommandNotReadyException("Entry " + e.toString() + " parent Transaction not set!");
			}
		}

		if (!transaction.isBalanced()) {
			throw new CommandNotReadyException("Transaction not balanced!");
		}

		if (transaction.getDate() == null) {
			throw new CommandNotReadyException(
				"Transaction doesn't have a date set, which means that the Entries may well have differing dates");
		}
	}

	public String getClassString() {
		return "Transaction";
	}
}
