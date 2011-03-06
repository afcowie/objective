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

/**
 * An asset account, implemented with a single Ledger.
 * 
 * @author Andrew Cowie
 */
public class CashAccount extends AssetAccount
{
    /**
     * This is a convenience only for use in single ledger accounts, ie, this
     * one.
     */
    private DebitPositiveLedger ledger = null;

    /**
     * [For creating search prototypes]
     */
    public CashAccount(long rowid) {
        super(rowid);
    }

    /*
     * Getters and Setters --------------------------------
     */

    public Ledger getLedger() {
        return ledger;
    }

    public void setLedger(Ledger ledger) {
        if (!(ledger instanceof DebitPositiveLedger)) {
            throw new IllegalArgumentException("You must use a DebitPositiveLedger for a CashAccount");
        }
        this.ledger = (DebitPositiveLedger) ledger;
    }

    public String getClassString() {
        return "Cash";
    }
}
