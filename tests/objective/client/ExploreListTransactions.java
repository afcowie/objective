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

import objective.domain.Account;
import objective.domain.Amount;
import objective.domain.Credit;
import objective.domain.Datestamp;
import objective.domain.Debit;
import objective.domain.Entry;
import objective.domain.Ledger;
import objective.domain.Transaction;
import objective.persistence.DataStore;
import objective.services.NotFoundException;
import objective.services.TransactionOperations;

import org.gnome.gtk.Gtk;

/**
 * Run an accounting transaction through to the database.
 * 
 * @author Andrew Cowie
 */
public class ExploreListTransactions
{
    private static void list(DataStore data) throws NotFoundException {
        Transaction[] list;
        Transaction t;
        TransactionOperations services;
        int i, j;
        Entry[] entries;
        Entry e;
        Ledger l;
        Account a;
        long cents;
        String date, dr, cr, code;

        services = new TransactionOperations(data);

        list = data.listTransactions();

        for (i = 0; i < list.length; i++) {
            t = list[i];
            date = Datestamp.dateToString(t.getDate());
            System.out.printf("%s, %-30.30s %29s\n", date, "\"" + t.getDescription() + "\"",
                    t.getClassString());

            entries = services.findEntries(t);
            for (j = 0; j < entries.length; j++) {
                e = entries[j];

                l = e.getParentLedger();
                a = l.getParentAccount();

                cents = e.getAmount();
                code = e.getCurrency().getCode();

                if (e instanceof Debit) {
                    dr = Amount.numberToString(cents) + " " + code;
                    cr = "";
                } else if (e instanceof Credit) {
                    dr = "";
                    cr = Amount.numberToString(cents) + " " + code;
                } else {
                    throw new AssertionError();
                }

                System.out.printf("%-40.40s  %14s%14s\n", a.getTitle() + " » " + l.getName(), dr, cr);
            }
            System.out.println();
        }
    }

    public static void main(String[] args) throws NotFoundException {
        DataStore data;

        Gtk.init(args);

        data = new DataStore("schema/accounts.db");

        list(data);

        data.close();
    }
}
