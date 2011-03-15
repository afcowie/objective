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
package objective.client;

import java.text.ParseException;

import objective.domain.Credit;
import objective.domain.Currency;
import objective.domain.Datestamp;
import objective.domain.Debit;
import objective.domain.Entry;
import objective.domain.GenericTransaction;
import objective.domain.Ledger;
import objective.domain.Transaction;
import objective.persistence.DataStore;
import objective.services.AccountOperations;
import objective.services.NotFoundException;
import objective.services.TransactionOperations;

import org.gnome.gtk.Gtk;

/**
 * Run an accounting transaction through to the database.
 * 
 * @author Andrew Cowie
 */
public class ExplorePostTransaction
{
    private static void doMockCreate(final DataStore data) throws NotFoundException, ParseException {
        final AccountOperations accounts;
        final Ledger l1, l2;
        final TransactionOperations services;
        Transaction t;
        long datestamp;
        Currency aud;
        final Entry e1, e2;

        accounts = new AccountOperations(data);
        services = new TransactionOperations(data);

        aud = data.lookupCurrency("AUD");
        l1 = accounts.findLedger("Andrew Cowie")[0];
        l2 = accounts.findLedger("Meals")[0];

        t = new GenericTransaction(0);

        datestamp = Datestamp.stringToDate("25 Dec 10");
        t.setDate(datestamp);
        t.setDescription("Christmas Turkey");

        e1 = new Credit(0);
        e1.setAmount(7500);
        e1.setCurrency(aud);
        e1.setValue(7500);
        e1.setParentLedger(l1);
        e1.setParentTransaction(t);

        e2 = new Debit(0);
        e2.setAmount(7500);
        e2.setCurrency(aud);
        e2.setValue(7500);
        e2.setParentLedger(l2);
        e2.setParentTransaction(t);

        services.postTransaction(t, e1, e2);

        id = t.getID();
    }

    private static long id;

    private static void doMockUpdate(DataStore data) {
        Transaction t;
        TransactionOperations services;
        Entry[] entries;
        Entry e2;
        Currency cad;

        services = new TransactionOperations(data);

        t = services.findTransaction(id);
        entries = services.findEntries(t);

        cad = data.lookupCurrency("CAD");
        e2 = entries[1];
        e2.setCurrency(cad);
        e2.setAmount(10840);

        services.postTransaction(t, entries);
    }

    public static void main(String[] args) throws NotFoundException, ParseException {
        DataStore data;

        Gtk.init(args);

        data = new DataStore("schema/accounts.db");

        doMockCreate(data);
        doMockUpdate(data);

        data.close();
    }
}
