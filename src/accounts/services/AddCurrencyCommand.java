/*
 * AddCurrencyCommand.java
 * 
 * See LICENCE file for usage and redistribution terms
 * Copyright (c) 2005 Operational Dynamics
 */
package accounts.services;

import java.util.Set;

import accounts.client.ObjectiveAccounts;
import accounts.domain.Books;
import accounts.domain.Currency;
import accounts.persistence.UnitOfWork;

/**
 * Add a Currency object to this set of books
 * 
 * @author Andrew Cowie
 */
public class AddCurrencyCommand extends Command
{
	private Currency	currency	= null;

	/**
	 * Create a new AddCurrencyCommand, specifying:
	 * 
	 * @param cur
	 *            the Currency object to add
	 */
	public AddCurrencyCommand(Currency cur) {
		if (cur == null) {
			throw new IllegalArgumentException("null Currency object passed");
		}
		currency = cur;
	}

	protected void action(UnitOfWork uow) {
		/*
		 * Add currency to Books's currency list
		 */
		Books root = ObjectiveAccounts.store.getBooks();
		Set currencies = root.getCurrencySet();
		currencies.add(currency);

		/*
		 * Store the new account itself, and update the collection which
		 * contains it.
		 */
		uow.registerDirty(currency);
		uow.registerDirty(currencies);
	}

	public void reverse(UnitOfWork uow) {
		throw new UnsupportedOperationException();
	}

	public String getClassString() {
		return "Add Currency";
	}
}
