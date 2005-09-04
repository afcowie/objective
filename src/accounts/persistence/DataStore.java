/*
 * DataStore.java
 * 
 * See LICENCE file for usage and redistribution terms
 * Copyright (c) 2005 Operational Dynamics
 */
package accounts.persistence;

import generic.util.DebugException;

import java.io.File;
import java.io.IOException;
import java.util.Set;

import accounts.domain.Books;

import com.db4o.Db4o;
import com.db4o.ObjectContainer;
import com.db4o.ObjectSet;
import com.db4o.config.Configuration;
import com.db4o.config.ObjectClass;

/**
 * Root class for an accounting database. This is not considered a domain class,
 * and we don't want it stored down into the db4o database, so no domain classes
 * should reference it.
 * 
 * This wraps the mechanics of configuring and accessing the underlying db4o
 * database. As we build up safety and recovery routines, they will be added
 * here.
 * 
 * @author Andrew Cowie
 */
public class DataStore
{
	private ObjectContainer	_container	= null;

	/**
	 * Configure the Db4o engine. Apparently this is once per VM, so here it is.
	 */
	static {
		Configuration config = Db4o.configure();

		/*
		 * This is so stupid - why on earth <I>wouldn't<I> you blow an
		 * exception if you can't store something?!? So set it to do so.
		 */
		config.exceptionsOnNotStorable(true);

		/*
		 * Apparently the default activation depth is five. Good. Set it anyway.
		 */
		config.activationDepth(5);

		/*
		 * But it needs to be further tweaked in a few key places.
		 */
		config.classActivationDepthConfigurable(true);

		ObjectClass booksObjectClass = config.objectClass(Books.class);
		booksObjectClass.cascadeOnActivate(true);
		booksObjectClass.cascadeOnUpdate(true);

		/*
		 * Default update depth is zero, so turn on the magic! A setting of 1
		 * would probably do, so 3 is good Russian style overengineering. Even
		 * so, this probably needs validation.
		 */
		config.updateDepth(3);
	}

	/**
	 * Instantiate a new accounting database object. Will create a new one if it
	 * doesn't exist (per the behaviour of {@link Db4o.openFile()}). WARNING
	 * This is assumed to be the first use of the Db4o engine, so this is where
	 * we do global configuration of Db4o.
	 * 
	 * @param filename
	 *            the database to open.
	 */
	public DataStore(String filename) {
		_container = Db4o.openFile(filename);
	}

	/**
	 * Factory method to obtain a new Set, optimized (and tied to) to the
	 * underlying persistence store.
	 * 
	 * @return An empty Set. Actually returns a {@link Db4oSet}.
	 */
	public Set newSet() {
		if (_container == null) {
			throw new DebugException("You managed ask for a new Linked List without having the _container initialized.");
		}
		// was _container.ext().collections().newLinkedList();
		return new Db4oSet(_container);
	}

	/**
	 * Commit the current transaction (and implicitly, start a new one)
	 */
	public void commit() {
		_container.commit();
	}

	/**
	 * Close the underlying data store. Commits.
	 */
	public void close() {
		_container.commit(); // be explicit!
		_container.close();
	}

	/**
	 * Wraps {@link ObjectContainer#set(java.lang.Object)}. I prefer
	 * Hibernate's term for this, so "save" it is.
	 */
	public void save(Object obj) {
		_container.set(obj);
	}

	/**
	 * Initiate a hot backup of the db4o database. Note that we do not permit
	 * overwrites.
	 * 
	 * @param path
	 *            a fully qualified pathname.
	 * 
	 * @throws IOException
	 * @see com.db4o.ext.ExtObjectContainer#backup()
	 */
	public void backup(String path) throws IllegalArgumentException, IOException {
		/*
		 * The default behaviour of ExtObjectContainer.backup() is to overwrite
		 * an existing backup. We don't do that here.
		 */
		if (new File(path).exists()) {
			throw new IllegalArgumentException("Specifed backup target already exists");
		}

		/*
		 * This blocks, I assume, seeing as how it throws something?!?
		 */
		_container.ext().backup(path);
	}

	/**
	 * Get db4o {@link ObjectContainer}. For most purposes, the wrapper methods
	 * provided by this class should be used to access the datastore. But
	 * there's no overt reason to hide this, particularly for unit testing.
	 * 
	 * @return The {@link ObjectContainer}that this class wraps.
	 */
	public ObjectContainer getContainer() {
		return _container;
	}

	/*
	 * Experimental ---------------------------------------
	 */

	/**
	 * Get the root Books object. Since DataStore wraps an accounts database, we
	 * have a few utility methods to get to that Object hierarchy.
	 */
	public Books getBooks() {
		ObjectSet os = _container.get(Books.class);

		if (os.size() > 1) {
			throw new IllegalStateException(
					"Whoa. You managed to get more than one Books object into the database. That's really bad.");
		} else if (os.size() == 0) {
			return null;
		} else {
			return (Books) os.next();
		}
	}
}