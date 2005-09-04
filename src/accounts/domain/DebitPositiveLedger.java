/*
 * DebitPositiveLedger.java
 * 
 * See LICENCE file for usage and redistribution terms
 * Copyright (c) 2005 Operational Dynamics
 */
package accounts.domain;

/**
 * An accountign ledger which is debit positive, ie a ledger representing a bank
 * account or an expense.
 * 
 * @author Andrew Cowie
 */
public class DebitPositiveLedger extends Ledger
{
	public DebitPositiveLedger() {
		super();
	}
	
	public DebitPositiveLedger(String name) {
		super();
		super.setName(name);
	}
	
	public String getClassString() {
		return "Debit Positive";
	}
}