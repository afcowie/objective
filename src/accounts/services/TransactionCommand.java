/*
 * TransactionCommand.java
 * 
 * See LICENCE file for usage and redistribution terms
 * Copyright (c) 2005 Operational Dynamics
 */
package accounts.services;

import accounts.domain.Transaction;

public class TransactionCommand extends Command
{
	private transient Transaction	_trans	= null;

	public TransactionCommand() {
		super("Transaction");
	}

	public void setTransaction(Transaction t) {
		_trans = t;
	}

	public boolean isComplete() {
		if (_trans == null) {
			return false;
		}

		if (_trans.getEntries() == null) {
			return false;
		}

		return _trans.isBalanced();
	}

	protected void persist() throws CommandNotReadyException {
		// Set entries = _trans.getEntries();
		// FIXME also the subordinate elements?
		_store.save(_trans);
	}

	public void undo() {
		throw new UnsupportedOperationException();
	}

}
