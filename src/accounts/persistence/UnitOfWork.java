/*
 * UnitOfWork.java
 * 
 * See LICENCE file for usage and redistribution terms
 * Copyright (c) 2005-2006 Operational Dynamics
 */
package accounts.persistence;

import generic.util.Debug;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;

/**
 * Mediate access to the datastore. Allows working applicaiton level
 * transactions to register their interest in objects (being notified if that
 * object changes); transactions which modify domain objects can use this
 * mechanism to note the objects they are dirtying so that this notification
 * will happen when changes are committed.
 * <p>
 * Once taken, a UnitOfWork must be completed by either calling commit() to
 * persist the changes made, or cancel() to release the registrations. Failure
 * to do so will leak memory.
 * <p>
 * This is in the persistence package, but in some sense it's really a part of
 * the services facade which underlies the user interface. Any time the
 * application needs to access stored Objects for a user visable length of time
 * (ie any UI Window that displays domain data from the DataClient), it must grab
 * a UnitOfWork and note which objects it is using which it needs to know about
 * if they change state. Likewise, an editor Window, wizard, or command-line
 * tool which wants to make changes to the domain data must get a UnitOfWork and
 * register the Objects it is changing. Obviously, you can always access the
 * DataClient directly and use its save() method directly, but you will loose the
 * notification aspect.
 * <p>
 * This functionality could have been implemented using the callbacks provided
 * by db4o, but that is bottom up and requies implementation in each and every
 * domain class. This way, it can cross user interactions and cross multiple
 * simultaneous workers.
 * 
 * @author Andrew Cowie
 * @deprecated
 */
public class UnitOfWork
{
	private transient HashSet				dirtyObjects			= null;
	private transient HashSet				interestingObjects		= null;
	private transient HashSet				myUpdateListeners		= null;

	/**
	 * As objects express interest, their callbacks are added to this list. They
	 * are to be removed on cleanup! LinkedHashSet chosen for the ability to
	 * quickly iterate over set.
	 */
	private transient static LinkedHashSet	globalUpdateListeners	= null;

	/**
	 * A unit of work can only be "used" once; that is, once it has been
	 * committed it's no longer valid and shouldn't be used anymore. active is
	 * true when valid.
	 */
	private transient boolean				active					= false;

	/**
	 * Only used for debugging, but it allows us to say where an update signal
	 * came from in debug output
	 */
	private transient String				name					= null;

	// for convenience
	private transient DataClient				store;

	static {
		globalUpdateListeners = new LinkedHashSet();
	}

	/**
	 * Establish a new in-flight UnitOfWork.
	 * 
	 * @param name
	 *            A descriptive label to be given for to this UnitOfWork.
	 *            Typically, you want to use the name of the UI Window that
	 *            opened the UnitOfWork.
	 */
	public UnitOfWork(String name) {
		if ((name == null) || (name.equals(""))) {
			throw new IllegalArgumentException("You need to provide a descriptive name when initializing a UnitOfWork");
		}
		this.name = name;

		if (Engine.server == null) {
			throw new IllegalStateException("Trying to init a UnitOfWork but Engine's static DataServer is not initialized.");
		} else {
			store = Engine.gainClient();
		}

		dirtyObjects = new HashSet();
		interestingObjects = new HashSet();
		myUpdateListeners = new HashSet();

		active = true;
	}

	/**
	 * Determine whether or not this UnitOfWork is still valid. This method
	 * exists largely so that
	 * {@link accounts.services.Command#execute(UnitOfWork)} can check before
	 * calling it's action() or undo() methods.
	 * 
	 * @return the state of the internal active flag
	 */
	public boolean isViable() {
		return active;
	}

	/**
	 * Register that you are interested in an object that you've retreived from
	 * the database and that you should be notified upon a change occuring to
	 * one of those objects.
	 * 
	 * @param obj
	 *            the Object you are noting interest in.
	 */
	public void registerInterest(Object obj) {
		if (!active) {
			throw new IllegalStateException("This UnitOfWork is no longer valid for operations. Go get a new one.");
		}

		if (obj == null) {
			throw new IllegalArgumentException("Can't register interest in a null object.");
		}

		interestingObjects.add(obj);
	}

	/**
	 * Tell the UnitOfWork that you are modifying an Object and that, upon
	 * commit, it will need to notify anyone who has registered interest in that
	 * object. <b>note that these Objects will be persisted by
	 * UnitOfWork.commit()! Good use of transient will prevent mistakes!</b>
	 * 
	 * @param obj
	 *            the Object that you are working on that will be written to the
	 *            database if you commit().
	 */
	public void registerDirty(Object obj) {
		if (!active) {
			throw new IllegalStateException("This UnitOfWork is no longer valid for operations. Go get a new one.");
		}

		if (obj == null) {
			throw new IllegalArgumentException("Can't register a null object as dirty.");
		}

		dirtyObjects.add(obj);
	}

	/**
	 * Add an UpdateListener to UnitOfWork's internal table of listeners which
	 * are to be fired when a UnitOfWork somewhere commit()s.
	 * 
	 * @param listener
	 */
	public void onChange(UpdateListener listener) {
		myUpdateListeners.add(listener);
		globalUpdateListeners.add(listener);
	}

	/**
	 * Persist all the changes made by this unit of work. This save()s the dirty
	 * objects, then calls the underlying DataClient's commit()
	 * <p>
	 * Once a UnitOfWork is committed, then it's done and can no longer be used
	 * and will throw IllegalStateException.
	 */
	public void commit() {
		if (!active) {
			throw new IllegalStateException("This UnitOfWork is no longer valid for operations (already committed!)");
		}

		/*
		 * save dirty objects
		 */

		Iterator iter = dirtyObjects.iterator();
		while (iter.hasNext()) {
			Object dirty = iter.next();
			store.save(dirty);
		}
		store.commit();

		/*
		 * iterate over interest list and callback to them
		 */

		iter = globalUpdateListeners.iterator();
		while (iter.hasNext()) {
			UpdateListener listener = (UpdateListener) iter.next();
			Iterator objects = dirtyObjects.iterator();
			while (objects.hasNext()) {
				Object dirty = objects.next();
				listener.changed(dirty);
			}
		}

		/*
		 * cleanup
		 */

		cleanup();
	}

	/**
	 * Rollback the changes pending in this UnitOfWork. Somewhat in variance
	 * with other systems, if you rollback() a UnitOfWork, it continues to be a
	 * valid, in-flight, UnitOfWork. Note that this <b>DOES</b> call rollback
	 * on the underlying DataClient.
	 */
	public void rollback() {
		if (!active) {
			throw new IllegalStateException("This UnitOfWork is no longer valid for operations");
		}

		/*
		 * This probably doesn't do anything, because in all likelikhood nothing
		 * has been set() by this UnitOfWork. TODO remove this?!?
		 */
		store.rollback();

		Iterator iter = dirtyObjects.iterator();
		while (iter.hasNext()) {
			Object dirty = iter.next();
			store.reload(dirty);
		}
	}

	/**
	 * Cancel a unit of work.
	 */
	public void cancel() {
		if (!active) {
			throw new IllegalStateException("This UnitOfWork is no longer valid for operations");
		}
		cleanup();
	}

	/**
	 * Remove all objects from the interest and dirty lists, clear listeners.
	 */
	protected void cleanup() {
		Iterator iter = dirtyObjects.iterator();
		while (iter.hasNext()) {
			Object o = iter.next();
			if (o != null) {
				iter.remove();
			}
		}
		dirtyObjects = null;

		/*
		 * Remove listeners this UnitOfWork might have registered.
		 */
		iter = myUpdateListeners.iterator();
		while (iter.hasNext()) {
			Object listener = iter.next();
			if (listener != null) {
				globalUpdateListeners.remove(listener);
				iter.remove();
			}

		}
		myUpdateListeners = null;
		active = false;
	}

	/**
	 * This is just here as a debugging measure. Any UnitOfWork object that's
	 * being finalized up should long since have let go of all of its lists of
	 * objects of interest, listener registrations, etc. Of course, a leak
	 * elsewhere would cause this to never be called, but this finalizer will at
	 * least trap UnitOfWork itself from causing leaks.
	 */
	protected void finalize() {
		if (active == true) {
			Debug.print("memory", "finalizing a UnitOfWork which was neither committed nor cancelled.");
		}
		if (dirtyObjects != null) {
			int i = 0;

			Iterator iter = dirtyObjects.iterator();
			while (iter.hasNext()) {
				Object dirty = iter.next();
				if (iter != null) {
					iter.remove();
					i++;
				}
			}

			Debug.print("memory", "leak in <" + name + ">; " + i + " removed from dirtyObjects");
		}

		if (myUpdateListeners != null) {
			int i = 0;
			int j = 0;
			Iterator iter = myUpdateListeners.iterator();
			while (iter.hasNext()) {
				Object listener = iter.next();
				if (listener != null) {
					iter.remove();
					i++;
				}

				if (globalUpdateListeners.contains(listener)) {
					globalUpdateListeners.remove(listener);
					j++;
				}
			}

			Debug.print("memory", "leak in <" + name + ">; " + i + " removed from myUpdateListeners and " + j
				+ " removed from globalUpdateListeners");
		}

		try {
			super.finalize();
		} catch (Throwable t) {
		}
	}
}