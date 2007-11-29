/*
 * ReimbursableExpensesPayableAccount.java
 * 
 * See LICENCE file for usage and redistribution terms
 * Copyright (c) 2006 Operational Dynamics
 */
package accounts.domain;

/**
 * Expenses payable to employees and subcontractors.
 * 
 * @author Andrew Cowie
 */
public class ReimbursableExpensesPayableAccount extends LiabilityAccount
{

    public ReimbursableExpensesPayableAccount() {
        super();
    }

    public ReimbursableExpensesPayableAccount(String title) {
        super(title);
    }

    public String getClassString() {
        return "Reimbursable Expenses Payable";
    }
}
