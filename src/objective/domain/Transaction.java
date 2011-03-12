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
 * Base class of the Transaction hierarchy. Transactions are operations which
 * are balanced double-entry accounting events. That also means that, per
 * GAAP, they have to have a source document somewhere.
 * 
 * <p>
 * We override {@link #equals(Object)} so this can't be naturally used in
 * TreeSets, which is fine; see
 * {@link objective.services.TransactionComparator} if you need a SortedSet of
 * Transactions.
 * 
 * @author Andrew Cowie
 */
public abstract class Transaction extends DomainObject
{
    protected String description = null;

    /**
     * A reference connected with the transaction. This is <b>not</b> a unique
     * id or primary key, but rather some optional meta data originating from
     * the external source document.
     */
    protected String reference = null;

    protected Datestamp date = null;

    protected Transaction(long rowid) {
        super(rowid);
    }

    /**
     * Perform checks on the Transaction object to make sure it is balanced
     * Debits == Credits.
     */
    public boolean isBalanced(Entry[] entries) {
        long total;

        if (entries == null) {
            return true;
        }

        total = 0;

        for (Entry entry : entries) {
            if (entry instanceof Debit) {
                total += entry.getAmount();
            } else if (entry instanceof Credit) {
                total -= entry.getAmount();
            } else {
                throw new AssertionError();
            }
        }

        return (total == 0);
    }

    /**
     * Get the description of the transaction.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Set a description for the transaction.
     * 
     * @param description
     * @throws NullPointerException
     *             if you try to set a null description.
     */
    public void setDescription(String description) throws NullPointerException {
        if (description == null) {
            throw new NullPointerException("No null descriptions for transactions!");
        }
        this.description = description;
    }

    /**
     * Get the reference for the transaction, assuming one is set. Remember
     * that this is <b>not</b> a unique id or primary key, but rather some
     * meta data which may <b>optionally</b> be provided. Examples include
     * cheque numbers, bill numbers, or statement numbers.
     */
    public String getReference() {
        return reference;
    }

    /**
     * Set a reference identifier for the transaction. In usage terms, it's
     * optional. Typically this will be an invoice number or reciept number.
     * Note that its <B>NOT</B> enforced to be unique across Transactions
     * space.
     * 
     * @param reference
     *            the String to set as the Transaction's reference.
     */
    public void setReference(String reference) {
        this.reference = reference;
    }

    /**
     * Get the date of this transaction
     */
    public Datestamp getDate() {
        return date;
    }

    /**
     * Set the date of the transaction.
     */
    public void setDate(Datestamp date) {
        this.date = date;
    }

    public String getClassString() {
        return "Transaction";
    }

    public String toString() {
        return getClassString() + ": " + date + " " + description
                + (reference != null ? " [" + reference + "]" : "");
    }
}
