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
package generic.persistence;

import generic.util.DebugException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * A static class providing a global reentry point to manipulating an
 * accounting database file. Provides methods and holders to:
 * <ul>
 * <li>Create or open a database within a DataServer, holding it as a
 * reference as singleton to be used by the rest of the application.
 * <li>Get a reference to the primary DataClient connection to that is used
 * for read-only purposes.
 * <li>Execute a clean shutdown of the persistence system.
 * </ul>
 * The bulk of uses of this class will be of the form:
 * 
 * <pre>
 * Engine.openDatafile(&quot;blah.yap&quot;);
 * 
 * DataClient ro = Engine.primaryClient();
 * DataClient rw = Engine.gainClient();
 * // ...
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
 * interfere with the instance code in DataServer. True enough, but there is
 * the possiblity (on upgrade, perhaps?) that we will someday need to have
 * multiple databases open simultaneously so leave DataServer as the instance
 * wrapper around an ObjectServer, and use this class to hold the static
 * singleton references that the bulk of the UI will use directly.
 */
public final class Engine
{
    /**
     * The DataServer singleton for the database as opened by this
     * application.
     */
    public static DataServer server = null;

    private static DataClient primary = null;

    /**
     * Create a new datafile for use. A DataServer is opened on the file, and
     * a reference to it is cached staticly as {@link Engine#server}. Outside
     * of unit tests, this should probably get called exactly once. :)
     * 
     * @param filename
     * @param rootType
     *            The Class object representing the root of your hierarchy.
     *            Specify this if you have a static initalizer there that you
     *            which to ensure is run (ie, to configure object cascade
     *            properties) before your database is opened.
     * @throws IllegalArgumentException
     *             if the given filename already exists (no blotto!)
     * @throws IllegalStateException
     *             if the datastore is locked (eg by another running program
     *             you forgot about)
     */
    public static void newDatafile(String filename, Class rootType) {
        if (server != null) {
            throw new IllegalStateException("Engine is already representing an open datafile");
        }

        File probe = new File(filename);
        if (probe.exists()) {
            throw new IllegalStateException(
                    "Proposed datafile already exists (or at least, a file by that name does)");
        }

        server = new DataServer(filename, rootType);

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
     * @param rootType
     *            The Class object representing the root of your hierarchy.
     *            Specify this if you have a static initalizer there that you
     *            which to ensure is run (ie, to configure object cascade
     *            properties) before your database is openned.
     * @throws FileNotFoundException
     *             if the specified datafile is not found.
     */
    public static void openDatafile(String filename, Class rootType) throws FileNotFoundException {
        if (server != null) {
            throw new IllegalStateException("Engine is already representing an open datafile");
        }

        File probe = new File(filename);
        if (!probe.exists()) {
            throw new FileNotFoundException();
        }

        server = new DataServer(filename, rootType);

        /*
         * Integrity check
         */
        if (server == null) {
            throw new DebugException("got a null DataClient back from constructor!");
        }
    }

    /**
     * Get the primary <b>read only</b> client to the database. This is, if
     * you will, the zeroth client in the connection pool. It is here as a
     * special variation on the DataClient whereby only queries and activation
     * are permitted. This client can be shared by any use cases which are
     * displaying data - viewers, reports, etc.
     * 
     * @return the singleton primary read only DataClient to the database.
     */
    public static DataClient primaryClient() {
        if (primary == null) {
            /**
             * FUTURE: An alternative possility is to use the ObjectServer's
             * ObjectContainer, via this code:
             * 
             * <pre>
             * ObjectContainer internal = server.getUnderlyingServer().ext().objectContainer();
             * primary = new DataClient(internal);
             * </pre>
             * 
             * The risk, however, is that changes (especially refresh() calls)
             * against this Container will result in catestrophic race
             * conditions in the server itself. Probably not such a good idea.
             */
            primary = server.gainClient();
            primary.setReadOnly();
        }
        return primary;
    }

    /**
     * Get a client connection to the database. This client will be
     * read+write.
     * 
     * @return a DataClient opened on the static DataServer held by Enigne
     *         open for read-write use. Note that you must not manually close
     *         the db4o ObjectContainer that underlies this DataClient! Return
     *         it to the DataServer which Engine wraps with
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
     * Initiate a shutdown of the database. This closes and returns the
     * primary (read-only) client if it has been opened, and then asks the
     * static DataServer instance to close out all clients it is holding, and
     * then to close itself.
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
             * There shouldn't be any in flight set() as this is supposed to
             * be read only. Enforce.
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
     * Initiate a backup of the live database. For details about db4o's
     * capabilities in this regard, see
     * {@link com.db4o.ext.ExtObjectServer#backup(String)} (which we call
     * through DataServer's <code>backup()</code> method).
     * 
     * @param backupBaseFilename
     *            The fully qualified pathname used as the prefix for the
     *            backup file (ie, not just a directory, assumed not to end
     *            with a '/' character).
     * @return the full filename of the created backup.
     * @throws IOException
     * 
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
