/*
 * OwnersEquityAccount.java
 * 
 * See LICENCE file for usage and redistribution terms
 * Copyright (c) 2005 Operational Dynamics
 */
package accounts.domain;

/**
 * A single ledger account for use in sole proprietorships. (ie, we use this for testing :))
 * 
 * @author Andrew Cowie
 */
public class OwnersEquityAccount extends EquityAccount implements SingleLedger
{
	private CreditPositiveLedger _ledger = null;
	
	/**
	 * 
	 */
	public OwnersEquityAccount() {
		super();
	}

	/**
	 * @param code
	 * @param title
	 */
	public OwnersEquityAccount(String accountTitle, String ledgerName) {
		super(accountTitle);
		_ledger = new CreditPositiveLedger();
		_ledger.setName(ledgerName);
		addLedger(_ledger);
	}
	
	/**
	 * Add an entry to the single Ledger in this Account. 
	 */
	public void addEntry(Entry entry) {
		_ledger.addEntry(entry);
		entry.setParentAccount(this);
		// TODO recalc account balance?
	}

	public Ledger getLedger() {
		return _ledger;
	}

	public void setLedger(Ledger ledger) {
		if (!(ledger instanceof CreditPositiveLedger)) {
			throw new IllegalArgumentException("You must use a CreditPositiveLedger for an OwnersEquityAccount");
		}
		_ledger = (CreditPositiveLedger) ledger;
	}

	public String getClassString() {
		return "Owner's Equity";
	}
}
