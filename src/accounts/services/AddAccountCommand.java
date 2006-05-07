/*
 * AddAccountCommand.java
 * 
 * See LICENCE file for usage and redistribution terms
 * Copyright (c) 2005 Operational Dynamics
 */
package accounts.services;

import generic.persistence.DataClient;

import java.util.Set;

import accounts.domain.Account;
import accounts.domain.Books;

/**
 * Add an Account to the system.
 * 
 * @author Andrew Cowie
 */
public class AddAccountCommand extends Command
{
	private Account	account	= null;

	/**
	 * Create a new AddAccountCommand, specifying:
	 * 
	 * @param account
	 *            The Account to add. Must not already be persisted in the
	 *            DataClient.
	 */
	public AddAccountCommand(Account account) {
		if (account == null) {
			throw new IllegalArgumentException("Can't add a null account");
		}
		this.account = account;
	}

	protected void action(DataClient store) {
		Books root = (Books) store.getRoot();
		Set accounts = root.getAccountsSet();
		if (accounts.add(account) == false) { // dup!?!
			throw new IllegalStateException("How did you add an account that's already in the system?");
		}

		/*
		 * Store the collection. Rely on cascading update depth to add the new
		 * account object along the way.
		 */

		store.save(accounts);
	}

	protected void reverse(DataClient store) {
		throw new UnsupportedOperationException();
	}

	public String getClassString() {
		return "Add Account";
	}
}
