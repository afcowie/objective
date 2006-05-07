/*
 * Root.java
 * 
 * See LICENCE file for usage and redistribution terms
 * Copyright (c) 2006 Operational Dynamics
 * 
 * Original per class turning code was in generic.persistence.DataServer,
 * formerly accounts.persistence.DataStore
 * Copyright (c) 2005-2006 Operational Dynamics
 */
package generic.domain;

import com.db4o.Db4o;
import com.db4o.config.Configuration;
import com.db4o.config.ObjectClass;

/**
 * The root of our domain classes. While there is no requirement whatsoever that
 * persistent classes in db4o extend some common ancestor class, we define a
 * singleton Root object to be the top of the object hierarchy of whatever
 * system we are persisting.
 * <p>
 * Unlike the utility classes and wrappers in generic.persistence, this class
 * <i>is</i> stored to the database. The reason to have such a generic root
 * class, over and above making the generic.* packages independent of
 * accounts.domain.*, is to have a place to manage a global database version and
 * thus protect against unexpected upgrades.
 * 
 * @author Andrew Cowie
 * @see accounts.domain.Books The root object of an Accounting Database.
 */
public abstract class Root
{
	/**
	 * Mark the specified class as one to which cascade behaviour is to apply.
	 * By turning cascade on (particularly for the Collection classes included
	 * in the array above) we create the magic that any time one of them is
	 * activated it will start a new {update,activate} through depth.
	 * <p>
	 * Most of the classes you will pass to this static contain a Set of
	 * subelements. We make sure it is {updated,activated} through it's own
	 * internal members (usually 2 or so deep) to reach the elements themselves.
	 */
	protected static final void markCascade(Class cascade) {
		Configuration config = Db4o.configure();

		ObjectClass db4oObjectClass = config.objectClass(cascade);

		db4oObjectClass.cascadeOnActivate(true);
		db4oObjectClass.cascadeOnUpdate(true);
		db4oObjectClass.minimumActivationDepth(5);
		db4oObjectClass.updateDepth(5);
	}

	/**
	 * Mark the specified class as one which is a leaf node, presumably
	 * containing only primative fields, so we can stop seeking further when
	 * these are hit or activated.
	 */
	protected static final void markLeaf(Class leaf) {
		Configuration config = Db4o.configure();

		ObjectClass db4oObjectClass = config.objectClass(leaf);

		db4oObjectClass.cascadeOnActivate(false);
		db4oObjectClass.cascadeOnUpdate(false);
		db4oObjectClass.minimumActivationDepth(0);
		db4oObjectClass.updateDepth(0);
	}

}
