/*
 * AddIdentifierCommand.java
 * 
 * See LICENCE file for usage and redistribution terms
 * Copyright (c) 2006 Operational Dynamics
 */
package accounts.services;

import accounts.domain.IdentifierGroup;
import accounts.persistence.UnitOfWork;

/**
 * Store changes to an IdentifierGroup and, implicitly, its underlying
 * Identifiers. Unlike things like Account which whose Create, Update and most
 * especially Delete need to be handled with different care, Identifiers and
 * their containing IdentifierGroup are imdepotent and so we can leverage our
 * underlying DataStore's magic create & update being the same operation.
 * <p>
 * Note that if you've removed an Identifier from an IdentifierGroup and use
 * this to persist it, the Identifier itself is NOT removed from the DataStore.
 * This is because it could be in use and will be required as a legacy
 * reference. If you're really trying to delete the identifier itself, use
 * DeleteIdentifierCommand.
 * 
 * @author Andrew Cowie
 */
public class StoreIdentifierGroupCommand extends Command
{
	private transient IdentifierGroup	group;

	/**
	 * 
	 * @param identifier
	 *            the Identifier subclass you wish to add to the DataStore
	 */
	public StoreIdentifierGroupCommand(IdentifierGroup group) {
		if (group == null) {
			throw new IllegalArgumentException("Can't construct with null as the IdentifierGroup");
		}
		this.group = group;
	}

	protected void action(UnitOfWork uow) throws CommandNotReadyException {

		// IdentifierGroup storedGroup = (IdentifierGroup)
		// ObjectiveAccounts.store.getContainer().ext().peekPersisted(
		// group, 3, true);

		uow.registerDirty(group);
	}

	protected void reverse(UnitOfWork uow) throws CommandNotUndoableException {
		throw new UnsupportedOperationException();

	}

	public String getClassString() {
		return "Store an IdentifierGroup";
	}

}