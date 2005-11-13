/*
 * AddCurrencyCommand.java
 * 
 * See LICENCE file for usage and redistribution terms
 * Copyright (c) 2005 Operational Dynamics
 */
package accounts.services;

import java.util.Set;

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
	private transient Currency currency = null;
	private transient Set currencies = null;
	
	public AddCurrencyCommand() {
		super("AddCurrency");		
	}
	
	/**
	 * 
	 * @param cur the Currency object to add
	 */
	public void setCurrency(Currency cur) {
		if (cur == null) {
			throw new IllegalArgumentException("null Currency object passed");
		}
	
		/*
		 * Add currency to Books's currency list
		 */
		Books root = _store.getBooks();
		currencies = root.getCurrencySet();
		currencies.add(cur);
		
		/*
		 * Mark as ready
		 */
		currency = cur;
	}

	public boolean isComplete() {
		if (currency == null) {
			return false;	
		} else {
			return true;
		}
	}

	protected void persist(UnitOfWork uow) throws CommandNotReadyException {
		/*
		 * Store the new account itself.
		 */
		uow.registerDirty(currency);
		/*
		 * And update the collection which contains it.
		 */
		uow.registerDirty(currencies);
	}

	public void undo() {
		throw new UnsupportedOperationException();
	}

}
