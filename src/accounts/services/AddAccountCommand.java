/*
 * AddAccountCommand.java
 * 
 * See LICENCE file for usage and redistribution terms
 * Copyright (c) 2005 Operational Dynamics
 */
package accounts.services;

import java.util.Set;

import accounts.client.ObjectiveAccounts;
import accounts.domain.Account;
import accounts.domain.Books;
import accounts.persistence.UnitOfWork;

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
	 *            DataStore.
	 */
	public AddAccountCommand(Account account) {
		if (account == null) {
			throw new IllegalArgumentException("Can't add a null account");
		}
		this.account = account;
	}

	protected void action(UnitOfWork uow) {
		Books root = ObjectiveAccounts.store.getBooks();
		Set accounts = root.getAccountsSet();
		if (accounts.add(account) == false) { // dup!?!
			throw new IllegalStateException("How did you add an account that's already in the system?");
		}

		Set r = account.getLedgers();
		if (r != null) {
			uow.registerDirty(r);
		}
		/*
		 * Store the new account itself, and update the collection
		 */
		uow.registerDirty(account);
		uow.registerDirty(accounts);
	}

	protected void reverse(UnitOfWork uow) {
		throw new UnsupportedOperationException();
	}

	public String getClassString() {
		return "Add Account";
	}
}
