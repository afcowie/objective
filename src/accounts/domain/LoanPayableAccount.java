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
 * An account containing loans. As designed, use a {@link LoanLedger} for each
 * person or entity making loans to the business, and then {@link Loan} for
 * individual loan tranches.
 */
public class LoanPayableAccount extends LiabilityAccount
{
    public LoanPayableAccount() {
        super();
    }

    public LoanPayableAccount(String title, LoanLedger[] loanLedgers) {
        super(title);

        for (int i = 0; i < loanLedgers.length; i++) {
            addLedger(loanLedgers[i]);
        }
    }

    public void addLedger(Ledger loanLedger) {
        if (!(loanLedger instanceof LoanLedger)) {
            throw new IllegalArgumentException(
                    "When using LoanPayableAccount.addLedger(), the argument must be a LoanLedger (not merely a CreditPositiveLedger one)");
        }
        super.addLedger(loanLedger);
    }

    public String getClassString() {
        return "Loan Payable";
    }
}
