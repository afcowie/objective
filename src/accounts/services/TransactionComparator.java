/*
 * TranasctionComparator.java
 * 
 * See LICENCE file for usage and redistribution terms
 * Copyright (c) 2005 Operational Dynamics
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
			 * if they're the same date, then order by identifier. There really
			 * isn't anything special about identifier (remember, it is not a
			 * unique or primary key) but if they're present on the same day
			 * they should probably be in order (ie cheque numbers, etc)
			 */
			String id1 = t1.getIdentifier();
			String id2 = t2.getIdentifier();

			int idCmp;
			if ((id1 == null) || (id2 == null)) {
				// force it to ignore this comparison and move to the next
				idCmp = 0;
			} else {
				// String implements Comparable; use it
				idCmp = id1.compareTo(id2);
			}

			if (idCmp != 0) {
				return idCmp;
			} else {
				/*
				 * If they're the same date and identifier, then it doesn't
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
