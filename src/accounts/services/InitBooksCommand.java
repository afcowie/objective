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

import java.util.LinkedHashSet;
import java.util.Set;

import accounts.domain.Books;
import accounts.domain.Currency;

/**
 * Create a new set of company books. Obvisouly, this is the first command,
 * and likewise, really, only ever run once!
 * 
 * @author Andrew Cowie
 */
public class InitBooksCommand extends Command
{
    /**
     * The home (native) currency to be set by this Command.
     * 
     * @see Books#getHomeCurrency()
     */
    protected transient Currency home;

    /**
     * Create a new InitBooksCommand, specifying:
     * 
     * @param home
     *            The Currency to be set as the "home", or underlying natural
     *            currency which the books are kept in.
     */
    public InitBooksCommand(Currency home) {
        if (home == null) {
            throw new IllegalArgumentException("Home Currency object must be non-null and initialized");
        }
        this.home = home;
    }

    /**
     * Allow the implicit () constructor for subclasses, which can perfectly
     * well set the home currency themsleves.
     */
    protected InitBooksCommand() {}

    protected void action(DataClient store) throws CommandNotReadyException {
        if (home == null) {
            throw new CommandNotReadyException("home Currency not set");
        }

        Books root = new Books();

        store.setRoot(root);

        Set accounts = new LinkedHashSet();
        root.setAccountsSet(accounts);

        /*
         * Establish the Collection for Currency objects, set the home
         * currency for this Books, and add it as the first currency in the
         * Books's Currencies Set
         */

        Set currencies = new LinkedHashSet();
        root.setCurrencySet(currencies);

        root.setHomeCurrency(home);

        currencies.add(home);

        /*
         * Persist. Just the top level Books object; let cascading update
         * depth do its thing.
         */

        store.save(root);
    }

    public void reverse(DataClient store) {
        throw new UnsupportedOperationException();
    }

    public String getClassString() {
        return "Initialize Books";
    }
}
