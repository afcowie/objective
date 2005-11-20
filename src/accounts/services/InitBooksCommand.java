/*
 * InitBooksCommand.java
 * 
 * See LICENCE file for usage and redistribution terms
 * Copyright (c) 2005 Operational Dynamics
 */
package accounts.services;

import java.util.Set;

import accounts.client.ObjectiveAccounts;
import accounts.domain.Books;
import accounts.domain.Currency;
import accounts.persistence.DataStore;
import accounts.persistence.UnitOfWork;

/**
 * Create a new set of company books. Obvisouly, this is the first command, and
 * likewise, really, only ever run once!
 * 
 * @author Andrew Cowie
 */
public class InitBooksCommand extends Command
{
	private Currency	home;

	/**
	 * Create a new InitBooksCommand, specifying:
	 * 
	 * @param home
	 *            The Currency to be set as the "home", or underlying natural
	 *            currency which the books are kept in.
	 */
	public InitBooksCommand(Currency home) {
		if (home == null) {
			throw new IllegalArgumentException("Home Currency object must be non-null and initialized");
		}
		this.home = home;
	}

	protected void action(UnitOfWork uow) {
		Books root = new Books();
		/*
		 * We use the built in sets of db4o for greater efficiency and for their
		 * autoactivation and update features.
		 */
		DataStore store = ObjectiveAccounts.store;
		store.setBooks(root);

		Set accounts = store.newSet();
		root.setAccountsSet(accounts);

		/*
		 * Establish the Collection for Currency objects, set the home currency
		 * for this Books, and add it as the first currency in the Books's
		 * Currencies Set
		 */
		Set currencies = store.newSet();
		root.setCurrencySet(currencies);

		root.setHomeCurrency(home);

		currencies.add(home);

		/*
		 * Persist.
		 */
		uow.registerDirty(accounts);
		uow.registerDirty(currencies);
		uow.registerDirty(home);
		uow.registerDirty(root);
	}

	public void reverse(UnitOfWork uow) {
		throw new UnsupportedOperationException();
	}

	public String getClassString() {
		return "Initialize Books";
	}
}
