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
package accounts.domain;

/**
 * A single ledger account for use in sole proprietorships. (ie, we use this
 * for testing :))
 * 
 * @author Andrew Cowie
 */
public class OwnersEquityAccount extends EquityAccount implements SingleLedger
{
    private CreditPositiveLedger ledger = null;

    /**
     * 
     */
    public OwnersEquityAccount() {
        super();
    }

    /**
     * @param accountTitle
     * @param ledgerName
     */
    public OwnersEquityAccount(String accountTitle, String ledgerName) {
        super(accountTitle);
        ledger = new CreditPositiveLedger();
        ledger.setName(ledgerName);
        addLedger(ledger);
    }

    /**
     * Add an entry to the single Ledger in this Account.
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
        if (!(ledger instanceof CreditPositiveLedger)) {
            throw new IllegalArgumentException(
                    "You must use a CreditPositiveLedger for an OwnersEquityAccount");
        }
        this.ledger = (CreditPositiveLedger) ledger;
    }

    public String getClassString() {
        return "Owner's Equity";
    }
}
