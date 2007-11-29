/*
 * ReimbursableExpensesTransaction.java
 * 
 * See LICENCE file for usage and redistribution terms
 * Copyright (c) 2006 Operational Dynamics
 */
package accounts.domain;

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
