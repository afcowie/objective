/*
 * Command.java
 * 
 * See LICENCE file for usage and redistribution terms
 * Copyright (c) 2005 Operational Dynamics
 */
package accounts.services;

import generic.util.Debug;
import accounts.client.ObjectiveAccounts;
import accounts.persistence.DataStore;
import accounts.persistence.UnitOfWork;

/**
 * The top of the command hierarchy. These classes are the distinct operations
 * that cause changes to be made to the underlying domain model (ie, the command
 * pattern). <BR>
 * <B>COMMAND OBJECTS ARE NOT COMMITRED TO THE DEMO_DATABASE</B>
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
	protected transient DataStore	_store		= null;
	protected transient boolean		_executed	= false;
	protected transient String		_name		= null;

	/**
	 * Commands all have a transient reference to the system's open DataStore.
	 * This constructor checks that it is initialized as a safety check. WARNING
	 * this will have to change if we ever stop having one global DataStore.
	 * 
	 * @param name
	 *            A debug visible name for the Command subclass.
	 */
	public Command(String name) {
		if (ObjectiveAccounts.store == null) {
			throw new IllegalStateException("Trying to setup a Command but the static DataStore is not initialized.");
		} else {
			_store = ObjectiveAccounts.store;
		}
		if ((name == null) || (name.equals(""))) {
			throw new IllegalArgumentException(
					"Command constructor needs to be called with a tag to be used in Debug messages to identify it.");
		} else {
			_name = name;
		}
	}

	/**
	 * Report whether or not all the necessary aspects of the Command have been
	 * decided, ie, if the Command is ready to execute.
	 */
	public abstract boolean isComplete();

	/**
	 * Carry out the changes to the underlying datastore. You should be using
	 * DateStore _store.save() here. DO NOT increase the visibility of this
	 * method to public. It should only be called via Command.execute().
	 * 
	 * @throws CommandNotReadyException
	 *             if your code needs to abort the Command. TODO rollback?
	 */
	protected abstract void persist(UnitOfWork uow) throws CommandNotReadyException;

	/**
	 * Save the Command's changes to the underlying datastore. This method calls
	 * isComplete() [which subclasses must implement], followed by persist()
	 * [also to be implemented by subclasses]. It doesn't call the store
	 * specific commit - that is up to the application holding the UnitOfWork to
	 * call that UnitOfWork's .commit()
	 */
	public void execute(UnitOfWork uow) throws CommandNotReadyException {
		/*
		 * First callback: ask the subclass if it is "ready".
		 */
		Debug.print("command", _name + " checking isComplete()");
		if (!(isComplete())) {
			throw new CommandNotReadyException();
		}

		/*
		 * Second callback: Execute the code to actually save the results of the
		 * Command in the DataStore. Also throws CommandNotReadyException.
		 */
		Debug.print("command", _name + " executing persist()");
		persist(uow);

		_executed = true;
	}

	/**
	 * Undo the affects of this Command. Undo is not rollback. In the case of
	 * updates, it instead reapplies the previous state (stored in this object).
	 * In the case of new object commands, it carries out deletes.
	 * <P>
	 * TODO: Should undo automatically commit? Presumably.
	 * <P>
	 * TODO: How do we signal the UI layer? Since this is initiated by the UI
	 * layer, presumably *it* takes care of that.
	 * 
	 * <P>
	 * an implentation of undo() must check the _executed field; if it's true,
	 * then the undo should proceed. If false, then undo should instead TODO
	 * rollback()?
	 */
	public abstract void undo();

	// _store.rollback();
	// _store.getContainer().rollback();
	// _store.close(); // ????????????????????
}
