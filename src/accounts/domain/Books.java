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
package accounts.domain;

import generic.domain.Root;

import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Set;

import objective.domain.Account;
import objective.domain.Currency;
import objective.domain.Datestamp;
import objective.domain.EquityAccount;
import objective.domain.RevenueAccount;

import accounts.services.AddCurrencyCommand;
import accounts.services.InitBooksCommand;

/**
 * Root object of a set of accounts.
 * <P>
 * There are certain known factors about a set of books ("Currency Gain/Loss"
 * for instance), and so rather than generically floundering, we have direct
 * references to them here.
 * 
 * Someday, subclassing this will allow RawBooks and CookedBooks! (credit to
 * pd for that one).
 * 
 * @author Andrew Cowie
 * @author Paul Drain
 */
public class Books extends Root
{
    public void configure() {
        /*
         * The classes for which we want to turn on cascade {update,activate}
         * behaviour (these static methods are inherited from root)
         * 
         * At the moment, we leave out Account.class, Ledger.class,
         * Entry.class, Transaction.class because objects are activated by
         * most resolved subtype. (TODO: we need to find out how to change
         * this behaviour so we can tune it better)
         */
        markCascade(Books.class);
        markCascade(LinkedHashSet.class);
        markCascade(LinkedList.class);
    }

    /*
     * Instance variables ---------------------------------
     */

    /**
     * The date which these books were openned, used for bounds checking and
     * date acceleration. Should be used as the date of the Openning Balance
     * transaction? TODO
     */
    protected Datestamp dateOfInception = null;

    /**
     * The entire Set of Account objects. At Books level, Accounts are all
     * treated in common, be they Assets, Liabilities, etc. Utility methods
     * (TODO which may move) are provided to pull accounts from those
     * individual subclasses.
     */
    protected Set<Account> accounts = null;

    protected Set workers = null;

    protected Set currencies = null;

    /*
     * Convenience references -----------------------------
     */

    protected RevenueAccount gainLoss = null;

    protected EquityAccount retainedEarnings = null;

    /**
     * The home currency that the set of books is denomenated in.
     */
    protected Currency homeCurrency = null;

    /**
     * @return Returns the entire Accounts Set.
     */
    public Set<Account> getAccountsSet() {
        return accounts;
    }

    public void setAccountsSet(Set<Account> accounts) {
        if (accounts == null) {
            throw new IllegalArgumentException(
                    "Can't make a null Set the accounts held by this Books object!");
        }
        this.accounts = accounts;
    }

    /**
     * Get the currency gain/loss Account.
     */
    public RevenueAccount getGainLossAccount() {
        return gainLoss;
    }

    /**
     * Set the Account that will accumulate currency gains and currency
     * losses. We have arbitrarily (but somewhat in line with common practice)
     * chosen to represent this as a revenue account, ie, currency gain is
     * possitive, currency loss is negative.
     */
    public void setGainLossAccount(RevenueAccount gainLoss) {
        this.gainLoss = gainLoss;
    }

    /**
     * Get the retained earnings Account.
     */
    public EquityAccount getRetainedEarningsAccount() {
        return retainedEarnings;
    }

    /**
     * Set the Account that will be used to hold the retained earnings of the
     * business.
     */
    public void setRetainedEarningsAccount(EquityAccount retainedEarnings) {
        this.retainedEarnings = retainedEarnings;
    }

    /**
     * Get the Set of Workers (Employees and Subcontractors) associated with
     * the business represetned by these Books.
     */
    public Set getWorkers() {
        return workers;
    }

    /**
     * Set the Workers (Set of Employees and Subcontractors) associated with
     * the business represented by these Books.
     */
    public void setWorkers(Set workers) {
        this.workers = workers;
    }

    /**
     * Get the Set of Currency objects, which was populated by
     * {@link AddCurrencyCommand}s
     */
    public Set getCurrencySet() {
        return currencies;
    }

    /**
     * Used to initialize the Set which holds Currency Objects for this set of
     * books.
     * 
     * @see InitBooksCommand
     */
    public void setCurrencySet(Set currencies) {
        this.currencies = currencies;
    }

    /*
     * Output ---------------------------------------------
     */

    public Currency getHomeCurrency() {
        return homeCurrency;
    }

    /**
     * Set the Currency in which all underlying amounts in this set of Books
     * will be represented. This can only be set once.
     */
    public void setHomeCurrency(Currency home) {
        if (home == null) {
            throw new IllegalArgumentException();
        }
        if (homeCurrency != null) {
            throw new UnsupportedOperationException("Can't change the home currency once its set!");
        }
        homeCurrency = home;
    }
}
