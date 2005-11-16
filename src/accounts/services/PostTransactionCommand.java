/*
 * PostTransactionCommand.java
 * 
 * See LICENCE file for usage and redistribution terms
 * Copyright (c) 2005 Operational Dynamics
 */
package accounts.services;

import java.util.Set;

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
		if (trans.getEntries() == null) {
			throw new CommandNotReadyException("Transaction passed has no Entries!");
		}

		if (!trans.isBalanced()) {
			throw new CommandNotReadyException("Transaction not balanced!");
		}

		/*
		 * Persist the Entries of the Transaction, then trans itself.
		 */
		Set entries = trans.getEntries();
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
