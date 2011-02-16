/*
 * ObjectiveAccounts, accounting for small professional services firms.
 *
 * Copyright © 2006-2011 Operational Dynamics Consulting, Pty Ltd
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

import generic.persistence.NotActivatedException;

/**
 * Superclass of any domain objects stored in our DataStore. This is not
 * necessary - but if you want to do equals() over the wire, then the objects
 * you're comparing had better ulimately extend this class.
 * 
 * @author Andrew Cowie
 */
public abstract class DomainObject
{
    // private static long count;

    private long databaseId;

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

    public void setID(long id) {
        this.databaseId = id;
    }
}
