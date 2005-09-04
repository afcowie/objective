/*
 * LoanPayableAccount.java
 * 
 * See LICENCE file for usage and redistribution terms
 * Copyright (c) 2005 Operational Dynamics
 */
package accounts.domain;

/**
 * An account containing loans. As designed, use a {@link LoanLedger} for each
 * person or entity making loans to the business, and then {@link Loan} for
 * individual loan tranches.
 */
public class LoanPayableAccount extends LiabilityAccount
{
	public LoanPayableAccount() {
		super();
	}

	public LoanPayableAccount(String title, LoanLedger[] loanLedgers) {
		super(title);

		for (int i = 0; i < loanLedgers.length; i++) {
			addLedger(loanLedgers[i]);
		}
	}

	public void addLedger(Ledger loanLedger) {
		if (!(loanLedger instanceof LoanLedger)) {
			throw new IllegalArgumentException(
					"When using LoanPayableAccount.addLedger(), the argument must be a LoanLedger (not merely a CreditPositiveLedger one)");
		}
		super.addLedger(loanLedger);
	}

	public String getClassString() {
		return "Loan Payable";
	}
}
