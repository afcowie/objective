/*
 * DataStore.java
 * 
 * See LICENCE file for usage and redistribution terms
 * Copyright (c) 2005 Operational Dynamics
 */
package accounts.persistence;

import generic.util.Debug;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.NoSuchElementException;

import accounts.domain.Amount;
import accounts.domain.Books;
import accounts.domain.Datestamp;
import accounts.domain.Ledger;

import com.db4o.Db4o;
import com.db4o.ObjectContainer;
import com.db4o.ObjectSet;
import com.db4o.config.Configuration;
import com.db4o.config.ObjectClass;
import com.db4o.query.Query;

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
		Debug.register("db4o");
		// config.messageLevel(3);
		// config.singleThreadedClient(true);
		config.callbacks(false);

		/*
		 * This is so stupid - why on earth _wouldn't_ you blow an exception if
		 * you can't store something?!? So set it to do so.
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

		/*
		 * The classes for which we want to turn on cascade {update,activate}
		 * behaviour.
		 * 
		 * At the moment, we leave out Account.class, Ledger.class, Entry.class,
		 * Transaction.class because objects are activated by most resolved
		 * subtype. (TODO: we need to find out how to change this behaviour so
		 * we can tune it better)
		 */
		Class[] cascadeClasses = {
		Books.class,
		LinkedHashSet.class,
		};

		for (int i = 0; i < cascadeClasses.length; i++) {
			ObjectClass db4oObjectClass = config.objectClass(cascadeClasses[i]);
			/*
			 * By turning cascade on (particularly for the Collection classes
			 * included in the array above) we create the magic that any time
			 * one of them is activated it will start a new {update,activate}
			 * through depth.
			 */
			db4oObjectClass.cascadeOnActivate(true);
			db4oObjectClass.cascadeOnUpdate(true);
			/*
			 * Most of these contain a Set of subelements. Make sure it is
			 * {updated,activated} through it's own internal members (usually 2
			 * or so deep) to reach the elements themselves.
			 */
			db4oObjectClass.minimumActivationDepth(5);
			db4oObjectClass.updateDepth(5);
		}

		Class[] leafClasses = {
		Datestamp.class,
		Amount.class,
		};

		for (int i = 0; i < leafClasses.length; i++) {
			ObjectClass db4oObjectClass = config.objectClass(leafClasses[i]);
			/*
			 * These leaf classes only contain primative fields, so stop seeking
			 * further.
			 */
			db4oObjectClass.cascadeOnActivate(false);
			db4oObjectClass.cascadeOnUpdate(false);
			db4oObjectClass.minimumActivationDepth(0);
			db4oObjectClass.updateDepth(0);
		}

		/*
		 * Default update depth is zero, so turn on the magic! A setting of 1
		 * would probably do, but only if we were rigourously set()ing
		 * subelements and so on. Validating, we find the following value
		 * necessary. This is probably a fairly significant target for
		 * performance tuning, but only commit a change to this value after
		 * extensive testing!
		 */
		config.updateDepth(5);
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
	 * Get a specified Ledger object. Query by example is nice, of course, but
	 * in our case we have a complex linked relationship between Accounts (which
	 * have titles) and the one-or-more Ledgers they contain (each with a
	 * description). This wraps querying into the database to find a single
	 * specific Ledger.
	 * 
	 * @param accountTitle
	 *            the Account's title to look for
	 * @param ledgerName
	 *            the Ledger to look for within this account.
	 * @return a Ledger if one found, or null.
	 */
	public Ledger getLedger(String accountTitle, String ledgerName) {
		/*
		 * Get db4o's Query interface
		 */

		Query query = container.query();

		/*
		 * We work inside-out here. Actually, we want ledgers
		 */
		query.constrain(Ledger.class);

		/*
		 * Constrain it with the account and ledger information
		 */
		query.descend("name").constrain(ledgerName).contains();

		Query subquery = query.descend("parentAccount");
		subquery.descend("title").constrain(accountTitle).contains();

		ObjectSet os = query.execute();

		/*
		 * TODO NOTES: This is SO implementation specific it makes me sick. It
		 * should really be somewhere closer to the code. On the other hand, it
		 * is also db4o specific, so this isn't such a bad spot for it. I guess
		 * it depends on how many finders we end up needing.
		 */

		final int len = os.size();

		if (len > 1) {
			throw new UnsupportedOperationException(
				"When calling getLedger(), you need to specify arguments such that only one ledger will be retreived!");
		}

		Object obj = os.next();

		if (!(obj instanceof Ledger)) {
			throw new IllegalStateException("In querying Ledgers, you managed to get something not a Ledger!");
		}

		return (Ledger) obj;
	}
}