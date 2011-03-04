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

import objective.domain.Entry;
import objective.domain.Transaction;
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

    public void postTransaction(Transaction t, Entry... entries) {
        int i;
        final int I;
        Entry e;

        if (t.getID() == 0) {
            data.createTransaction(t);
        }

        data.updateTransaction(t);

        I = entries.length;

        for (i = 0; i < I; i++) {
            e = entries[i];

            if (e.getID() == 0) {
                data.createEntry(e);
            }
            data.updateEntry(e);
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
}
