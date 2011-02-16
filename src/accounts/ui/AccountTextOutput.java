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
package accounts.ui;

import generic.persistence.DataClient;
import generic.ui.TextOutput;

import java.io.PrintWriter;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import accounts.domain.Account;
import accounts.services.AccountComparator;

/**
 * Output the contents of a Account and its subordinate Ledgers in a human
 * readable text form.
 * 
 * @author Andrew Cowie
 */
public class AccountTextOutput extends TextOutput
{
    protected static final int descWidth;

    protected static final int idWidth = 6;

    protected static final int typeWidth;

    static {
        int piece = COLUMNS / 7;
        typeWidth = piece * 3;
        // desc gets the rest, less one to keep off the edge.
        descWidth = COLUMNS - idWidth - typeWidth - 1;
    }

    private Set accounts;

    /**
     * @param store
     *            a DataClient from which to fetch all instances of Account.
     */
    public AccountTextOutput(DataClient store) {
        List aL = store.queryByExample(Account.class);

        accounts = new TreeSet(new AccountComparator());
        accounts.addAll(aL);
    }

    /**
     * @param accounts
     *            the Set of accounts to be output. It will be sorted by a new
     *            TreeSet during instantiation.
     */
    public AccountTextOutput(Set accounts) {
        this.accounts = new TreeSet(new AccountComparator());

        Iterator iter = accounts.iterator();
        while (iter.hasNext()) {
            Object o = iter.next();
            if (!(o instanceof Account)) {
                throw new IllegalArgumentException("The Set passed must only contain Account objects");
            }
            accounts.add(o);
        }
    }

    /**
     * @param a
     *            a single Account to run the outputter over. Use for spot
     *            debugging only.
     */
    public AccountTextOutput(Account a) {
        accounts = Collections.singleton(a);
    }

    /**
     * @param out
     *            the PrintWriter you want to send the output to.
     */
    public void toOutput(PrintWriter out) {
        if (accounts.size() == 0) {
            return;
        }

        Iterator aI = accounts.iterator();
        while (aI.hasNext()) {
            Account a = (Account) aI.next();

            out.print(pad("\"" + chomp(a.getTitle(), descWidth + idWidth - 3) + "\" ", descWidth
                    + idWidth, LEFT));
            // String codeText = a.getCode();
            // codeText = ((codeText == null) ? "" : codeText);
            // out.print(pad(codeText, idWidth, LEFT));
            out.print(pad(chomp(a.getClassString(), typeWidth), typeWidth, RIGHT));

            out.println();

            Set lS = a.getLedgers();
            if (lS == null) {
                // no ledgers...
            } else {
                LedgerTextOutput ledgerOutputter = new LedgerTextOutput(lS);
                ledgerOutputter.toOutput(out);
            }
            // blank line after account is done
            out.println();
        }
    }
}
