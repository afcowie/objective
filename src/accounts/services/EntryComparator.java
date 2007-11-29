/*
 * EntryComparator.java
 * 
 * See LICENCE file for usage and redistribution terms
 * Copyright (c) 2005-2006 Operational Dynamics
 */
package accounts.services;

import generic.util.DebugException;

import java.util.Comparator;
import java.util.Iterator;

import accounts.domain.Account;
import accounts.domain.Entry;
import accounts.domain.Ledger;
import accounts.domain.Transaction;

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
public class EntryComparator implements Comparator
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

    public int compare(Object o1, Object o2) {
        if ((!(o1 instanceof Entry)) || (!(o2 instanceof Entry))) {
            throw new ClassCastException("This comparator only orders Entry objects");
        }

        Entry e1 = (Entry) o1;
        Entry e2 = (Entry) o2;

        if (orderingWithin instanceof Ledger) {
            /*
             * First off, sort by date.
             */
            long time1 = e1.getDate().getInternalTimestamp();
            long time2 = e2.getDate().getInternalTimestamp();

            if (time1 < time2) {
                return -1;
            } else if (time1 > time2) {
                return +1;
            } else {
                /*
                 * ... we're ordering within a Ledger listing. So first sort
                 * by Transaction reference
                 */
                String ref1 = e1.getParentTransaction().getReference();
                String ref2 = e2.getParentTransaction().getReference();

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
                     * Then sort by Transaction description?
                     */
                    String desc1 = e1.getParentTransaction().getDescription();
                    String desc2 = e2.getParentTransaction().getDescription();

                    int descCmp;
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
                         * Finally, sort by position in entries Set...
                         * assuming predictable iteration order of
                         * LinkedHashSet!
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
                             * In normal use, if you're in the same Ledger and
                             * in the same position in the set, then they're
                             * the same object! However, unit tests can bust
                             * this, so finally, order by hash
                             */
                            int hash1 = e1.hashCode();
                            int hash2 = e2.hashCode();

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
        } else if (orderingWithin instanceof Transaction) {
            /*
             * ... we're ordering within a Transaction listing. Each
             * Transaction's Entries all have the same date, so So first sort
             * by Account
             */
            Account a1 = e1.getParentLedger().getParentAccount();
            Account a2 = e2.getParentLedger().getParentAccount();
            if (cachedAccountComparator == null) {
                cachedAccountComparator = new AccountComparator();
            }
            int aCmp = cachedAccountComparator.compare(a1, a2);

            if (aCmp != 0) {
                return aCmp;
            } else {
                /*
                 * If it's the same account, then try sorting by the Ledger
                 * names within
                 */
                String name1 = e1.getParentLedger().getName();
                String name2 = e2.getParentLedger().getName();

                int nameCmp;
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
                     * Finally, sort by position in the transaction's entries
                     * Set... relies on predictable iteration order of backing
                     * LinkedHashSet!
                     */
                    Iterator iter1 = e1.getParentTransaction().getEntries().iterator();
                    int pos1 = 0;
                    for (; iter1.hasNext(); pos1++) {
                        if (o1 == iter1.next()) {
                            break;
                        }
                    }

                    Iterator iter2 = e2.getParentTransaction().getEntries().iterator();
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
                         * If they're the same position in the Ledger entries
                         * Set, then they're the same object! But there are
                         * unit test cases (even if not normal use ones) where
                         * you can end up comparing Entries so similar this
                         * breaks. So finally, use hashes
                         */
                        int hash1 = e1.hashCode();
                        int hash2 = e2.hashCode();

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
        } else {
            /*
             * ... I don't know what kind of parent ordering you want!
             */
            throw new DebugException(orderingWithin.getClass().getName()
                    + " is an invalid class to be ordering by");
        }
    }
}
