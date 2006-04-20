/*
 * Engine.java
 * 
 * See LICENCE file for usage and redistribution terms
 * Copyright (c) 2006 Operational Dynamics
 * 
 * Based on code originally in accounts.services.DatafileServices
 * Copyright (c) 2005-2006 Operational Dynamics
 */
package accounts.persistence;

import generic.util.DebugException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * A static class providing a global reentry point to manipulating an accounting
 * database file. Provides methods and holders to:
 * <ul>
 * <li>Create or open a database within a DataServer, holding it as a reference
 * as singleton to be used by the rest of the application.
 * <li>Get a reference to the primary DataClient connection to that is used for
 * read-only purposes.
 * <li>Execute a clean shutdown of the persistence system.
 * </ul>
 * The bulk of uses of this class will be of the form:
 * 
 * <pre>
 * Engine.openDatafile(&quot;blah.yap&quot;);
 * 
 * DataClient ro = Engine.primaryClient();
 * DataClient rw = Engine.gainClient();
 * Engine.releaseClient(rw);
 * 
 * Engine.shutdown();
 * </pre>
 * 
 * The primary reference can be fetched out as often as you want. No need to
 * return it.
 * 
 * Providing gainClient() and releaseClient() is somewhat of a duplication,
 * since these merely pass through to DataServer, but it makes the singleton
 * static API a little easier to use. That Engine wraps a DataServer is
 * transparent, other than your having to ensure you shutdown safely.
 * 
 * @author Andrew Cowie
 */
/*
 * This class is separate from the accounts.client.ObjectiveAccounts primary
 * program client to provide better reuse and reduce clutter. A much more
 * tempting fit would be to merge this classes behaviour into
 * accounts.persistence.DataServer; after all this is all static and would not
 * interfere with the instance code in DataServer. True enough, but there is the
 * possiblity (on upgrade, perhaps?) that we will someday need to have multiple
 * databases open simultaneously so leave DataServer as the instance wrapper
 * around an ObjectServer, and use this class to hold the static singleton
 * references that the bulk of the UI will use directly.
 */
public final class Engine
{
	/**
	 * The DataServer singleton for the database as opened by this application.
	 */
	static DataServer			server	= null;

	private static DataClient	primary	= null;

	/**
	 * Create a new datafile for use. A DataServer is opened on the file, and a
	 * reference to it is cached staticly as {@link Engine#server}. Outside of
	 * unit tests, this should probably get called exactly once. :)
	 * 
	 * @param filename
	 * @throws IllegalArgumentException
	 *             if the given filename already exists (no blotto!)
	 * @throws IllegalStateException
	 *             if the datastore is locked (eg by another running program you
	 *             forgot about)
	 */
	public static void newDatafile(String filename) {
		if (server != null) {
			throw new IllegalStateException("Engine is already representing an open datafile");
		}

		File probe = new File(filename);
		if (probe.exists()) {
			throw new IllegalStateException("Proposed datafile already exists (or at least, a file by that name does)");
		}

		server = new DataServer(filename);

		/*
		 * Integrity check
		 */
		if (server == null) {
			throw new DebugException("got a null DataServer back, should have been a brand new one!");
		}
	}

	/**
	 * Open a {@link DataServer} on an existing datafile, caching it in
	 * {@link #server} for the other class methods of Engine to operate on.
	 * 
	 * @param filename
	 *            the path to the database you wish to open.
	 * @throws FileNotFoundException
	 *             if the specified datafile is not found.
	 */
	public static void openDatafile(String filename) throws FileNotFoundException {
		if (server != null) {
			throw new IllegalStateException("Engine is already representing an open datafile");
		}

		File probe = new File(filename);
		if (!probe.exists()) {
			throw new FileNotFoundException();
		}

		server = new DataServer(filename);

		/*
		 * Integrity check
		 */
		if (server == null) {
			throw new DebugException("got a null DataClient back from constructor!");
		}
	}

	/**
	 * Get the primary <b>read only</b> client to the database. This is, if you
	 * will, the zeroth client in the connection pool. It is here as a special
	 * variation on the DataClient whereby only queries and activation are
	 * permitted. This client can be shared by any use cases which are
	 * displaying data - viewers, reports, etc.
	 * 
	 * @return the singleton primary read only DataClient to the database.
	 */
	public static DataClient primaryClient() {
		if (primary == null) {
			primary = server.gainClient();
			primary.setReadOnly();
		}
		return primary;
	}

	/**
	 * Get a client connection to the database. This client will be read+write.
	 * 
	 * @return a DataClient opened on the static DataServer held by Enigne open
	 *         for read-write use. Note that you must not manually close the
	 *         db4o ObjectContainer that underlies this DataClient! Return it to
	 *         the DataServer which Engine wraps with
	 *         {@link #releaseClient(DataClient)}.
	 * @see DataServer#gainClient() which this wraps.
	 */
	public static DataClient gainClient() {
		if (server == null) {
			throw new IllegalStateException("Engine is not open");
		}
		return server.gainClient();
	}

	/**
	 * Return a client to the connection pool inside Engine's singleton
	 * DataServer.
	 * 
	 * @param client
	 *            a DataClient that you are finished with. It should be in a
	 *            pristine state, meaning no changes have been to the objects
	 *            fetched from it, and certainly that there are no save()'d
	 *            objects awaiting commit().
	 */
	public static void releaseClient(DataClient client) {
		if (server == null) {
			throw new IllegalStateException("Engine is not open");
		}
		if (client == primary) {
			throw new IllegalArgumentException(
				"There's no need to return the primary client to the connection pool. Just use Engine.shutdown() when you're done.");
		}
		server.releaseClient(client);
	}

	/**
	 * Initiate a shutdown of the database. This closes and returns the primary
	 * (read-only) client if it has been opened, and then asks the static
	 * DataServer instance to close out all clients it is holding, and then to
	 * close itself.
	 */
	/*
	 * TODO implement as a Hooks somewhere?
	 */
	public static void shutdown() {
		if (server == null) {
			throw new IllegalStateException("Engine is not open");
		}
		if (primary != null) {
			/*
			 * There shouldn't be any in flight set() as this is supposed to be
			 * read only. Enforce.
			 */
			primary.rollback();
			/*
			 * And let the DataServer dispose of it.
			 */
			server.releaseClient(primary);
			primary = null;
		}

		server.close();
		server = null;
	}

	/**
	 * Initiate a backup of the live database.
	 * 
	 * @param store
	 * @param backupBaseFilename
	 *            The fully qualified pathname used as the prefix for the backup
	 *            file (ie, not just a directory, assumed not to end with a '/'
	 *            character).
	 * @return the full filename of the created backup.
	 * @throws IOException
	 * @see DataServer#backup(String)
	 */
	public static String backupToFile(String backupBaseFilename) throws IOException {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd-HHmmss");
		String timestamp = sdf.format(new Date());

		String toFilename = backupBaseFilename + "_backup-" + timestamp;

		/*
		 * Do the backup.
		 */
		server.backup(toFilename);

		return toFilename;
	}
}