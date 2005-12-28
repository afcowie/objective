/*
 * EntryComparator.java
 * 
 * See LICENCE file for usage and redistribution terms
 * Copyright (c) 2005 Operational Dynamics
 */
package accounts.services;

import java.util.Comparator;
import java.util.Iterator;

import accounts.domain.Entry;

public class EntryComparator implements Comparator
{

	public int compare(Object o1, Object o2) {
		if ((!(o1 instanceof Entry)) || (!(o2 instanceof Entry))) {
			throw new ClassCastException("This comparator only orders Entry objects");
		}

		/*
		 * First sort by date
		 */
		Entry e1 = (Entry) o1;
		Entry e2 = (Entry) o2;

		long t1 = e1.getDate().getInternalTimestamp();
		long t2 = e2.getDate().getInternalTimestamp();

		if (t1 < t2) {
			return -1;
		} else if (t1 > t2) {
			return +1;
		} else {
			/*
			 * Then sort by Transaction identifier
			 */
			String id1 = e1.getParentTransaction().getIdentifier();
			String id2 = e2.getParentTransaction().getIdentifier();

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
				 * Then sort by Transaction description?
				 */
				String name1 = e1.getParentTransaction().getDescription();
				String name2 = e2.getParentTransaction().getDescription();

				int nameCmp;
				if ((name1 == null) || (name2 == null)) {
					// force it to ignore this comparison and move to the next
					nameCmp = 0;
				} else {
					nameCmp = name1.compareTo(name2);
				}

				if (nameCmp != 0) {
					return nameCmp;
				} else {
					/*
					 * Finally, sort by position in entries Set... assuming
					 * predictable iteration order of LinkedHashSet!
					 */
					Iterator iter1 = e1.getParentLedger().getEntries().iterator();
					int pos1 = 0;
					for (; iter1.hasNext(); pos1++) {
						if (o1 == iter1.next()) {
							break;
						}
					}

					Iterator iter2 = e2.getParentLedger().getEntries().iterator();
					int pos2 = 0;
					for (; iter2.hasNext(); pos2++) {
						if (o2 == iter2.next()) {
							break;
						}
					}

					if (pos1 < pos2) {
						return -1;
					} else if (pos1 > pos2) {
						return 1;
					} else {
						/*
						 * If they're the same position in the set, then they're
						 * the same object! So finally,
						 */
						return 0;
					}
				}
			}
		}
	}
}
