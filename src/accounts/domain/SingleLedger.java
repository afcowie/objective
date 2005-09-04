/*
 * SingleLedger.java
 * 
 * See LICENCE file for usage and redistribution terms
 * Copyright (c) 2005 Operational Dynamics
 */
package accounts.domain;

/**
 * Marks an account which only has one Ledger in it. In addition to a few
 * convenience methods (to be coded in implementing classes) for accessing the
 * single Ledger, this is a marker interface to allow appopriate UI generation.
 * 
 * @author Andrew Cowie
 */
public interface SingleLedger
{
	/**
	 * Add an entry to the single Ledger in this Account. 
	 */
	public void addEntry(Entry entry);

	/**
	 * Get the [single] ledger associated with this account. 
	 */
	public Ledger getLedger();

	/**
	 * Set the [sole] ledger associated with this account.
	 */
	public void setLedger(Ledger ledger);
}
