/*
 * GenericTransaction.java
 * 
 * See LICENCE file for usage and redistribution terms
 * Copyright (c) 2005-2006 Operational Dynamics
 */
package accounts.domain;

/**
 * A generic transaction in the "general ledger" as traditional accounting
 * systems would call it. Simply consists of a balanced set of Entries, and does
 * not link to any special user interface.
 * <P>
 * This would have been called GeneralLedgerTransaction but ther was potential
 * for naming confustion as we have modelled the actual ledger(s) in any given
 * account with the Ledger class. This is most assuredly an accounting
 * Transaction.
 * 
 * @author Andrew Cowie
 */
public class GenericTransaction extends Transaction
{
	/**
	 * Default constructor, for searching.
	 */
	public GenericTransaction() {
		super();
	}

	/**
	 * Somewhat artificial constructor useful for mockups and unit tests
	 */
	public GenericTransaction(String description, Datestamp date, Entry[] entries) {
		super(description, date, entries);
	}

	public String getClassString() {
		return "Generic Transaction";
	}
}