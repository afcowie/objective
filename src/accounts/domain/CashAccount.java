/*
 * CashAccount.java
 * 
 * See LICENCE file for usage and redistribution terms
 * Copyright (c) 2005 Operational Dynamics
 */
package accounts.domain;

/**
 * An asset account, implemented with a single Ledger.
 * 
 * @author Andrew Cowie
 */
public class CashAccount extends AssetAccount implements SingleLedger
{
	/**
	 * This is a convenience only for use in single ledger accounts, ie, this
	 * one.
	 */
	private DebitPositiveLedger	_ledger	= null;

	public CashAccount() {
		super();
	}

	/**
	 * Create a new asset account with a single ledger.
	 * 
	 * @param code
	 * @param title
	 */
	public CashAccount(String accountTitle, String ledgerName) {
		super(accountTitle);
		_ledger = new DebitPositiveLedger();
		_ledger.setName(ledgerName);
		addLedger(_ledger);
	}

	/**
	 * Add an entry to the (single) Ledger of this CashAccount.
	 */
	public void addEntry(Entry entry) {
		_ledger.addEntry(entry);
		entry.setParentAccount(this);
		// TODO recalc account balance?
	}

	/*
	 * Getters and Setters --------------------------------
	 */

	public Ledger getLedger() {
		return _ledger;
	}

	public void setLedger(Ledger ledger) {
		if (!(ledger instanceof DebitPositiveLedger)) {
			throw new IllegalArgumentException("You must use a DebitPositiveLedger for a CashAccount");
		}
		_ledger = (DebitPositiveLedger) ledger;
	}

	public String getClassString() {
		return "Cash";
	}
}
