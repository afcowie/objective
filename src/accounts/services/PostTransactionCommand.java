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
package accounts.services;

import generic.persistence.DataClient;

import java.util.Iterator;
import java.util.Set;

import accounts.domain.Entry;
import accounts.domain.Ledger;
import accounts.domain.Transaction;

/**
 * Post a Transaction.
 * 
 * @author Andrew Cowie
 */
public class PostTransactionCommand extends TransactionCommand
{
    /**
     * Create a new PostTransactionCommand, specifying:
     * 
     * @param t
     *            the Transaction to be persisted.
     */
    public PostTransactionCommand(Transaction t) {
        super(t);
    }

    protected void action(DataClient store) throws CommandNotReadyException {
        super.validate();

        /*
         * Carry out the addition of the [values of the] Entries to the
         * [balances of the] Ledgers they bridge to.
         */

        Set entries = transaction.getEntries();
        Iterator iter = entries.iterator();
        while (iter.hasNext()) {
            Entry e = (Entry) iter.next();
            Ledger l = e.getParentLedger();
            l.addEntry(e);
            store.save(l);
        }

        /*
         * Persist the Transaction.
         */

        store.save(transaction);
    }

    public void reverse(DataClient store) {
        throw new UnsupportedOperationException();
    }

    public String getClassString() {
        return "Post Transaction";
    }
}
