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

import objective.domain.Account;
import objective.domain.Ledger;
import objective.persistence.DataStore;
import objective.persistence.Operation;

import com.operationaldynamics.sqlite.Statement;

public class AccountOperations extends Operation
{
    private Statement lookup;

    private DataStore data;

    public AccountOperations(DataStore data) {
        super(data);
        this.data = data;
    }

    /**
     * Get the Account with the given title.
     */
    /*
     * FIXME Titles aren't unique, so this isn't unary.
     */
    public Account[] findAccount(final String query) throws NotFoundException {
        final Statement stmt;
        final String[] sql;
        final long accountId;
        final Account result;

        sql = new String[] {
            "SELECT account_id",
            "FROM accounts",
            "WHERE title = ?"
        };

        stmt = db.prepare(combine(sql));

        stmt.bindText(1, query);

        if (stmt.step()) {
            accountId = stmt.columnInteger(0);
        } else {
            throw new NotFoundException();
        }

        stmt.finish();

        result = data.lookupAccount(accountId);
        return new Account[] {
            result
        };
    }

    public void release() {
        if (lookup != null) {
            lookup.finish();
            lookup = null;
        }
    }

    /**
     * Delete the given Account.
     * 
     * @param account
     */
    /*
     * This won't work yet due to foreign keys from ledgers
     */
    public void deleteAccount(Account account) {
        throw new UnsupportedOperationException();
    }

    /**
     * Get the Ledgers with the given name.
     */
    public Ledger[] findLedger(final String query) throws NotFoundException {
        final Statement stmt;
        final String[] sql;
        final long ledgerId;
        final Ledger result;

        sql = new String[] {
            "SELECT ledger_id",
            "FROM ledgers",
            "WHERE name = ?"
        };

        stmt = db.prepare(combine(sql));

        stmt.bindText(1, query);

        if (stmt.step()) {
            ledgerId = stmt.columnInteger(0);
        } else {
            throw new NotFoundException();
        }

        stmt.finish();

        result = data.lookupLedger(ledgerId);
        return new Ledger[] {
            result
        };
    }
}
