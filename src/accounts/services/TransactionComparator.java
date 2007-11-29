/*
 * TranasctionComparator.java
 * 
 * See LICENCE file for usage and redistribution terms
 * Copyright (c) 2005-2006 Operational Dynamics
 */
package accounts.services;

import java.util.Comparator;

import accounts.domain.Transaction;

/**
 * Impose ordering on two Transaction objects.
 * 
 * @author Andrew Cowie
 */
public class TransactionComparator implements Comparator
{
    public int compare(Object o1, Object o2) {
        if ((!(o1 instanceof Transaction)) || (!(o2 instanceof Transaction))) {
            throw new ClassCastException("This comparator only orders Entry objects");
        }

        Transaction t1 = (Transaction) o1;
        Transaction t2 = (Transaction) o2;

        long time1 = t1.getDate().getInternalTimestamp();
        long time2 = t2.getDate().getInternalTimestamp();

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
            String ref1 = t1.getReference();
            String ref2 = t2.getReference();

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
                int hash1 = t1.hashCode();
                int hash2 = t2.hashCode();

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
