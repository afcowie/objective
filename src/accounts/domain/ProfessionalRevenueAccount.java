/*
 * ProfessionalRevenueAccount.java
 * 
 * See LICENCE file for usage and redistribution terms
 * Copyright (c) 2005 Operational Dynamics
 */
package accounts.domain;

/**
 * A revenue account specific to professional services such as consulting. It
 * has two built in Ledgers: one for fee revenue, and one representing received
 * expense reimbursement payments.
 * 
 * @author Andrew Cowie
 */
public class ProfessionalRevenueAccount extends RevenueAccount
{
	public ProfessionalRevenueAccount() {
		super();
	}

	/**
	 * @param accountTitle
	 *            Account's title, perhaps "Performance Coaching"
	 * @param feeName
	 *            Ledger name, typically "Consulting Fees"
	 */
	public ProfessionalRevenueAccount(String accountTitle, String feeName) {
		super(accountTitle);
		Ledger[] ledgers = {
				new CreditPositiveLedger(feeName), new CreditPositiveLedger("Expense Reimbursement")
		};
		for (int i = 0; i < ledgers.length; i++) {
			addLedger(ledgers[i]);
		}
	}

	public String getClassString() {
		return "Professional Services Revenue";
	}
}
