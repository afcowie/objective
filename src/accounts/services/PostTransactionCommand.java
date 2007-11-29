/*
 * PostTransactionCommand.java
 * 
 * See LICENCE file for usage and redistribution terms
 * Copyright (c) 2005-2006 Operational Dynamics
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
