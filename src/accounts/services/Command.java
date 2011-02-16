/*
 * ObjectiveAccounts, accounting for small professional services firms.
 *
 * Copyright © 2005-2011 Operational Dynamics Consulting, Pty Ltd
 *
 * The code in this file, and the program it is a part of, is made available
 * to you by its authors as open source software: you can redistribute it
 * and/or modify it under the terms of the GNU General Public License version
 * 2 ("GPL") as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GPL for more details.
 *
 * You should have received a copy of the GPL along with this program. If not,
 * see http://www.gnu.org/licenses/. The authors of this program may be
 * contacted via http://research.operationaldynamics.com/projects/objective/.
 */
package accounts.services;

import generic.persistence.DataClient;
import generic.persistence.Engine;
import generic.util.Debug;

/**
 * The top of the command hierarchy. These classes are the distinct operations
 * that cause changes to be made to the underlying domain model (ie, the
 * command pattern). <BR>
 * <B>COMMAND OBJECTS ARE NOT COMMITTED TO THE DATABASE</B>
 * <P>
 * Commands underlie the user interface. Any user interface that updates the
 * domain will use a Command object to do so. While this is slightly
 * cumbersome in terms of parallel classes, it means that the commands can be
 * fully validated through unit tests separate from the logic to translate
 * from UI to equivalent domain changes. The Command hierarchy is in the
 * services layer because they use the persistence layer directly to find and
 * store domain objects.
 * <P>
 * Unlike the domain model, we do not use a deep tree here. While immediate
 * subclasses like NewCommand and UpdateCommand and DeleteCommand are
 * tempting, many commands will have aspects of both New <I>and</I> Update.
 * Being able to identify one over the other is a bit meaningless.
 * <P>
 * With luck Commands will be undoable via a stack, but that implies that they
 * are able to hold a complete description of the previous state. Obviously we
 * could do this if we held a copy of the <i>entire</i> domain object tree,
 * but that is really rather excessive. On the other hand, only carrying the
 * prior state of objects we know we touched implies that we know the
 * implications of what each of those touches are - frought with possibility
 * of error.
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
    private transient boolean executed = false;

    /**
     * 
     * This constructor checks that the persistence Engine has an open
     * datafile to work with as a safety check.
     */
    public Command() {
        if (Engine.server == null) {
            throw new IllegalStateException(
                    "Trying to setup a Command but the Engine is not initialized.");
        }
    }

    /**
     * Carry out the work of the command and save the changes to the
     * underlying datastore. You should be using store.save() here. DO NOT
     * increase the visibility of this method to public. It should only be
     * called via Command.execute().
     * 
     * @throws CommandNotReadyException
     *             If your code needs to abort the Command because the state
     *             isn't correct or prerequisites are not met.
     */
    /*
     * TODO Rollback? Try again? If so, code to do so automatically will go in
     * execute()
     */
    protected abstract void action(DataClient store) throws CommandNotReadyException;

    /**
     * Save the Command's changes to the underlying datastore. This method
     * calls action() [which subclasses must implement]. Since Commands can be
     * chained or grouped in logical sets, neither this method nor action()
     * call commit() - it is up to the application holding the DataClient to
     * do that.
     */
    public final void execute(DataClient store) throws CommandNotReadyException {
        if (store == null) {
            throw new IllegalArgumentException("Null UnitOfWork passed!");
        }
        if (executed) {
            throw new IllegalStateException("You can't execute a Command that has already been run!");
        }
        /*
         * Callback: Execute the code to actually save the results of the
         * Command in the DataClient. Throws CommandNotReadyException.
         */
        Debug.print("command", "executing " + getClassString());
        action(store);

        executed = true;
    }

    /**
     * Reverse the effects of action(). This is implemented by subclasses of
     * Command and is called by undo() to actually reverse the actions
     * previously taken by this command.
     */
    protected abstract void reverse(DataClient store) throws CommandNotUndoableException;

    /**
     * Undo the affects of a Command. Undo is not rollback. In the case of
     * updates, you must instead reapply the previous state (assuming it is
     * available). In the case of new object commands, you must carry out a
     * delete. The code calling this is responsible to .commit() the
     * DataClient afterwards.
     */
    public final void undo(DataClient store) throws CommandNotUndoableException {
        if (store == null) {
            throw new IllegalArgumentException("Null UnitOfWork passed!");
        }
        /*
         * Check forward/back status
         */
        if (!executed) {
            throw new IllegalStateException("Can't undo a Command that hasn't been executed");
        }

        Debug.print("command", "undoing " + getClassString());
        reverse(store);

        executed = false; // ?
    }

    /**
     * Return a human readable name for the Command. Each command should
     * override this.
     */
    public abstract String getClassString();
}
