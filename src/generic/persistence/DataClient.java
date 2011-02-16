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
 * contacted through http://research.operationaldynamics.com/projects/objective/.
 */
package generic.persistence;

import generic.domain.DomainObject;
import generic.domain.Root;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

import com.db4o.ObjectContainer;
import com.db4o.ObjectSet;
import com.db4o.events.Event4;
import com.db4o.events.EventArgs;
import com.db4o.events.EventListener4;
import com.db4o.events.EventRegistry;
import com.db4o.events.EventRegistryFactory;
import com.db4o.events.ObjectEventArgs;
import com.db4o.ext.ExtObjectContainer;
import com.db4o.ext.ObjectInfo;

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
    private transient ExtObjectContainer container = null;

    private transient boolean readOnly = false;

    private transient Root root = null;

    private transient Set dirty = null;

    private transient EventListener4 listener = null;

    /**
     * Create a new DataClient instance around a {@link ObjectContainer}.
     */
    DataClient(final ObjectContainer container) throws IllegalStateException {
        final EventRegistry registry;

        if (container == null) {
            throw new IllegalArgumentException(
                    "A bit hard to be instantiating a client with a null ObjectContainer");
        }
        this.container = container.ext();

        this.listener = new EventListener4() {
            public void onEvent(Event4 event, EventArgs args) {
                final Object obj;
                final DomainObject dom;
                final ObjectInfo info;
                final long id;

                obj = ((ObjectEventArgs) args).object();

                if (obj instanceof DomainObject) {
                    dom = (DomainObject) obj;
                    info = container.ext().getObjectInfo(obj);
                    if (info != null) {
                        id = info.getInternalID();
                        dom.setID(id);
                    }
                }
            }
        };

        registry = EventRegistryFactory.forObjectContainer(container);

        registry.created().addListener(listener);
        registry.activated().addListener(listener);

        dirty = new LinkedHashSet();
    }

    /**
     * Mark this DataClient as read-only. This is enforced here by preventing
     * {@link #save(Object)} and {@link #commit()}.
     */
    /*
     * Enforced here rather than using db4o's read-only configuration option
     * as apparently that mode inhibits inter client communication and updates
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
        /*
         * WARNING: By brute force testing, this yield() seems critical to
         * force the Gtk main loop to take a break and allow the database
         * threads to do their thing. When this doesn't happen, the main
         * thread can get to UserInterface.propegateUpdate() before a commit
         * has propegated and is available to a db4o refresh() under
         * READ_COMMITTED isolation.
         */
        try {
            Thread.sleep(50);
        } catch (InterruptedException ie) {
            Thread.yield();
        }
    }

    /**
     * Rollback the current transaction (implicitly, db4o starts a new one for
     * you per its practice of there always being an open transaction). Of
     * course, this only reverts changes in flight due to previous save()
     * calls; any objects which you have dirtied which you still hold
     * references to must be refreshed with {@link #reload(Object)}.
     */
    public void rollback() {
        container.rollback();
    }

    /**
     * Reload (refresh) an individual object from the database, typically
     * after a rollback. Per db4o's JavaDoc, after a rollback changed objects
     * should be restored with a deactivate() call followed by an activate()
     * call; this method does this.
     */
    public void reload(Object obj) {
        // container.deactivate(obj, 6);
        container.refresh(obj, 6);
        // container.activate(obj, 6);
    }

    /**
     * Close this connection to the data store. <b>Danger! db4o has a default
     * commit-on-close behaviour!</b> So this method has package visibility,
     * and the only thing that should be calling this is
     * {@link DataServer#close()} which rigourously calls rollback()
     * immediately before hand.
     */
    void close() {
        container.close();

        /*
         * Once closed we aren't reoppening this one. It's still a long way
         * down the graph, but closing is preparatory to finalization and
         * garbage collection, so help nudge things along.
         */
        container = null;
    }

    /**
     * Wraps
     * {@link ObjectContainer#set(java.lang.Object) ObjectContainer.set()}. I
     * prefer Hibernate's term for this, so "save" it is.
     */
    public void save(Object obj) {
        if (readOnly) {
            throw new UnsupportedOperationException("Can't save() through a read-only client!");
        }
        if (container.isClosed()) {
            throw new IllegalStateException("You can't save() if the container is closed!");
        }
        try {
            container.set(obj);
            dirty.add(obj);
        } catch (Exception e) {
            System.err.println("FIXME! Uncaught exception when trying to set()");
            e.printStackTrace();
            System.err.println("FIXME! Continuing...");
        }
    }

    /**
     * Get db4o {@link ObjectContainer}. For most purposes, the wrapper
     * methods provided by this class should be used to access the datastore.
     * But there's no overt reason to hide this, particularly for unit
     * testing.
     * 
     * @return The {@link ExtObjectContainer} that this class wraps.
     */
    ExtObjectContainer getUnderlyingContainer() {
        return container;
    }

    /**
     * Get the root object, and in doing so verify the version of the
     * datafile. Note that DataClient caches this lookup once performed.
     */
    /*
     * The use of the passed Class object ensures that the application
     * specific activation and cascade settings will apply when (ie
     * Books.class) is queried, whereas (Root.class) would not.
     */
    public Root getRoot() {
        if (root == null) {
            ObjectSet<Root> os = container.get(Root.class);

            if (os.size() > 1) {
                throw new IllegalStateException(
                        "Whoa. You managed to get more than one Books object into the database. That's really bad.");
            } else if (os.size() == 0) {
                throw new NoSuchElementException("No Books object in this container!");
            } else {
                this.root = os.next();
            }
        }
        return root;
    }

    /**
     * Set the cached Books object. Internal use only; does not commit to
     * database. To be used by InitBooksCommand to allow chaining; regardless
     * of Command using this, a commit of the Books object to the database
     * needs to be done via a Command.
     * 
     * @param root
     *            the Root object to be cached in this DataClient object
     *            preparatory to you persisting it.
     */
    public void setRoot(Root root) {
        if (this.root == null) {
            if (root != null) {
                this.root = root;
            } else {
                throw new IllegalArgumentException(
                        "Can't set a null Root object to DataClient's internally cached root reference.");
            }
        } else {
            throw new UnsupportedOperationException(
                    "You aren't supposed to call setRoot unless initializing a DataClient via InitBooksCommand");
        }
    }

    /**
     * Expose the ability to query by example. Most persistence engines (and,
     * in the present instance, the implemented db4o database) provide the
     * ability to get objects from the database by specifying a partially
     * instantiated object prototype which is used to constrain the query.
     * This call wraps the underlying db4o implementation and returns a Java
     * List instead of db4o's ObjectSet.
     * 
     * @param example
     *            the example object whose fields provide the prototype which
     *            constrains the returned collection of objects. If a class
     *            literal is provided (ie Ledger.class) then all Ledger
     *            objects persisted in the database will be retrieved.
     * @return a List of the objects retreieved from the database. If no
     *         objects are fetched, then the list will not be null but will
     *         have size 0. The List is actually a db4o ObjectSet (implements
     *         List) which does lazy but automatic instantiation as you
     *         iterate over it.
     * @see com.db4o.ObjectSet
     */
    public List queryByExample(Object example) {
        ObjectSet os = container.get(example);

        activateAndDirty(os);
        return os;
    }

    private void activateAndDirty(ObjectSet os) {
        Iterator iter = os.iterator();
        while (os.hasNext()) {
            Object obj = os.next();
            container.activate(obj, 5);

            // if (obj instanceof Cascade) {
            // Cascade cascade = (Cascade) obj; ...

            dirty.add(obj);
        }
    }

    /**
     * Expose the "native query" interface provided by db4o. The predicate
     * argument is an (typically anonymous) Selector with a match() method.
     * For example,
     * 
     * <pre>
     * List result = nativeQuery(new Selector() {
     *     public boolean match(Type t) {
     *     // something with t
     *     }
     * });
     * </pre>
     * 
     * @param predicate
     *            a Selector implementing a match(SomeObject) method which
     *            returns true if SomeObject is to be included in the result
     *            set.
     * @return a List of the objects retreieved from the database. If no
     *         objects are fetched, then the list will not be null but will
     *         have size 0. The List is actually a db4o ObjectSet (implements
     *         List) which does lazy but automatic instantiation as you
     *         iterate over it.
     * @see Selector
     * @see com.db4o.query.Predicate
     * @see com.db4o.ObjectSet
     */
    public List nativeQuery(Selector predicate) {
        ObjectSet os = container.query(predicate);

        activateAndDirty(os);
        return os;
    }

    /**
     * Retrieve a copy of the object as it is committed to the database.
     * 
     * @param original
     *            The object whose persisted value you want to peek.
     * @return a transient object representing the currently committed value
     *         of the object; this object is <b>disconnected</b> from the
     *         datastore, activated with depth 2.
     * @see com.db4o.ext.ExtObjectContainer#peekPersisted(Object, int,
     *      boolean)
     */
    public Object peek(Object original) throws NoSuchElementException {
        Object aboo = container.peekPersisted(original, 2, true);

        if (aboo == null) {
            throw new NoSuchElementException("No committed version of " + original + " persisted");
        }
        return aboo;
    }

    /**
     * The default db4o behaviour is that deletes are NOT recursive (cascading
     * in their parleance). You can change that on a per class or per field
     * basis by making settings in the static block of {@link DataServer} or
     * {@link Root}.
     * 
     * @param target
     *            the Object to be deleted from the database. Only target will
     *            be removed, not any of the objects that comprise its fields.
     */
    public void delete(Object target) {
        container.delete(target);
    }

    /**
     * @return the Set of dirty objects being those that resulted from
     *         explicit queries.
     * @see DataServer#releaseClient(DataClient) for the method that uses this
     *      to refresh these objects before returning a DataClient to the
     *      connection pool.
     */
    Set getDirtyObjects() {
        return dirty;
    }

    /**
     * Get the long ID used internally by db4o to represent the ID of obj as
     * persisted. You can use this ID to have another DataClient quickly fetch
     * up this object.
     * 
     * @throws IllegalStateException
     *             if the object you asked for isn't stored in this container.
     *             That's probably just bad form, but could conceivably
     *             represnt a state where you deleted something but still have
     *             a reference to it.
     * @see #fetchByID(long)
     */
    public long getID(Object obj) {
        if (obj == null) {
            throw new IllegalArgumentException();
        }
        long id = container.getID(obj);
        if (id == 0) {
            throw new IllegalStateException();
        }
        return id;
    }

    /**
     * Query the database for a specific object by ID. Within the context of a
     * single ObjectContainer, the getByID() look up returning whatever is in
     * that container's cache (without further activating) is fine. In our
     * usage, where we use IDs in the update notifications between clients,
     * there is certainly no likelihood that that Container has seen the
     * object already. So we activate it here before returning.
     * 
     * @param id
     *            the long object ID for the object you're fetching.
     * @throws IllegalStateException
     *             if the ID you've requested is not present in this
     *             container. The only time you should be asking for an object
     *             by ID is if you were given that ID by another DataClient;
     *             to have it reported that an this ID doesn't represent an
     *             Object in the container is bad indeed.
     */
    public Object fetchByID(long id) {
        if (id == 0) {
            throw new IllegalArgumentException("Can't fetch ID 0");
        }
        Object target = container.getByID(id);
        if (target == null) {
            throw new IllegalStateException("Object of that ID not present");
        }
        container.activate(target, 5);
        dirty.add(target);
        return target;
    }

    /**
     * Allow {@link DataServer} to tell this Client to discard any cached
     * references it might be carrying. [This is at user level, not within
     * db4o]
     */
    /*
     * TODO needs serious tuning improvement.
     */
    void clearCachedReferences() {
        // container.deactivate(root,100);
        root = null;
        dirty.clear();
    }
}
