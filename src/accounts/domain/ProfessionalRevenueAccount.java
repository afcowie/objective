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
 * A revenue account specific to professional services such as consulting. It
 * has two built in Ledgers: one for fee revenue, and one representing
 * received expense reimbursement payments.
 * 
 * @author Andrew Cowie
 */
public class ProfessionalRevenueAccount extends RevenueAccount
{
    public ProfessionalRevenueAccount() {
        super();
    }

    /**
     * @param accountTitle
     *            Account's title, perhaps "Performance Coaching"
     * @param feeName
     *            Ledger name, typically "Consulting Fees"
     */
    public ProfessionalRevenueAccount(String accountTitle, String feeName) {
        super(accountTitle);
        Ledger[] ledgers = {
                new CreditPositiveLedger(feeName), new CreditPositiveLedger("Expense Reimbursement")
        };
        for (int i = 0; i < ledgers.length; i++) {
            addLedger(ledgers[i]);
        }
    }

    public String getClassString() {
        return "Professional Services Revenue";
    }
}
