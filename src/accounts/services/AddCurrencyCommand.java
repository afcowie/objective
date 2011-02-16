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
package accounts.services;

import generic.persistence.DataClient;

import java.util.Set;

import accounts.domain.Books;
import accounts.domain.Currency;

/**
 * Add a Currency object to this set of books
 * 
 * @author Andrew Cowie
 */
public class AddCurrencyCommand extends Command
{
    private Currency currency = null;

    /**
     * Create a new AddCurrencyCommand, specifying:
     * 
     * @param cur
     *            the Currency object to add
     */
    public AddCurrencyCommand(Currency cur) {
        if (cur == null) {
            throw new IllegalArgumentException("null Currency object passed");
        }
        currency = cur;
    }

    protected void action(DataClient store) {
        /*
         * Add currency to Books's currency list
         */
        Books root = (Books) store.getRoot();
        Set currencies = root.getCurrencySet();
        currencies.add(currency);

        /*
         * Store the new account itself, and update the collection which
         * contains it.
         */
        store.save(currency);
        store.save(currencies);
    }

    public void reverse(DataClient store) {
        throw new UnsupportedOperationException();
    }

    public String getClassString() {
        return "Add Currency";
    }
}
