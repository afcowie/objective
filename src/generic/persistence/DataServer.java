/*
 * DataServer.java
 * 
 * See LICENCE file for usage and redistribution terms
 * Copyright (c) 2006 Operational Dynamics
 * 
 * Code forked from accounts.persistence.DataStore,
 * Copyright (c) 2005-2006 Operational Dynamics
 */
package generic.persistence;

import generic.domain.Root;
import generic.util.Debug;
import generic.util.DebugException;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;

import com.db4o.Db4o;
import com.db4o.ObjectContainer;
import com.db4o.ObjectServer;
import com.db4o.config.Configuration;
import com.db4o.ext.ExtObjectContainer;

/**
 * An open accounting database. This wraps the mechanics of configuring and
 * accessing the underlying db4o database.
 * <p>
 * In order to amortize data retrieval, we use a connection pool mechanism to
 * reuse clients openned on the database. Get client connections with
 * {@link #gainClient()} and return them to the pool with
 * {@link #releaseClient(DataClient)}.
 * <p>
 * <b>This is not a domain class, and since we don't want it stored down into
 * the db4o database (what a recursive mess that would be!) no domain classes
 * should reference it!</b> Note, however, that all the necessary configuration
 * and tuning of the db4o library engine is done here in a
 * <code>static {...}</code> block.
 * <p>
 * The expected use of this class is to use {@link Engine#newDatafile(String)}
 * or {@link Engine#openDatafile(String, Class)} to open a DataServer and from
 * there to use its static server reference to obtain clients.
 * 
 * @author Andrew Cowie
 */
public final class DataServer
{
	/**
	 * the db4o ObjectServer that is opened by this DataServer over the
	 * specified database file.
	 */
	private ObjectServer		objectServer	= null;

	private static LinkedList	poolAvailable;
	private static Set			poolInUse;

	/**
	 * Configure the Db4o engine. Apparently this is once per VM, and on first
	 * use of the Db4o classs, so here it is.
	 */
	static {
		Debug.register("db4o");
		Debug.register("pool");

		Configuration config = Db4o.configure();
		// config.messageLevel(3);
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
		 * But it needs to be further tweaked in a few key places, which is done
		 * care of the methods on Root.
		 */
		config.classActivationDepthConfigurable(true);

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
	 * Instantiate a new accounting database server object. Will create a new
	 * datafile if it doesn't exist (per the behaviour of
	 * {@link Db4o.openFile()}).
	 * <p>
	 * <b>WARNING</b> This is assumed to be the first use of the Db4o engine,
	 * so global configuration of Db4o needs to be complete before this is
	 * called - see the <code>static {...}</code> code block in this class.
	 * <p>
	 * As coded presently, we open the server for in-process access only, rather
	 * than listening on a TCP port. Easily changed.
	 * 
	 * @param filename
	 *            the database to open.
	 * @param rootType
	 *            the Class object representing your subclass of Root which is
	 *            the top of your object hierarchy. This is used to make sure
	 *            that the static initializations to customize Db4o's
	 *            Configuration have happened by forcing that Root subclass to
	 *            be loaded by the JVM before DataServer creates the db4o
	 *            ObjectServer.
	 * @throws IllegalStateException
	 *             if the database is locked (ie you've got another instance of
	 *             the GUI program up. TODO, when we implement multiuser, this
	 *             will have evolve to starting up the server in TCP mode.
	 */
	DataServer(String filename, Class rootType) throws IllegalStateException {
		if (rootType != null) {
			try {
				if (!Root.class.isAssignableFrom(rootType)) {
					throw new ClassCastException("WTF?");
				}
				rootType.newInstance();
			} catch (Exception e) {
				/*
				 * We just ignore exceptions. The whole point of this is to do
				 * our best to have your base Root subclass loaded so its static
				 * block gets run.
				 */
				System.err.println("Warning: instantiating a " + rootType.getName() + " threw " + e);
			}
		}

		try {
			// port 0 means direct client access only.
			objectServer = Db4o.openServer(filename, 0);
		} catch (com.db4o.ext.DatabaseFileLockedException dfle) {
			/*
			 * very strange - this doesn't seem to get thrown if you
			 * openServer() twice from within the same process. It DOES get
			 * thrown if this process tries to access a database that another
			 * has opened.
			 */
			throw new IllegalStateException("Database locked");
		}
		if (objectServer == null) {
			throw new DebugException("Huh? openServer() failed to return an ObjectServer!");
		}

		poolAvailable = new LinkedList();
		poolInUse = Collections.synchronizedSet(new HashSet());
	}

	/**
	 * Get db4o {@link ObjectServer}. For most purposes the wrapper methods
	 * provided by this class should be used to access the database, but there's
	 * no overt reason to hide this, particularly for unit testing.
	 * 
	 * @return the server that this class wraps and provides client connections
	 *         to
	 */
	ObjectServer getUnderlyingServer() {
		return objectServer;
	}

	/**
	 * Get a client connection to the database. This client will be read+write,
	 * and will be pulled from the connection pool if there is an instance
	 * available there, otherwise newly opened.
	 * 
	 * @return a DataClient on this DataServer. In order to build up the
	 *         connection pool, do not not manually close the db4o
	 *         ObjectContainer that underlies this DataClient! Return it to the
	 *         DataServer with {@link #releaseClient(DataClient)}.
	 */
	/*
	 * Lots of guards here - but this is pretty fundamental, so it's worth
	 * taking the time to make sure things don't get out of whack.
	 */
	public DataClient gainClient() {
		DataClient client = null;
		synchronized (poolInUse) {
			if (poolAvailable.size() > 0) {
				Debug.print("pool", "Getting client from pool");
				client = (DataClient) poolAvailable.removeLast();
				if (client.getUnderlyingContainer().isClosed()) {
					throw new IllegalStateException("Client retrieved from pool is closed!");
				}
				if (client == null) {
					throw new DebugException(
						"How can it pull null as a DataClient from the available pool?");
				}
			} else {
				Debug.print("pool", "Opening new client");

				ObjectContainer container;
				try {
					container = objectServer.openClient();
				} catch (Exception e) {
					throw new DebugException("Calling db4o's openClient() threw " + e);
				}

				client = new DataClient(container);
				if (client == null) {
					throw new DebugException("How did you end up with a null new DataClient?");
				}
			}

			poolInUse.add(client);
		}

		poolStatus();

		return client;
	}

	/**
	 * Return a client to the connection pool. The client will be asked for its
	 * list of queried Objects and those objects will be have refresh() called
	 * on them before returning the client to the pool.
	 * 
	 * @param client
	 *            a DataClient that you are finished with. Notwithstanding the
	 *            measures described above, it should be in a pristine (ie no
	 *            uncommitted changes made) state.
	 */
	public void releaseClient(DataClient client) {
		synchronized (poolInUse) {
			if (!(poolInUse.contains(client))) {
				throw new IllegalStateException("This client not recorded as checked out!");
			}
			poolInUse.remove(client);

			ExtObjectContainer ext = client.getUnderlyingContainer();

			if (ext.isClosed()) {
				throw new DebugException("This client is closed, can't return it to connection pool!");
			}

			Set dS = client.getDirtyObjects();
			Iterator dI = dS.iterator();
			while (dI.hasNext()) {
				Object dirty = dI.next();
				ext.refresh(dirty, 5);
				dI.remove();
			}

			Debug.print("pool", "Returning client to pool");
			poolAvailable.addLast(client);
		}
		poolStatus();
	}

	/**
	 * Close this database server. <b>Will rollback any open clients!</b>
	 * <p>
	 * As with {@link DataClient#close()}, this is package visibility - the
	 * only place you should be calling this from is from
	 * {@link Engine#shutdown()}
	 */
	void close() {
		synchronized (poolInUse) {
			Iterator iter;

			if (poolInUse.size() > 0) {
				Debug.print("pool", "WARNING: There are still released connections!");
			}

			iter = poolInUse.iterator();
			while (iter.hasNext()) {
				DataClient client = (DataClient) iter.next();
				client.rollback();
				client.close();
				iter.remove();
			}

			iter = poolAvailable.iterator();
			while (iter.hasNext()) {
				DataClient client = (DataClient) iter.next();
				client.rollback();
				client.close();
				iter.remove();
			}
		}
		objectServer.close();

		poolStatus();

		/*
		 * Since the kinds of things that hold refereces to DataServer might be
		 * long lived, null out the reference to make things more obvious for
		 * the garbage collector.
		 */
		objectServer = null;
		poolInUse = null;
		poolAvailable = null;
	}

	/**
	 * Initiate a hot backup of the db4o database. Note that we do not permit
	 * overwrites.
	 * 
	 * @param path
	 *            a fully qualified pathname.
	 * 
	 * @throws IOException
	 * @see com.db4o.ext.ExtObjectServer#backup()
	 */
	void backup(String path) throws IllegalArgumentException, IOException {
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
		objectServer.ext().backup(path);
	}

	int sizeInUse() {
		synchronized (poolInUse) {
			return poolInUse.size();
		}
	}

	int sizeAvailable() {
		synchronized (poolInUse) {
			return poolAvailable.size();
		}
	}

	final void poolStatus() {
		synchronized (poolInUse) {
			Debug.print("pool", "status: " + poolInUse.size() + " in use, " + poolAvailable.size()
				+ " available");
		}
	}

}
