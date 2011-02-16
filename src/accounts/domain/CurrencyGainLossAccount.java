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

/**
 * A single ledger account to record currency gains and losses. TODO Should
 * this in fact be a multi ledger account, with one ledger per currency? En
 * verra.
 * 
 * @author Andrew Cowie
 */
public class CurrencyGainLossAccount extends RevenueAccount implements SingleLedger
{
    private CreditPositiveLedger ledger = null;

    public CurrencyGainLossAccount() {
        super();
    }

    public CurrencyGainLossAccount(String title) {
        super(title);
        ledger = new CreditPositiveLedger();
        addLedger(ledger);
    }

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
                    "You must use a CreditPositiveLedger for an CurrencyGainLossAccount");
        }
        this.ledger = (CreditPositiveLedger) ledger;
    }

    public String getClassString() {
        return "Currency Gain/Loss Account";
    }
}
