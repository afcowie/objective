/*
 * AddCurrencyCommand.java
 * 
 * See LICENCE file for usage and redistribution terms
 * Copyright (c) 2005-2006 Operational Dynamics
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
