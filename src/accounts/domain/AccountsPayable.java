/*
 * ObjectiveAccounts, accounting for small professional services firms.
 *
 * Copyright © 2006-2011 Operational Dynamics Consulting, Pty Ltd
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
 * Trade Creditors. Holds a ledger per business Entity to which we owe money.
 * Such debts presumably arise out of expense related transactions.
 * 
 * @author Andrew Cowie
 * @see accounts.domain.ItemsLedger
 * @see accounts.domain.AccountsReceivable
 */
public class AccountsPayable extends LiabilityAccount
{
    public AccountsPayable() {
        super();
    }

    /**
     * 
     * @param title
     *            a title for the Accounts Receivable account. Australians
     *            probably prefer something like "Trade Creditors"
     */
    public AccountsPayable(String title) {
        super(title);
    }

    public void addLedger(Ledger ledger) {
        if (!(ledger instanceof ItemsLedger)) {
            throw new IllegalArgumentException(
                    "Ledgers added to AccountsPayable accounts need to be ItemLedgers");
        }
        super.addLedger(ledger);
    }

    public String getClassString() {
        return "Accounts Payable";
    }
}
