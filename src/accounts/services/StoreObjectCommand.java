/*
 * StoreObjectCommand.java
 * 
 * See LICENCE file for usage and redistribution terms
 * Copyright (c) 2006 Operational Dynamics
 */
package accounts.services;

import accounts.persistence.UnitOfWork;

/**
 * Store a generic Object to the DataStore. Like all Commands this is just a
 * wrapper around the basic DataStore.save() functionality (which in turn is
 * just a wrapper around db4o's ObjectContainer.set()) - however this one does
 * no domain specific validation and so is appropriate for the cases where that
 * simply isn't required, but you to still need (ought) to use the Command and
 * UnitOfWork signalling framework.
 * 
 * @author Andrew Cowie
 */
public class StoreObjectCommand extends Command
{
	private transient Object	obj;

	public StoreObjectCommand(Object o) {
		super();
		if (o == null) {
			throw new IllegalArgumentException("Can't store null");
		}
		this.obj = o;
	}

	protected void action(UnitOfWork uow) throws CommandNotReadyException {
		uow.registerDirty(obj);
	}

	protected void reverse(UnitOfWork uow) throws CommandNotUndoableException {
		throw new CommandNotUndoableException();
	}

	public String getClassString() {
		return "Store Object";
	}
}