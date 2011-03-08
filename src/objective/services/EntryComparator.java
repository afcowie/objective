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

import objective.domain.Account;
import objective.domain.Entry;
import objective.domain.Ledger;
import objective.domain.Transaction;

/**
 * Sort Entries. The constructor takes an argument by which you specify that
 * what context you're ordering within. For Entries within a Ledger, we
 * organize by the characteristics of the parentTransactions. But for Entries
 * within a Transaction, we organize by the characteristics of the
 * parentLedger and it's parentAccount.
 * 
 * @author Andrew Cowie
 * @see AccountComparator
 */
public class EntryComparator implements Comparator<Entry>
{
    private transient Object orderingWithin;

    private transient AccountComparator cachedAccountComparator;

    /**
     * @param orderingWithin
     *            the object type you're ordering within. The two known use
     *            cases are Ledger and Transaction; just pass (this) -
     *            EntryComparator uses instanceof to discriminate.
     */
    public EntryComparator(Object orderingWithin) {
        this.orderingWithin = orderingWithin;
    }

    public int compare(final Entry e1, final Entry e2) {
        final long time1, time2;
        final String ref1, ref2, name1, name2;
        final int refCmp, descCmp, hash1, hash2, aCmp, nameCmp;
        final Account a1, a2;

        if (orderingWithin instanceof Ledger) {
            /*
             * First off, sort by date.
             */
            time1 = e1.getParentTransaction().getDate().getInternalTimestamp();
            time2 = e2.getParentTransaction().getDate().getInternalTimestamp();

            if (time1 < time2) {
                return -1;
            } else if (time1 > time2) {
                return +1;
            } else {
                /*
                 * ... we're ordering within a Ledger listing. So first sort
                 * by Transaction reference
                 */
                ref1 = e1.getParentTransaction().getReference();
                ref2 = e2.getParentTransaction().getReference();

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
                     * Then sort by Transaction description?
                     */
                    String desc1 = e1.getParentTransaction().getDescription();
                    String desc2 = e2.getParentTransaction().getDescription();

                    if ((desc1 == null) || (desc2 == null)) {
                        // force it to ignore this comparison and move to the
                        // next
                        descCmp = 0;
                    } else {
                        descCmp = desc1.compareTo(desc2);
                    }

                    if (descCmp != 0) {
                        return descCmp;
                    } else {
                        /*
                         * In normal use, if you're in the same Ledger and in
                         * the same position in the set, then they're the same
                         * object! However, unit tests can bust this, so
                         * finally, order by hash
                         */
                        hash1 = e1.hashCode();
                        hash2 = e2.hashCode();

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
        } else if (orderingWithin instanceof Transaction) {
            /*
             * ... we're ordering within a Transaction listing. Each
             * Transaction's Entries all have the same date, so So first sort
             * by Account
             */
            a1 = e1.getParentLedger().getParentAccount();
            a2 = e2.getParentLedger().getParentAccount();
            if (cachedAccountComparator == null) {
                cachedAccountComparator = new AccountComparator();
            }
            aCmp = cachedAccountComparator.compare(a1, a2);

            if (aCmp != 0) {
                return aCmp;
            } else {
                /*
                 * If it's the same account, then try sorting by the Ledger
                 * names within
                 */
                name1 = e1.getParentLedger().getName();
                name2 = e2.getParentLedger().getName();

                if ((name1 == null) || (name2 == null)) {
                    // force it to ignore this comparison and move to the
                    // next
                    nameCmp = 0;
                } else {
                    nameCmp = name1.compareTo(name2);
                }

                if (nameCmp != 0) {
                    return nameCmp;
                } else {
                    /*
                     * If they're the same position in the Ledger entries Set,
                     * then they're the same object! But there are unit test
                     * cases (even if not normal use ones) where you can end
                     * up comparing Entries so similar this breaks. So
                     * finally, use hashes
                     */
                    hash1 = e1.hashCode();
                    hash2 = e2.hashCode();

                    if (hash1 < hash2) {
                        return -1;
                    } else if (hash1 > hash2) {
                        return +1;
                    } else {
                        return 0;
                    }
                }
            }
        } else {
            throw new AssertionError();
        }
    }
}
