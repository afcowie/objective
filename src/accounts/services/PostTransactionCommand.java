/*
 * PostTransactionCommand.java
 * 
 * See LICENCE file for usage and redistribution terms
 * Copyright (c) 2005 Operational Dynamics
 */
package accounts.services;

import generic.util.DebugException;

import java.util.Iterator;
import java.util.Set;

import accounts.domain.Entry;
import accounts.domain.Ledger;
import accounts.domain.Transaction;
import accounts.persistence.UnitOfWork;

/**
 * Post a Transaction. TODO: This suffices for Add and Update? What about
 * Delete?
 * 
 * @author Andrew Cowie
 */
public class PostTransactionCommand extends Command
{
	private Transaction	trans	= null;

	/**
	 * Create a new PostTransactionCommand, specifying:
	 * 
	 * @param t
	 *            the Transaction to be persisted.
	 */
	public PostTransactionCommand(Transaction t) {
		if (t == null) {
			throw new IllegalArgumentException("Null Transaction passed!");
		}
		trans = t;
	}

	protected void action(UnitOfWork uow) throws CommandNotReadyException {
		Set entries = trans.getEntries();
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

		if (!trans.isBalanced()) {
			throw new CommandNotReadyException("Transaction not balanced!");
		}

		if (trans.getDate() == null) {
			throw new CommandNotReadyException(
					"Transaction doesn't have a date set, which means that the Entries may well have differing dates");
		}

		/*
		 * At last, carry out the addition of the [values of the] Entries to the
		 * [balances of the] Ledgers they bridge to.
		 */

		iter = entries.iterator();
		while (iter.hasNext()) {
			Entry e = (Entry) iter.next();
			Ledger l = e.getParentLedger();
			l.addEntry(e);
		}

		/*
		 * Persist the Entries of the Transaction, then trans itself.
		 */

		// FIXME also the subordinate elements?
		uow.registerDirty(entries);
		uow.registerDirty(trans);
	}

	public void reverse(UnitOfWork uow) {
		throw new UnsupportedOperationException();
	}

	public String getClassString() {
		return "Post Transaction";
	}
}
