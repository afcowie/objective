/*
 * BankAccount.java
 * 
 * See LICENCE file for usage and redistribution terms
 * Copyright (c) 2005 Operational Dynamics
 */
package accounts.domain;

/**
 * A bank account. This is more complicated than a simple cash account because
 * it implements a reconcilable ledger. Class description here.
 * 
 * @author Andrew Cowie
 */
public class BankAccount extends AssetAccount implements SingleLedger
{
	// private String _institution = null;
	// private String _name = null;

	/**
	 * This is a convenience only for use in single ledger accounts, ie, this
	 * one.
	 */
	private DebitPositiveLedger	_ledger	= null;

	public BankAccount() {
		super();
	}

	/**
	 * 
	 * @param code
	 *            the numerical account code.
	 * @param institution
	 *            the name of the bank of financial institution carrying the
	 *            account. Becomes Account title.
	 * @param name
	 *            the type or name of the account, for instance "Current
	 *            Account". Used as Ledger name.
	 */
	public BankAccount(String institution, String name) {
		super();
		this.setTitle(institution);
		// _institution = institution;
		// _name = name;

		_ledger = new DebitPositiveLedger();
		_ledger.setName(name);
		addLedger(_ledger);
	}

	/**
	 * Add an entry to the (single) Ledger of this BankAccount. FIXME
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
		if (ledger instanceof DebitPositiveLedger) {
			_ledger = (DebitPositiveLedger) ledger;
		} else {
			throw new IllegalArgumentException("ledger argument needs ot be DebitPositive");
		}
	}

	public String getClassString() {
		return "Bank Account";
	}
}
