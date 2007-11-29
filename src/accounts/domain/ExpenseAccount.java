/*
 * ExpenseAccount.java
 * 
 * See LICENCE file for usage and redistribution terms
 * Copyright (c) 2005 Operational Dynamics
 */
package accounts.domain;

/**
 * An expense account.
 */
public class ExpenseAccount extends DebitPositiveAccount
{
    public ExpenseAccount() {
        super();
    }

    public ExpenseAccount(String title) {
        super(title);
    }

    public String getClassString() {
        return "Expense";
    }
}
