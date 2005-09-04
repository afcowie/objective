/*
 * TransactionServices.java
 * 
 * See LICENCE file for usage and redistribution terms
 * Copyright (c) 2005 Operational Dynamics
 */
package accounts.services;

import java.util.Set;

import accounts.domain.Transaction;
import accounts.persistence.DataStore;
import accounts.persistence.Db4oSet;

/**
 * Facade to expose service layer for manipulating transactions. This will be
 * static until there's a reason for it not to be.
 * 
 * @author Andrew Cowie
 */
public class TransactionServices
{

	public static void commitTransaction(Transaction transaction) {

		/*
		 * TODO Do something like
		 */
		DataStore store = null; // where does this come from????

		Set originalEntries = transaction.getEntries();

		if (originalEntries instanceof Db4oSet) {
			// nothing
		} else {
			Set newEntries = store.newSet();
			newEntries.addAll(originalEntries); // TODO use boolean of addAll..
			transaction.setEntries(newEntries);
		}

		throw new UnsupportedOperationException();

	}

	/*
	 * TODO IDEA: updateAccount will cause balances to be recaluclated... ...
	 * updates to GUI outside of a single window will be after refresh to
	 * database?... or perhaps account balances will be updated on (and only on)
	 * Transaction commit?
	 */
}