/*
 * DomainObject.java
 * 
 * See LICENCE file for usage and redistribution terms
 * Copyright (c) 2006 Operational Dynamics
 */
package generic.domain;

import generic.persistence.NotActivatedException;

import com.db4o.ObjectContainer;

/**
 * Superclass of any domain objects stored in our DataStore. This is not
 * necessary - but if you want to do equals() over the wire, then the objects
 * you're comparing had better ulimately extend this class.
 * 
 * @author Andrew Cowie
 */
public abstract class DomainObject
{
    private long databaseId;

    public void objectOnActivate(ObjectContainer container) {
        if (databaseId == 0) {
            this.databaseId = container.ext().getID(this);
        }
    }

    public void objectOnNew(ObjectContainer container) {
        this.databaseId = container.ext().getID(this);
    }

    public long getID() {
        if (databaseId == 0) {
            throw new NotActivatedException();
        }
        return databaseId;
    }

    /**
     * Compare two domain objects by database ID, rather than referential
     * identity, as is used by Object.equals(). Use this in native queries
     * when you want to find out whether a member is equal to a parameter.
     */
    public boolean congruent(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj instanceof DomainObject) {
            DomainObject x = (DomainObject) obj;
            /*
             * Safety check: make sure both objects are actually persisted and
             * activated:
             */
            if ((this.databaseId == 0) || (x.databaseId == 0)) {
                return false;
            }
            /*
             * Now evaluate the objects:
             */
            if (this.databaseId == x.databaseId) {
                return true;
            } else {
                return false;
            }
        }
        return false;
    }

    /**
     * Return a String with the databaseId (if set) or "new".
     */
    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append("(");
        buf.append(databaseId);
        buf.append(")");
        return buf.toString();
    }
}
