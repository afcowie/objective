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

import java.util.Set;

/**
 * This provides a CreditPositiveLedger which implements ItemsLedger
 */
public class LoanLedger extends CreditPositiveLedger implements ItemsLedger
{
    protected Set loans = null;

    public LoanLedger() {
        super();
    }

    public LoanLedger(String name) {
        super(name);
    }

    // public LoanLedger(String name, Loan[] loans) {
    // super(name);
    // if (loans == null) {
    // throw new IllegalArgumentException("need to specify an array of Loan
    // objects with this constructor");
    // }
    // this.loans = loans;
    // }

    /*
     * Implementation of inhereted abstract methods -------
     */

    public Set getItems() {
        return loans;
    }

    public void setItems(Set items) {
        loans = items;
    }

    public String getClassString() {
        return "Loan Ledger";
    }

}
