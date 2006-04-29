/*
 * AccountsPayable.java
 * 
 * See LICENCE file for usage and redistribution terms
 * Copyright (c) 2006 Operational Dynamics
 */
package accounts.domain;

/**
 * Trade Creditors. Holds a ledger per business Entity to which we owe money.
 * Such debts presumably arise out of expense related transactions.
 * 
 * @author Andrew Cowie
 * @see accounts.domain.ItemsLedger
 * @see accounts.domain.AccountsReceivable
 */
public class AccountsPayable extends LiabilityAccount
{
	public AccountsPayable() {
		super();
	}

	/**
	 * 
	 * @param title
	 *            a title for the Accounts Receivable account. Australians
	 *            probably prefer something like "Trade Creditors"
	 */
	public AccountsPayable(String title) {
		super(title);
	}

	public void addLedger(Ledger ledger) {
		if (!(ledger instanceof ItemsLedger)) {
			throw new IllegalArgumentException("Ledgers added to AccountsPayable accounts need to be ItemLedgers");
		}
		super.addLedger(ledger);
	}

	public String getClassString() {
		return "Accounts Payable";
	}
}
