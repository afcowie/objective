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
 * An entry in an account. These are formed in balanced (total Debits = total
 * Credits) transactions. Entries form a bridge between Transactions and
 * Accounts.
 * 
 * @author Andrew Cowie
 */
public class Entry extends DomainObject
{
    /**
     * If this is a real Entry, this is the face value amount in whatever
     * currency.
     */
    private long amount;

    /**
     * If this is a real entry, then what currency is it denominated in?
     */
    private Currency currency;

    /**
     * The value of this Entry, in home currency terms.
     */
    private long value;

    /**
     * The ledger to which this entry [will be] added.
     */
    private Ledger parentLedger = null;

    /**
     * The Transaction which this entry is a part of.
     */
    private Transaction parentTransaction = null;

    public Entry(long rowid) {
        super(rowid);
    }

    /**
     * Get the amount (value) this entry describes.
     */
    public long getAmount() {
        return amount;
    }

    public void setAmount(long amount) {
        this.amount = amount;
    }

    public Currency getCurrency() {
        return currency;
    }

    public void setCurrency(Currency currency) {
        this.currency = currency;
    }

    /**
     * Get the underlying value of this Entry. This will be valid regardless
     * of whether or not this is a "real" Entry representing money.
     */
    public long getValue() {
        return value;
    }

    /**
     * Set the underlying value of this Entry. This is in home currency terms.
     */
    public void setValue(long value) {
        this.value = value;
    }

    public Ledger getParentLedger() {
        return parentLedger;
    }

    public void setParentLedger(Ledger parent) {
        this.parentLedger = parent;
    }

    public Transaction getParentTransaction() {
        return parentTransaction;
    }

    public void setParentTransaction(Transaction parent) {
        this.parentTransaction = parent;
    }

    /**
     * Prints the value of this entry, with an indication of whether it is a
     * Debit or Credit value.
     */
    public String toString() {
        StringBuffer buf;
        String str, code;

        buf = new StringBuffer();

        if (currency == null) {
            str = Amount.numberToString(amount);
            buf.append(str);

            buf.append(' ');
            buf.append("   ");
        } else {
            str = Amount.numberToString(value);
            buf.append(str);

            buf.append(' ');

            code = currency.getCode();
            buf.append(code);
        }

        buf.append(' ');

        if (this instanceof Debit) {
            buf.append("DR");
        } else if (this instanceof Credit) {
            buf.append("CR");
        } else {
            throw new IllegalStateException();
        }
        buf.append(' ');
        buf.append(super.toString());
        return buf.toString();
    }
}
