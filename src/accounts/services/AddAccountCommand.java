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

import java.util.Set;

import objective.domain.Account;

import accounts.domain.Books;

/**
 * Add an Account to the system.
 * 
 * @author Andrew Cowie
 */
public class AddAccountCommand extends Command
{
    private Account account = null;

    /**
     * Create a new AddAccountCommand, specifying:
     * 
     * @param account
     *            The Account to add. Must not already be persisted in the
     *            DataClient.
     */
    public AddAccountCommand(Account account) {
        if (account == null) {
            throw new IllegalArgumentException("Can't add a null account");
        }
        this.account = account;
    }

    protected void action(DataClient store) {
        Books root = (Books) store.getRoot();
        Set accounts = root.getAccountsSet();
        if (accounts.add(account) == false) { // dup!?!
            throw new IllegalStateException("How did you add an account that's already in the system?");
        }

        /*
         * Store the collection. Rely on cascading update depth to add the new
         * account object along the way.
         */

        store.save(accounts);
    }

    protected void reverse(DataClient store) {
        throw new UnsupportedOperationException();
    }

    public String getClassString() {
        return "Add Account";
    }
}
