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

import accounts.domain.SingleLedger;

/**
 * A bank account. This is more complicated than a simple cash account because
 * it implements a reconcilable ledger. Class description here.
 * 
 * @author Andrew Cowie
 */
public class BankAccount extends AssetAccount implements SingleLedger
{
    // private String institution = null;
    // private String name = null;

    /**
     * This is a convenience only for use in single ledger accounts, ie, this
     * one.
     */
    private DebitPositiveLedger ledger = null;

    /**
     * [For creating search prototypes]
     */
    public BankAccount(long rowid) {
        super(rowid);
    }

    /**
     * @param institution
     *            the name of the bank of financial institution carrying the
     *            account. Becomes Account title.
     * @param name
     *            the type or name of the account, for instance "Current
     *            Account". Used as Ledger name.
     */
    public BankAccount(String institution, String name) {
        super();
        this.setTitle(institution);

        ledger = new DebitPositiveLedger();
        ledger.setName(name);
        addLedger(ledger);
    }

    /**
     * Add an entry to the (single) Ledger of this BankAccount. FIXME
     */
    public void addEntry(Entry entry) {
        ledger.addEntry(entry);
        entry.setParentLedger(ledger);
        // TODO recalc account balance?
    }

    public Ledger getLedger() {
        return ledger;
    }

    public void setLedger(Ledger ledger) {
        if (ledger instanceof DebitPositiveLedger) {
            this.ledger = (DebitPositiveLedger) ledger;
        } else {
            throw new IllegalArgumentException("ledger argument needs ot be DebitPositive");
        }
    }

    public String getClassString() {
        return "Bank Account";
    }
}
