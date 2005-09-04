/*
 * GenericExpenseAccount.java
 * 
 * See LICENCE file for usage and redistribution terms
 * Copyright (c) 2005 Operational Dynamics
 */
package accounts.domain;

public class GenericExpenseAccount extends ExpenseAccount
{
	public GenericExpenseAccount() {
		super();
	}

	public GenericExpenseAccount(String title) {
		super(title);
	}

	public GenericExpenseAccount(String title, Ledger[] ledgers) {
		super(title);
		for (int i = 0; i < ledgers.length; i++) {
			addLedger(ledgers[i]);
		}
	}

	public String getClassString() {
		return "Generic Expense";
	}
}
