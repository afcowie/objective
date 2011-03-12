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
package objective.domain;

/**
 * Expenses reimbursable to a Worker who incurred them.
 * 
 * @author Andrew Cowie
 */
public class ReimbursableTransaction extends Transaction
{
    /**
     * The person who the expenses are reimbursable to.
     */
    private Worker worker;

    public ReimbursableTransaction() {
        this(0);
    }

    public ReimbursableTransaction(long rowid) {
        super(rowid);
    }

    public Worker getWorker() {
        return worker;
    }

    /**
     * Set the person to whom the expeneses must be reimbursed.
     */
    public void setWorker(Worker worker) {
        if (worker == null) {
            throw new IllegalArgumentException("\n" + "Can't set null as the Worker you're reimbursing");
        }
        this.worker = worker;
    }

    public String getClassString() {
        return "Reimbursable Expenses";
    }

    public long getType() {
        return -11;
    }
}
