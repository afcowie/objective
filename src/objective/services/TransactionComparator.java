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
package objective.services;

import java.util.Comparator;

import objective.domain.Transaction;

/**
 * Impose ordering on two Transaction objects.
 * 
 * @author Andrew Cowie
 */
public class TransactionComparator implements Comparator<Transaction>
{
    public int compare(Transaction t1, Transaction t2) {
        final long time1, time2;
        final String ref1, ref2;
        final int hash1, hash2;

        time1 = t1.getDate();
        time2 = t2.getDate();

        if (time1 < time2) {
            return -1;
        } else if (time1 > time2) {
            return +1;
        } else {
            /*
             * if they're the same date, then order by reference. There really
             * isn't anything special about reference (remember, it is not a
             * unique or primary key) but if they're present on the same day
             * they should probably be in order (ie cheque numbers, etc)
             */
            ref1 = t1.getReference();
            ref2 = t2.getReference();

            int refCmp;
            if ((ref1 == null) || (ref2 == null)) {
                // force it to ignore this comparison and move to the next
                refCmp = 0;
            } else {
                // String implements Comparable; use it
                refCmp = ref1.compareTo(ref2);
            }

            if (refCmp != 0) {
                return refCmp;
            } else {
                /*
                 * If they're the same date and reference, then it doesn't
                 * matter any further, so just order by hash
                 */
                hash1 = t1.hashCode();
                hash2 = t2.hashCode();

                if (hash1 < hash2) {
                    return -1;
                } else if (hash1 > hash2) {
                    return +1;
                } else {
                    return 0;
                }
            }
        }
    }
}
