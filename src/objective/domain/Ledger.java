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
package objective.domain;

import objective.persistence.DomainObject;

/**
 * Base class for the ledgers within actual accounts.
 * 
 * @author Andrew Cowie
 */
public abstract class Ledger extends DomainObject
{
    private String name;

    private Account parentAccount;

    private Currency currency;

    protected Ledger(long rowid) {
        super(rowid);
    }

    /**
     * Get the name tag (owner, customer, creditor, etc) of this Ledger
     * 
     * @return a String with the name of the Ledger.
     */
    public String getName() {
        return name;
    }

    /**
     * Set the name of this Ledger.
     */
    public void setName(String name) {
        if (name.equals("")) {
            throw new IllegalArgumentException("Can't set a blank String as the name of a Ledger");
        }
        this.name = name;
    }

    /**
     * parentAccount is a relation which allows one to walk "up" the object
     * graph.
     * 
     * @return the Account which is the parent of this Ledger, which was set
     *         when this Ledger was added to an Account.
     * @see Entry#getParentLedger()
     * @see Entry#getParentTransaction()
     * @see Account#addLedger(Ledger)
     */
    public Account getParentAccount() {
        return parentAccount;
    }

    /**
     * A relation to allow you to track up the object graph, going the reverse
     * direction to the Set which Account contains which caries the Ledgers.
     * 
     * @param parent
     *            the Account which this Ledger belongs to.
     * @see Account#addLedger(Ledger)
     */
    public void setParentAccount(Account parent) {
        this.parentAccount = parent;
    }

    public String getClassString() {
        return "Ledger";
    }

    public String toString() {
        return getClassString() + ": " + name + " (" + getID() + ")";
    }

    /**
     * Yes, colour is spelled with a u... but in GTK it's spelled color. Fine.
     */
    public String getColor(boolean active) {
        if (this instanceof DebitPositiveLedger) {
            if (active) {
                return Debit.COLOR_ACTIVE;
            } else {
                return Debit.COLOR_NORMAL;
            }
        } else if (this instanceof CreditPositiveLedger) {
            if (active) {
                return Credit.COLOR_ACTIVE;
            } else {
                return Credit.COLOR_NORMAL;
            }

        } else {
            return "";
        }
    }

    /**
     * The currency this Ledger is denominated in <i>if it is a cash
     * account</i>. <code>null</code> is the usual case for "accounting value"
     * Ledgers.
     */
    public void setCurrency(Currency currency) {
        this.currency = currency;
    }

    public Currency getCurrency() {
        return currency;
    }
}
