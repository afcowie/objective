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
 * Base class of employees and subcontractors. A Worker is someone we pay
 * money to - periodically or otherwise.
 */
public abstract class Worker extends DomainObject
{
    private String name;

    private CreditPositiveLedger expensesPayable;

    protected Worker(long rowid) {
        super(rowid);
    }

    /**
     * Get the [full] name of the person.
     */
    public String getName() {
        return name;
    }

    /**
     * Set the name of the Worker. This is full name; no need to bother with
     * first name/last name distinctions.
     * 
     * @param name
     *            a String with the full name of the person
     */
    public void setName(String name) {
        if ((name == null) || (name.equals(""))) {
            throw new IllegalArgumentException();
        }
        this.name = name;
    }

    /**
     * Get the Ledger within the ReimbursableExpensesPayableAccount that
     * refers to the amount owing this Worker.
     */
    public Ledger getExpensesPayable() {
        return expensesPayable;
    }

    /**
     * Set ledger within the ReimbursableExpensesPayableAccount that holds
     * this Worker's reimbursable expenses.
     */
    public void setExpensesPayable(Ledger ledger) {
        this.expensesPayable = (CreditPositiveLedger) ledger;
    }

    public String toString() {
        return name + " (" + getID() + ")";
    }
}
