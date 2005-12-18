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
	private DebitPositiveLedger	ledger	= null;

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
		ledger = new DebitPositiveLedger();
		ledger.setName(ledgerName);
		addLedger(ledger);
	}

	/**
	 * Add an entry to the (single) Ledger of this CashAccount.
	 */
	public void addEntry(Entry entry) {
		ledger.addEntry(entry);
		entry.setParentLedger(ledger);
		// TODO recalc account balance?
	}

	/*
	 * Getters and Setters --------------------------------
	 */

	public Ledger getLedger() {
		return ledger;
	}

	public void setLedger(Ledger ledger) {
		if (!(ledger instanceof DebitPositiveLedger)) {
			throw new IllegalArgumentException("You must use a DebitPositiveLedger for a CashAccount");
		}
		this.ledger = (DebitPositiveLedger) ledger;
	}

	public String getClassString() {
		return "Cash";
	}
}
