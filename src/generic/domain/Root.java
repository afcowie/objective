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
package generic.domain;

import com.db4o.Db4o;
import com.db4o.config.Configuration;
import com.db4o.config.ObjectClass;

/**
 * The root of our domain classes. While there is no requirement whatsoever
 * that persistent classes in db4o extend some common ancestor class, we
 * define a singleton Root object to be the top of the object hierarchy of
 * whatever system we are persisting.
 * <p>
 * Unlike the utility classes and wrappers in generic.persistence, this class
 * <i>is</i> stored to the database. The reason to have such a generic root
 * class, over and above making the generic.* packages independent of
 * accounts.domain.*, is to have a place to manage a global database version
 * and thus protect against unexpected upgrades.
 * 
 * @author Andrew Cowie
 * @see accounts.domain.Books The root object of an Accounting Database.
 */
public abstract class Root
{
    static {
        markCascade(Cascade.class);
        markNormal(Normal.class);
        markLeaf(Leaf.class);
    }

    /**
     * Mark the specified class as one to which cascade behaviour is to apply.
     * By turning cascade on (particularly for the Collection classes included
     * in the array above) we create the magic that any time one of them is
     * activated it will start a new {update,activate} through depth.
     * <p>
     * Most of the classes you will pass to this static contain a Set of
     * subelements. We make sure it is {updated,activated} through it's own
     * internal members (usually 2 or so deep) to reach the elements
     * themselves.
     */
    protected static final void markCascade(Class cascade) {
        Configuration config = Db4o.configure();

        ObjectClass db4oObjectClass = config.objectClass(cascade);

        db4oObjectClass.cascadeOnActivate(true);
        db4oObjectClass.cascadeOnUpdate(true);
        db4oObjectClass.minimumActivationDepth(6);
        db4oObjectClass.updateDepth(6);
    }

    protected static final void markNormal(Class normal) {
        Configuration config = Db4o.configure();

        ObjectClass db4oObjectClass = config.objectClass(normal);

        db4oObjectClass.cascadeOnActivate(true);
        db4oObjectClass.cascadeOnUpdate(true);
        db4oObjectClass.minimumActivationDepth(2);
        db4oObjectClass.updateDepth(1);
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
        db4oObjectClass.minimumActivationDepth(1);
        db4oObjectClass.updateDepth(1);
    }

    public abstract void configure();

}
