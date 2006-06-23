/*
 * OwnersEquityAccount.java
 * 
 * See LICENCE file for usage and redistribution terms
 * Copyright (c) 2005 Operational Dynamics
 */
package accounts.domain;

/**
 * A single ledger account for use in sole proprietorships. (ie, we use this for
 * testing :))
 * 
 * @author Andrew Cowie
 */
public class OwnersEquityAccount extends EquityAccount implements SingleLedger
{
	private CreditPositiveLedger	ledger	= null;

	/**
	 * 
	 */
	public OwnersEquityAccount() {
		super();
	}

	/**
	 * @param accountTitle
	 * @param ledgerName
	 */
	public OwnersEquityAccount(String accountTitle, String ledgerName) {
		super(accountTitle);
		ledger = new CreditPositiveLedger();
		ledger.setName(ledgerName);
		addLedger(ledger);
	}

	/**
	 * Add an entry to the single Ledger in this Account.
	 */
	public void addEntry(Entry entry) {
		ledger.addEntry(entry);
		entry.setParentLedger(ledger);
		// TODO recalc account balance?
	}

	public Ledger getLedger() {
		return ledger;
	}

	public void setLedger(Ledger ledger) {
		if (!(ledger instanceof CreditPositiveLedger)) {
			throw new IllegalArgumentException(
				"You must use a CreditPositiveLedger for an OwnersEquityAccount");
		}
		this.ledger = (CreditPositiveLedger) ledger;
	}

	public String getClassString() {
		return "Owner's Equity";
	}
}
