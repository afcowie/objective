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
package objective.services;

import java.util.ArrayList;

import objective.domain.Currency;
import objective.domain.Entry;
import objective.domain.Ledger;
import objective.domain.Transaction;
import objective.domain.Worker;
import objective.persistence.DataStore;
import objective.persistence.Operation;

import com.operationaldynamics.sqlite.Statement;

public class TransactionOperations extends Operation
{
    private Statement lookup;

    private DataStore data; // FIXME super

    public TransactionOperations(DataStore data) {
        super(data);
        this.data = data;
    }

    public Transaction findTransaction(final long transactionId) {
        return data.lookupTransaction(transactionId);
    }

    public void release() {
        if (lookup != null) {
            lookup.finish();
            lookup = null;
        }
    }

    public void postTransaction(Transaction transaction, Entry... entries) {
        int i;
        final int I;
        Entry entry;

        I = entries.length;

        /*
         * Validate
         */

        validate(transaction);

        for (i = 0; i < I; i++) {
            entry = entries[i];
            validate(entry);
        }

        /*
         * Post
         */

        try {
            data.begin();

            if (transaction.getID() == 0) {
                data.createTransaction(transaction);
            }
            data.updateTransaction(transaction);

            for (i = 0; i < I; i++) {
                entry = entries[i];

                if (entry.getID() == 0) {
                    data.createEntry(entry);
                }
                data.updateEntry(entry);
            }

            data.commit();
        } catch (RuntimeException re) {
            data.rollback();
            throw re;
        }
    }

    /**
     * Ensure the Transaction is in proper form.
     */
    /*
     * We assume our code can get this right, so failing validity is a
     * programmer error, not a user one. Hence unchecked exceptions.
     */
    private static void validate(Transaction t) {
        if (t.getDate() == 0L) {
            throw new IllegalStateException("\n" + "Date must be set before you can post a Transaction");
        }
    }

    /**
     * Ensure the Entry is in proper form.
     */
    private static void validate(Entry e) {
        if (e.getAmount() == 0L) {
            throw new IllegalStateException("\n" + "Entry amount can't be 0");
        }
        if (e.getValue() == 0L) {
            throw new IllegalStateException("\n" + "Entry value can't be 0");
        }
        if (e.getCurrency() == null) {
            throw new IllegalStateException("\n" + "Entry Currency must be set");
        }
    }

    /**
     * Delete the given Transaction
     * 
     * @param transaction
     */
    /*
     * This won't work yet due to foreign keys from entries
     */
    public void deleteTransaction(Transaction transaction) {
        throw new UnsupportedOperationException();
    }

    /**
     * Find the Entry rows that have the data for the given Transaction.
     */
    public Entry[] findEntries(Transaction t) {
        Statement stmt;
        long transactionId, entryId;
        int num;
        ArrayList<Entry> list;
        Entry entry;
        Entry[] result;

        stmt = db.prepare("SELECT entry_id FROM entries WHERE transaction_id = ?");

        transactionId = t.getID();
        stmt.bindInteger(1, transactionId);

        list = new ArrayList<Entry>(4);

        while (stmt.step()) {
            entryId = stmt.columnInteger(0);
            entry = data.lookupEntry(entryId);
            if (entry == null) {
                throw new IllegalStateException();
            }
            list.add(entry);
        }

        stmt.finish();

        num = list.size();
        result = new Entry[num];
        result = list.toArray(result);

        return result;
    }

    /**
     * Find the Worker that owns a given ReimbursableExpensesLiabilityAccount
     * » Ledger.
     */
    /*
     * Assumes that this Ledger corresponds to a Worker's expenses payable,
     * which is rather unsafe!
     */
    public Worker findWorker(Ledger l) {
        final Statement stmt;
        final long ledgerId, workerId;
        final Worker worker;

        stmt = db.prepare("SELECT worker_id FROM workers w, ledgers l WHERE l.ledger_id = ? AND w.ledger_id = l.ledger_id");

        ledgerId = l.getID();
        stmt.bindInteger(1, ledgerId);

        stmt.step();
        workerId = stmt.columnInteger(0);
        worker = data.lookupWorker(workerId);

        stmt.finish();

        return worker;
    }

    public Currency findCurrencyHome() {
        return data.lookupCurrency("AUD");
    }
}
