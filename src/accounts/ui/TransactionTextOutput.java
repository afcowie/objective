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
 * contacted through http://research.operationaldynamics.com/projects/objective/.
 */
package accounts.ui;

import generic.persistence.DataClient;
import generic.ui.TextOutput;

import java.io.PrintWriter;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import accounts.domain.Transaction;
import accounts.services.TransactionComparator;

/**
 * Output the contents of a Transaction in a human readable text form.
 * 
 * @author Andrew Cowie
 */
public class TransactionTextOutput extends TextOutput
{
    protected static final int descWidth;

    protected static final int idWidth;

    protected static final int typeWidth;

    static {
        int piece = COLUMNS / 7;
        idWidth = piece;
        typeWidth = piece * 2;
        // desc gets the rest, in this case 4/7ths, less one to keep off the
        // edge.
        descWidth = COLUMNS - idWidth - typeWidth - 1;
    }

    private Set transactions;

    /**
     * @param store
     *            a DataClient from which to fetch all instances of
     *            Transaction.
     */
    public TransactionTextOutput(DataClient store) {
        List tL = store.queryByExample(Transaction.class);

        transactions = new TreeSet(new TransactionComparator());
        transactions.addAll(tL);
    }

    /**
     * @param transactions
     *            the Set of transactions to be output. It will be sorted by a
     *            new TreeSet during instantiation.
     */
    public TransactionTextOutput(Set transactions) {
        this.transactions = new TreeSet(new TransactionComparator());

        Iterator iter = transactions.iterator();
        while (iter.hasNext()) {
            Object o = iter.next();
            if (!(o instanceof Transaction)) {
                throw new IllegalArgumentException(
                        "The Set passed must only contain Transaction objects");
            }
            transactions.add(o);
        }
    }

    /**
     * @param t
     *            a single Transaction to run the outputter over. Use for spot
     *            debugging only.
     */
    public TransactionTextOutput(Transaction t) {
        transactions = Collections.singleton(t);
        transactions.add(t);
    }

    /**
     * @param out
     *            the PrintWriter you want to send the output to.
     */
    public void toOutput(PrintWriter out) {
        if (transactions.size() == 0) {
            return;
        }

        Iterator tI = transactions.iterator();
        while (tI.hasNext()) {
            Transaction t = (Transaction) tI.next();

            out.print(pad("\"" + chomp(t.getDescription(), descWidth - 3) + "\" ", descWidth, LEFT));
            out.print(pad(t.getReference(), idWidth, LEFT));
            out.print(pad(t.getClassString(), typeWidth, RIGHT));

            out.println();

            EntryTextOutput entryOutputter = new EntryTextOutput(t);
            entryOutputter.toOutput(out);
            out.println();
        }
    }
}
