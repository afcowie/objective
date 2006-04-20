/*
 * StoreObjectCommand.java
 * 
 * See LICENCE file for usage and redistribution terms
 * Copyright (c) 2006 Operational Dynamics
 */
package accounts.services;

import accounts.persistence.DataClient;

/**
 * Store a generic Object to the DataClient. Like all Commands this is just a
 * wrapper around the basic DataClient.save() functionality (which in turn is
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

	protected void action(DataClient store) throws CommandNotReadyException {
		store.save(obj);
	}

	protected void reverse(DataClient store) throws CommandNotUndoableException {
		throw new CommandNotUndoableException();
	}

	public String getClassString() {
		return "Store Object";
	}
}