/*
 * Db4oSet.java
 * 
 * See LICENCE file for usage and redistribution terms
 * Copyright (c) 2005 Operational Dynamics
 */
package accounts.persistence;

import java.util.AbstractSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.Set;

import com.db4o.ObjectContainer;
import com.db4o.types.Db4oMap;

/**
 * For some reason, db4o doesn't provide a database backed set in
 * Db4oCollections. So we implement one. Alternate name: StoredSet
 * 
 * We use a Db4oMap retreived with newHashMap() from Db4oCollections. Our
 * elements are stored in the key part of the map.
 * 
 * @author Andrew Cowie
 * @deprecated
 */
public class Db4oSet extends AbstractSet
{
	/*
	 * Instance variables ---------------------------------
	 */

	private Db4oMap	_map	= null;

	/**
	 * This is an internal no-argument constuctor so that Db4o can store and
	 * re-instantiate these!
	 */
	private Db4oSet() {
	}

	/**
	 * A new database-backed set. This constructor has package visability, as
	 * we're only supposed to be getting these via the factory method in
	 * DataStore.
	 * <P>
	 * We use an IdentifyHashMap so that equality is reference equality, not
	 * just Object.equals(). This seems to fit better with the whole Db4o "just
	 * use references" thing. See {@link IdentityHashMap}.
	 * 
	 * @param container
	 *            An open ObjectContainer pointing to a Db4o database.
	 */
	Db4oSet(ObjectContainer container) {
//		_map = container.ext().collections().newIdentityHashMap(0);
		_map = container.ext().collections().newHashMap(0);
	}

	/**
	 * 
	 * @see java.util.Collection#size()
	 */
	public int size() {
		return _map.size();
	}

	/**
	 * @param o
	 *            element to be added to this set.
	 * @return true if this set did not already contain the specified element.
	 * @throws NullPointerException
	 *             if you try to add null to the Set
	 */
	public boolean add(Object o) throws NullPointerException {
		if (o == null) {
			throw new NullPointerException("can't add null to our Db4oSet");
		}
		if (_map.containsKey(o)) {
			return false;
		} else {
			// _container.set(o); // HERE TODO FIXME is this right? An identity
			// hash map in Db4o land's key has to be already
			// stored...
			_map.put(o, new Object());
			return true;
		}
	}

	/**
	 * Remove the specified object from the set. Note that this does not
	 * automatically imply that the Object will be deleted from the underlying
	 * datastore. Do that yourself if that's what you actually want.
	 * 
	 * @param o
	 *            object to be removed from this set, if present.
	 * @return true if the set contained the specified element.
	 * @throws NullPointerException
	 *             if the specified element is null
	 */
	public boolean remove(Object o) throws NullPointerException {
		if (_map.remove(o) == null) {
			return false;
		} else {
			return true;
		}
	}

	/**
	 * 
	 * @see java.util.Collection#iterator()
	 * @see IdentityHashMap.html#keySet()
	 */
	public Iterator iterator() {
		Set keys = _map.keySet();
		return keys.iterator();
	}
}