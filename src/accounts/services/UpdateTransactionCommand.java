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
package accounts.services;

import generic.persistence.DataClient;
import generic.persistence.Selector;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

import objective.domain.Entry;
import objective.domain.Ledger;
import objective.domain.Transaction;

/**
 * Update an existing Transaction already stored in the database. This is a
 * tricker case than PostTransactionCommand because of the fact that in
 * addition to having modified Entry objects, the Transaction object being
 * passed to save() may well have new Entry objects (no biggie) but equally
 * ones that have been removed. If we don't delete these they will become
 * orphans - unlike a live VM garbage collecting unreachable objects, unused
 * objects are not arbitrarily removed from the persistence store!
 * <p>
 * The following is assumed: a client or ui layer class making modifications
 * to a Transaction and asking UpdateTransactionCommand to persist these
 * changes <b>must call Transaction.removeEntry() and addEntry() for Entries
 * that are not simply being modified</b>.
 * <p>
 * This Command will then:
 * <ul>
 * <li>remove the Transaction's old Entries reducing the balances in the
 * various Ledgers to which they bridged.
 * <li>Add the new Entries to the parent Ledgers,
 * <li>then update the Transaction by calling DataClient.save().
 * </ul>
 */
public class UpdateTransactionCommand extends TransactionCommand
{
    public UpdateTransactionCommand(Transaction t) {
        super(t);
    }

    protected void action(DataClient store) throws CommandNotReadyException {
        super.validate();

        Transaction original;
        try {
            original = (Transaction) store.peek(transaction);
        } catch (NoSuchElementException nsoe) {
            throw new CommandNotReadyException(
                    "If we can't get the persisted version of the Transaction, how on earth are we updating it? "
                            + nsoe);
        }

        /*
         * Get the entries who claim this Transaction is their parent. Some
         * will actually still be in the Transaction, and some will have faded
         * into the sunset.
         */
        List storedEntries = store.nativeQuery(new Selector<Entry>(transaction) {
            public boolean match(Entry entry) {
                Transaction parent = entry.getParentTransaction();

                if (parent.congruent(target)) {
                    return true;
                }
                return false;
            }
        });

        /*
         * For each one, we query for the Ledger currently containing it. If
         * one is found (as should be for any pre-existing objects) so, we
         * remove it from that Ledger. We do it this way because the
         * parentLedger field may have been changed.
         */

        Set affectedLedgers = new LinkedHashSet();

        Iterator cI = storedEntries.iterator();
        while (cI.hasNext()) {
            Entry c = (Entry) cI.next();

            Ledger ledgerContainingEntry = null;

            List ledgers = store.queryByExample(Ledger.class);
            Iterator lI = ledgers.iterator();
            while (lI.hasNext()) {
                Ledger l = (Ledger) lI.next();
                Set eS = l.getEntries();
                if (eS == null) {
                    continue;
                }
                if (eS.contains(c)) {
                    ledgerContainingEntry = l;
                    break;
                }
            }

            if (ledgerContainingEntry != null) {
                Entry committed = (Entry) store.peek(c);

                long revised = c.getAmount().getNumber();
                c.getAmount().setValue(committed.getAmount());
                ledgerContainingEntry.removeEntry(c);
                c.getAmount().setNumber(revised);

                affectedLedgers.add(ledgerContainingEntry);
            } else {
                /*
                 * Huh? How can there be a committed Entry without its
                 * parentLedger viable?
                 */
                throw new IllegalStateException(
                        "Huh? How can there be a committed Entry that doesn't belong to a Ledger?");
            }
        }

        /*
         * We now update the Transaction by [re-]adding its Entries to the
         * various parent Ledgers and then simply save()ing it; our
         * persistence engine will take care of adding new objects and
         * updating existing ones.
         */

        Set liveEntries = transaction.getEntries();

        Iterator eI = liveEntries.iterator();
        while (eI.hasNext()) {
            Entry e = (Entry) eI.next();
            Ledger l = e.getParentLedger();

            l.addEntry(e);

            affectedLedgers.add(l);
        }

        /*
         * And now persist the objects: the Transaction, its Entries, and all
         * the Ledgers affected.
         */
        store.save(transaction);

        eI = liveEntries.iterator();
        while (eI.hasNext()) {
            Entry e = (Entry) eI.next();
            store.save(e);
        }

        Iterator alI = affectedLedgers.iterator();
        while (alI.hasNext()) {
            Ledger al = (Ledger) alI.next();
            store.save(al);
        }

        /*
         * With that taken care of, we now clean up our mess by deleting the
         * Entries which are no longer in use. We do this by fetching up
         * Entries that have this Transaction marked as their parent, then
         * deleting any which are not in the live Transaction's entries Set.
         */

        cI = storedEntries.iterator();
        while (cI.hasNext()) {
            Entry c = (Entry) cI.next();
            if (!(liveEntries.contains(c))) {

                store.delete(c);
                // FIXME and delete Amount?
            }
        }
    }

    protected void reverse(DataClient client) throws CommandNotUndoableException {
        throw new UnsupportedOperationException();
    }

    public String getClassString() {
        return "Update Transaction";
    }
}
