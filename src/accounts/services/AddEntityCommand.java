/*
 * AddEntityCommand.java
 * 
 * See LICENCE file for usage and redistribution terms
 * Copyright (c) 2006 Operational Dynamics
 */
package accounts.services;

import generic.util.DebugException;

import java.util.List;

import accounts.client.ObjectiveAccounts;
import accounts.domain.Account;
import accounts.domain.AccountsPayable;
import accounts.domain.AccountsReceivable;
import accounts.domain.Client;
import accounts.domain.ClientLedger;
import accounts.domain.Entity;
import accounts.domain.Ledger;
import accounts.domain.Supplier;
import accounts.domain.SupplierLedger;
import accounts.persistence.IdentifierAlreadyExistsException;
import accounts.persistence.UnitOfWork;

public class AddEntityCommand extends Command
{
	private transient Entity	candidate	= null;
	private transient Ledger	ledger		= null;

	/**
	 * ... Assumes that a ledger in the name of this Entity has not yet been
	 * created.
	 * 
	 * @param entity
	 */
	public AddEntityCommand(Entity entity) throws IdentifierAlreadyExistsException {
		/*
		 * Use DataStore (db4o)'s capability to search by example to see if
		 * there's already an Entity by this name.
		 */

		Entity prototype = new Entity();
		prototype.setName(entity.getName());

		List found = ObjectiveAccounts.store.query(prototype);

		if (found.size() > 0) {
			throw new IdentifierAlreadyExistsException(entity.getName() + " already exists as an Entity");
		}

		/*
		 * Ditto a {Client,Supplier}Ledger by this name
		 */

		Ledger protoLedger;
		if (entity instanceof Client) {
			protoLedger = new ClientLedger();
		} else if (entity instanceof Supplier) {
			protoLedger = new SupplierLedger();
		} else {
			throw new DebugException("Huh? How come neither Client nor Supplier?");
		}
		protoLedger.setName(entity.getName());

		found = ObjectiveAccounts.store.query(protoLedger);

		if (found.size() > 0) {
			throw new IdentifierAlreadyExistsException("There is already an ItemsLedger with " + entity.getName()
					+ " as its name");
		}

		/*
		 * Safety checks passed
		 */
		this.candidate = entity;
		this.ledger = protoLedger;

	}

	/**
	 * Creates the ItemsLedger appropriate to this Entity
	 */
	protected void action(UnitOfWork uow) throws CommandNotReadyException {
		List found;
		if (candidate instanceof Client) {
			found = ObjectiveAccounts.store.query(AccountsReceivable.class);
		} else if (candidate instanceof Supplier) {
			found = ObjectiveAccounts.store.query(AccountsPayable.class);
		} else {
			throw new DebugException(
					"Huh? How did this AddEntityCommmand come to have neither Client nor Supplier as its candidate Entity?");
		}

		if (found.size() > 1) {
			throw new DebugException("As coded, we only allow for there being one Accounts{Receivable|Payable} accout");
		} else if (found.size() == 0) {
			throw new IllegalStateException("Where is the Accounts{Receivable|Payable} account?");
		}

		Account trade = (Account) found.get(0);
		trade.addLedger(ledger);

		// TODO automatically? Linking them in action()?

		uow.registerDirty(candidate);
		uow.registerDirty(trade);
	}

	protected void reverse(UnitOfWork uow) throws CommandNotUndoableException {
		// TODO Auto-generated method stub

	}

	public String getClassString() {
		return "Add Entity";
	}

}
