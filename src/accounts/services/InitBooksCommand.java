/*
 * InitBooksCommand.java
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
 * Create a new set of company books. Obvisouly, this is the first command, and
 * likewise, really, only ever run once!
 * 
 * @author Andrew Cowie
 */
public class InitBooksCommand extends Command
{
	private Books	_root;
	boolean			ready	= false;

	/**
	 * This assumes that the global DataStore has already been initialized (the
	 * contract required by the Command() constructor).
	 */
	public InitBooksCommand() {
		super("InitBooks");

		_root = new Books();
		/*
		 * We use the built in sets of db4o for greater efficiency and for their
		 * autoactivation and update features.
		 */
		_root.setAccountsSet(_store.newSet());
		_root.setCurrencySet(_store.newSet());
	}

	public void setHomeCurrency(Currency home) {
		_root.setHomeCurrency(home);

		Set currencies = _root.getCurrencySet();
		currencies.add(home);
		ready = true;
	}

	/*
	 * Override inherited Command methods -------------
	 */

	public boolean isComplete() {
		return ready;
	}

	protected void persist(UnitOfWork uow) {
		uow.registerDirty(_root);
	}

	public void undo() {
		throw new UnsupportedOperationException();
	}
}
