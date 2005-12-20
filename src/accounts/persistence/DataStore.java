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
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
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
 * Incidentally, if we were to ever want to change the underlying persistence
 * mechanism, this could be turned into an interface as this class is the
 * abstraction point.
 * 
 * @author Andrew Cowie
 */
public class DataStore
{
	private transient ObjectContainer	container	= null;
	private transient Books				books		= null;

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
		container = Db4o.openFile(filename);
	}

	/**
	 * Factory method to obtain a new Set, optimized (and tied to) to the
	 * underlying persistence store.
	 * 
	 * @return An empty Set. Actually returns a {@link Db4oSet}.
	 */
	public Set newSet() {
		if (container == null) {
			throw new DebugException("You managed ask for a new Linked List without having the container initialized.");
		}
		// was container.ext().collections().newLinkedList();
		return new Db4oSet(container);
	}

	/**
	 * Commit the current transaction (and implicitly, start a new one)
	 */
	public void commit() {
		container.commit();
	}

	/**
	 * Rollback the current transaction (and implicitly, start a new one as per
	 * db4o's practice of there always being an open transaction).
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
	 * Close the underlying data store. Commits.
	 */
	public void close() {
		container.commit(); // be explicit!
		container.close();
	}

	/**
	 * Wraps {@link ObjectContainer#set(java.lang.Object)}. I prefer
	 * Hibernate's term for this, so "save" it is.
	 */
	public void save(Object obj) {
		container.set(obj);
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
		container.ext().backup(path);
	}

	/**
	 * Get db4o {@link ObjectContainer}. For most purposes, the wrapper methods
	 * provided by this class should be used to access the datastore. But
	 * there's no overt reason to hide this, particularly for unit testing.
	 * 
	 * @return The {@link ObjectContainer}that this class wraps.
	 */
	public ObjectContainer getContainer() {
		return container;
	}

	/**
	 * Get the root Books object. Since DataStore wraps an accounts database, we
	 * have a few utility methods to get to that Object hierarchy. Note that
	 * DataStore caches this lookup once performed.
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
	 *            the Books object to be cached in this DataStore object.
	 */
	public void setBooks(Books root) {
		if (books == null) {
			if (root != null) {
				this.books = root;
			} else {
				throw new IllegalArgumentException(
						"Can't set a null Books object to DataStore's internally cached root reference.");
			}
		} else {
			throw new UnsupportedOperationException(
					"You aren't supposed to call setBooks unless initializing a DataStore via InitBooksCommand");
		}
	}

	public List query(Object example) {
		ObjectSet os = container.get(example);
		final int len = os.size();
		ArrayList result;

		if (len == 0) {
			result = new ArrayList(0);
		} else {
			result = new ArrayList(len);

			for (int i = 0; i < len; i++) {
				// automatic activation. Cool.
				result.add(os.next());
			}
		}

		return result;
	}
}