/*
 * DataClient.java
 * 
 * See LICENCE file for usage and redistribution terms
 * Copyright (c) 2006 Operational Dynamics
 * 
 * Code originally accounts.persistence.DataStore,
 * Copyright (c) 2005-2006 Operational Dynamics
 */
package accounts.persistence;

import java.util.List;
import java.util.NoSuchElementException;

import accounts.domain.Books;

import com.db4o.ObjectContainer;
import com.db4o.ObjectSet;

/**
 * A connection to an accounting database. This wraps the mechanics of reading
 * and writing to the underlying db4o database.
 * <p>
 * <b>This is not a domain class, and since we don't want it stored down into
 * the db4o database (what a recursive mess that would be!) no domain classes
 * should reference it!</b>
 * <p>
 * Incidentally, if you were to ever want to change the underlying persistence
 * mechanism, this could be turned into an interface as this class is the
 * abstraction point.
 * 
 * @author Andrew Cowie
 */
public final class DataClient
{
	private transient ObjectContainer	container	= null;
	private transient boolean			readOnly	= false;
	private transient Books				books		= null;

	/**
	 * Create a new DataClient instance around a {@link ObjectContainer}.
	 */
	DataClient(ObjectContainer container) throws IllegalStateException {
		if (container == null) {
			throw new IllegalArgumentException("A bit hard to be instantiating a client with a null ObjectContainer");
		}
		this.container = container;
	}

	/**
	 * Mark this DataClient as read-only. This is enforced here by preventing
	 * {@link #save()} and {@link #commit()}.
	 */
	/*
	 * Enforced here rather than using db4o's read-only configuration option as
	 * apparently that mode inhibits inter client communication and updates
	 * inside the db4o engine.
	 */
	void setReadOnly() {
		this.readOnly = true;
	}

	/**
	 * Commit the current transaction (and implicitly, start a new one)
	 */
	public void commit() {
		if (readOnly) {
			throw new UnsupportedOperationException("Can't commit on a read-only client!");
		}
		container.commit();
	}

	/**
	 * Rollback the current transaction (implicitly, db4o starts a new one for
	 * you per its practice of there always being an open transaction). Of
	 * course, this only reverts changes in flight due to previous save() calls;
	 * any objects which you have dirtied which you still hold references to
	 * must be refreshed with {@link #reload(Object)}.
	 */
	public void rollback() {
		container.rollback();
	}

	/**
	 * Reload (refresh) an individual object from the database, typically after
	 * a rollback. Per db4o's JavaDoc, after a rollback changed objects should
	 * be restored with a deactivate() call followed by an activate() call; this
	 * method does this.
	 */
	public void reload(Object obj) {
		synchronized (obj) {
			container.deactivate(obj, 2);
			container.activate(obj, 2);
		}
	}

	/**
	 * Close this connection to the data store. <b>Danger! db4o has a default
	 * commit-on-close behaviour!</b> So this method has package visibility,
	 * and the only thing that should be calling this is
	 * {@link DataServer#close()} which rigourously calls rollback() immediately
	 * before hand.
	 */
	void close() {
		container.close();

		/*
		 * Once closed we aren't reoppening this one. It's still a long way down
		 * the graph, but closing is preparatory to finalization and garbage
		 * collection, so help nudge things along.
		 */
		container = null;
	}

	/**
	 * Wraps {@link ObjectContainer#set(java.lang.Object)}. I prefer
	 * Hibernate's term for this, so "save" it is.
	 */
	public void save(Object obj) {
		if (readOnly) {
			throw new UnsupportedOperationException("Can't save() through a read-only client!");
		}
		if (container.ext().isClosed()) {
			throw new IllegalStateException("You can't save() if the container is closed!");
		}
		try {
			container.set(obj);
		} catch (Exception e) {
			System.err.println("FIXME! Uncaught exception when trying to set()");
			e.printStackTrace();
			System.err.println("FIXME! Continuing...");
		}
	}

	/**
	 * Get db4o {@link ObjectContainer}. For most purposes, the wrapper methods
	 * provided by this class should be used to access the datastore. But
	 * there's no overt reason to hide this, particularly for unit testing.
	 * 
	 * @return The {@link ObjectContainer}that this class wraps.
	 */
	ObjectContainer getUnderlyingContainer() {
		return container;
	}

	/**
	 * Get the root Books object. Since DataClient wraps an accounts database,
	 * we have a few utility methods to get to that Object hierarchy. Note that
	 * DataClient caches this lookup once performed.
	 */
	public Books getBooks() {
		if (books == null) {
			ObjectSet os = container.get(Books.class);

			if (os.size() > 1) {
				throw new IllegalStateException(
					"Whoa. You managed to get more than one Books object into the database. That's really bad.");
			} else if (os.size() == 0) {
				throw new NoSuchElementException("No Books object in this container!");
			} else {
				this.books = (Books) os.next();
			}
		}
		return books;
	}

	/**
	 * Set the cached Books object. Internal use only; does not commit to
	 * database. To be used by InitBooksCommand to allow chaining; regardless of
	 * Command using this, a commit of the Books object to the database needs to
	 * be done via a UnitOfWork.
	 * 
	 * @param root
	 *            the Books object to be cached in this DataClient object.
	 */
	public void setBooks(Books root) {
		if (books == null) {
			if (root != null) {
				this.books = root;
			} else {
				throw new IllegalArgumentException(
					"Can't set a null Books object to DataClient's internally cached root reference.");
			}
		} else {
			throw new UnsupportedOperationException(
				"You aren't supposed to call setBooks unless initializing a DataClient via InitBooksCommand");
		}
	}

	/**
	 * Expose the ability to query by example. Most persistence engines (and, in
	 * the present instance, the implemented db4o database) provide the ability
	 * to get objects from the database by specifying a partially instantiated
	 * object prototype which is used to constrain the query. This call wraps
	 * the underlying db4o implementation and returns a Java List instead of
	 * db4o's ObjectSet.
	 * 
	 * @param example
	 *            the example object whose fields provide the prototype which
	 *            constrains the returned collection of objects. If a class
	 *            literal is provided (ie Ledger.class) then all Ledger objects
	 *            persisted in the database will be retrieved.
	 * @return a List of the objects retreieved from the database. If no objects
	 *         are fetched, then the list will not be null but will have size 0.
	 *         The List is actually a db4o ObjectSet (implements List) which
	 *         does lazy but automatic instantiation as you iterate over it.
	 * @see com.db4o.ObjectSet
	 */
	public List queryByExample(Object example) {
		ObjectSet os = container.get(example);
		return os;
	}

	/**
	 * Expose the "native query" interface provided by db4o. The predicate
	 * argument is an (typically anonymous) Selector with a match() method. For
	 * example,
	 * 
	 * <pre>
	 * List	result	= nativeQuery(new Selector() {
	 * 					public boolean match(Type t) {
	 * 						// something with t
	 * 					}
	 * 				});
	 * </pre>
	 * 
	 * @param predicate
	 *            a Selector implementing a match(SomeObject) method which
	 *            returns true if SomeObject is to be included in the result
	 *            set.
	 * @return a List of the objects retreieved from the database. If no objects
	 *         are fetched, then the list will not be null but will have size 0.
	 *         The List is actually a db4o ObjectSet (implements List) which
	 *         does lazy but automatic instantiation as you iterate over it.
	 * @see Selector
	 * @see com.db4o.query.Predicate
	 * @see com.db4o.ObjectSet
	 */
	public List nativeQuery(Selector predicate) {
		ObjectSet os = container.query(predicate);
		return os;
	}

	/**
	 * Retrieve a copy of the object as it is committed to the database.
	 * 
	 * @param original
	 *            The object whose persisted value you want to peek.
	 * @return a transient object representing the currently committed value of
	 *         the object; this object is <b>disconnected</b> from the
	 *         datastore, activated with depth 2.
	 * @see com.db4o.ext.ExtObjectContainer#peekPersisted(Object, int, boolean)
	 */
	public Object peek(Object original) throws NoSuchElementException {
		Object aboo = container.ext().peekPersisted(original, 2, true);

		if (aboo == null) {
			throw new NoSuchElementException("No committed version of " + original + " persisted");
		}
		return aboo;
	}

	/**
	 * The default db4o behaviour is that deletes are NOT recursive (cascading
	 * in their parleance). You can change that on a per class or per field
	 * basis by making settings in the static block of {@link DataStore}.
	 * 
	 * @param target
	 *            the Object to be deleted from the database. Only target will
	 *            be removed, not any of the objects that comprise its fields.
	 */
	public void delete(Object target) {
		container.delete(target);
	}
}