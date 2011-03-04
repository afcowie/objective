/*
 * ObjectiveAccounts, accounting for small professional services firms.
 *
 * Copyright © 2011 Operational Dynamics Consulting, Pty Ltd
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
package objective.persistence;

import com.operationaldynamics.sqlite.Database;

public abstract class Operation
{
    protected final Database db;

    protected Operation(DataStore store) {
        db = store.gainConnection(this);
    }

    /**
     * Invoke when you're done with the Operation's Statements.
     */
    protected abstract void release();

    /**
     * Given an array of Strings, combine them into a single String. Use this
     * for constructing SQL statements.
     */
    protected static String combine(String[] sql) {
        StringBuilder buf;
        int len, i;

        len = 0;

        for (i = 0; i < sql.length; i++) {
            len += sql[i].length();
        }

        buf = new StringBuilder(len);

        for (i = 0; i < sql.length; i++) {
            if (i > 0) {
                buf.append(' ');
            }

            buf.append(sql[i]);
        }

        return buf.toString();
    }
}
