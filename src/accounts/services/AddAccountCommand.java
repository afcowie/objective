/*
 * AddAccountCommand.java
 * 
 * See LICENCE file for usage and redistribution terms
 * Copyright (c) 2005 Operational Dynamics
 */
package accounts.services;

import java.util.Set;

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
	private transient Account	_account	= null;
	private transient Set		_accounts	= null;

	/**
	 * Create a new AddAccount Command.
	 * 
	 */
	public AddAccountCommand() {
		super("AddAccount");
	}

	/**
	 * @param account
	 *            The Account to add.
	 */
	public void setAccount(Account account) {

		Books root = _store.getBooks();
		_accounts = root.getAccountsSet();
		_accounts.add(account); // TODO should throw on dup?

		/*
		 * Mark as ready to rock.
		 */
		_account = account;
	}

	/*
	 * Override inherited Command methods -------------
	 */

	public boolean isComplete() {
		if (_account != null) {
			return true;
		} else {
			return false;
		}
	}

	protected void persist(UnitOfWork uow) {
		/*
		 * Store the new account itself.
		 */
		uow.registerDirty(_account);
		/*
		 * And update the collection
		 */
		uow.registerDirty(_accounts);
	}

	public void undo() {
		throw new UnsupportedOperationException();
	}
}
