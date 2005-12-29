/*
 * LoanLedger.java
 * 
 * See LICENCE file for usage and redistribution terms
 * Copyright (c) 2005 Operational Dynamics
 */
package accounts.domain;

import java.util.Set;

/**
 * This provides a CreditPositiveLedger which implements ItemsLedger
 */
public class LoanLedger extends CreditPositiveLedger implements ItemsLedger
{
	protected Set	loans	= null;

	public LoanLedger() {
		super();
	}

	public LoanLedger(String name) {
		super(name);
	}

	// public LoanLedger(String name, Loan[] loans) {
	// super(name);
	// if (loans == null) {
	// throw new IllegalArgumentException("need to specify an array of Loan
	// objects with this constructor");
	// }
	// this.loans = loans;
	// }

	/*
	 * Implementation of inhereted abstract methods -------
	 */

	public Set getItems() {
		return loans;
	}

	public void setItems(Set items) {
		loans = items;
	}

	public String getClassString() {
		return "Loan Ledger";
	}

}
