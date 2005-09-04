/*
 * DepreciatingAssetAccount.java
 * 
 * See LICENCE file for usage and redistribution terms
 * Copyright (c) 2005 Operational Dynamics
 */
package accounts.domain;

/**
 * A revenue account specific to consulting services. It has two built in
 * Ledgers: one for consulting fee revenue, and one representing received
 * expense reimbursement payments.
 * 
 * @author Andrew Cowie
 */
public class DepreciatingAssetAccount extends AssetAccount
{
	public DepreciatingAssetAccount() {
		super();
	}

	public DepreciatingAssetAccount(String title) {
		super(title);
		addLedger(new DebitPositiveLedger("At Cost"));
		addLedger(new CreditPositiveLedger("Accumulated Depreciation"));
	}

	public String getClassString() {
		return "Depreciating Asset";
	}
}
