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

import objective.domain.Datestamp;
import objective.domain.Entry;
import objective.domain.Transaction;

/**
 * Expenses reimbursable to an Worker
 * 
 * @author Andrew Cowie
 */
public class ReimbursableExpensesTransaction extends Transaction
{
    /**
     * The person who the expenses are reimbursable to.
     */
    private Worker worker;

    // FUTURE maybe a "main" Entry? Common case; if we need it, push it up to
    // a
    // superclass.

    public ReimbursableExpensesTransaction() {
        super();
    }

    public ReimbursableExpensesTransaction(Worker person, String description, Datestamp date,
            Entry[] entries) {
        super(description, date, entries);
        setWorker(person);
    }

    public Worker getWorker() {
        return worker;
    }

    /**
     * Set the person to whom the expeneses must be reimbursed.
     */
    public void setWorker(Worker worker) {
        if (worker == null) {
            throw new IllegalArgumentException("Can't set null as the Worker you're reimbursing");
        }
        this.worker = worker;
    }

    public String getClassString() {
        return "Reimbursable Expenses";
    }
}
