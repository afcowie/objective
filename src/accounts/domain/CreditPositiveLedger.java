/*
 * CreditPositiveLedger.java
 * 
 * See LICENCE file for usage and redistribution terms
 * Copyright (c) 2005 Operational Dynamics
 */
package accounts.domain;

/**
 * An accountign ledger which is credit positive, ie the accumulated
 * depreciation of a fixed asset, or the a ledger in an accounts payable
 * account.
 * 
 * @author Andrew Cowie
 */
public class CreditPositiveLedger extends Ledger
{
	public CreditPositiveLedger() {
		super();
	}
	
	public CreditPositiveLedger(String name) {
		super();
		super.setName(name);
	}
	
	public String getClassString() {
		return "Credit Positive";
	}
}