/*
 * Command.java
 * 
 * See LICENCE file for usage and redistribution terms
 * Copyright (c) 2005 Operational Dynamics
 */
package accounts.services;

import generic.util.Debug;
import accounts.client.ObjectiveAccounts;
import accounts.persistence.UnitOfWork;

/**
 * The top of the command hierarchy. These classes are the distinct operations
 * that cause changes to be made to the underlying domain model (ie, the command
 * pattern). <BR>
 * <B>COMMAND OBJECTS ARE NOT COMMITTED TO THE DATABASE</B>
 * <P>
 * Commands underlie the user interface. Any user interface that updates the
 * domain will use a Command object to do so. While this is slightly cumbersome
 * in terms of parallel classes, it means that the commands can be fully
 * validated through unit tests separate from the logic to translate from UI to
 * equivalent domain changes. The Command hierarchy is in the services layer
 * because they use the persistence layer directly to find and store domain
 * objects.
 * <P>
 * Unlike the domain model, we do not use a deep tree here. While immediate
 * subclasses like NewCommand and UpdateCommand and DeleteCommand are tempting,
 * many commands will have aspects of both New <I>and</I> Update. Being able to
 * identify one over the other is a bit meaningless.
 * <P>
 * With luck Commands will be undoable via a stack, but that implies that they
 * are able to hold a complete description of the previous state. Obviously we
 * could do this if we held a copy of the <i>entire</i> domain object tree, but
 * that is really rather excessive. On the other hand, only carrying the prior
 * state of objects we know we touched implies that we know the implications of
 * what each of those touches are - frought with possibility of error.
 * 
 * @author Andrew Cowie
 * @author Robert Collins
 */
public abstract class Command
{
	/*
	 * Strictly, there's no reason for these to be transient, but as we are
	 * dangerously close to the persisted domain objects here, this a) helps
	 * reinforce the point tha the commands themselves are not being persisted
	 * (unlike, say, Prevayler) and b) to provide a syntax guard against
	 * persisting these fields by accident.
	 */
	private transient boolean	executed	= false;

	/**
	 * Commands all have a transient reference to the system's open DataStore.
	 * This constructor checks that it is initialized as a safety check. WARNING
	 * this will have to change if we ever stop having one global DataStore.
	 */
	public Command() {
		if (ObjectiveAccounts.store == null) {
			throw new IllegalStateException("Trying to setup a Command but the static DataStore is not initialized.");
		}
	}

	/**
	 * Carry out the work of the command and save the changes to the underlying
	 * datastore. You should be using uow.registerDirty() here. DO NOT increase
	 * the visibility of this method to public. It should only be called via
	 * Command.execute().
	 * 
	 * @throws CommandNotReadyException
	 *             If your code needs to abort the Command because the state
	 *             isn't correct.
	 */
	/*
	 * TODO Rollback? Try again? If so, code to do so automatically will go in
	 * execute()
	 */
	protected abstract void action(UnitOfWork uow) throws CommandNotReadyException;

	/**
	 * Save the Command's changes to the underlying datastore. This method calls
	 * action() [which subclasses must implement]. It does not call the store
	 * specific commit - that is up to the application holding the UnitOfWork to
	 * call that UnitOfWork's .commit()
	 */
	public final void execute(UnitOfWork uow) throws CommandNotReadyException {
		if (uow == null) {
			throw new IllegalArgumentException("Null UnitOfWork passed!");
		}
		if (!uow.isViable()) {
			throw new IllegalArgumentException("UnitOfWork passed is not viable!");
		}
		if (executed) {
			throw new IllegalStateException("You can't execute a Command that has already been run!");
		}
		/*
		 * Callback: Execute the code to actually save the results of the
		 * Command in the DataStore. Throws CommandNotReadyException.
		 */
		Debug.print("command", getClassString() + " executing action()");
		action(uow);

		executed = true;
	}

	/**
	 * Reverse the effects of action(). This is implemented by subclasses of
	 * Command and is called by undo() to actually reverse the actions
	 * previously taken by this command.
	 */
	protected abstract void reverse(UnitOfWork uow) throws CommandNotUndoableException;

	/**
	 * Undo the affects of this Command. Undo is not rollback. In the case of
	 * updates, it instead reapplies the previous state (stored in this object).
	 * In the case of new object commands, it carries out deletes. The
	 * UnitOfWork that is passed in is responsible to .commit() afterwards.
	 */
	public final void undo(UnitOfWork uow) throws CommandNotUndoableException {
		if (uow == null) {
			throw new IllegalArgumentException("Null UnitOfWork passed!");
		}
		if (!uow.isViable()) {
			throw new IllegalArgumentException("UnitOfWork passed is not viable!");
		}

		/*
		 * Check forward/back status
		 */
		if (!executed) {
			throw new IllegalStateException("Can't undo a Command that hasn't been executed");
		}

		Debug.print("command", getClassString() + " executing reverse()");
		reverse(uow);

		executed = false; // ?
	}

	/**
	 * Return a human readable name for the Command. Each command should
	 * override this.
	 */
	public abstract String getClassString();
}
