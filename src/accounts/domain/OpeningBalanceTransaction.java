/*
 * OpeningBalanceTransaction.java
 * 
 * See LICENCE file for usage and redistribution terms
 * Copyright (c) 2005 Operational Dynamics
 */
package accounts.domain;

import java.util.Set;

/**
 * The transaction used to import a set of existing accounts. In essence it's
 * just a big general ledger entry, but a single GL transaction UI is really
 * terrible for entry of this data - rather, an aspect of an account is it's
 * openning balance, which is recorded here.
 * 
 * @author Andrew Cowie
 */
public class OpeningBalanceTransaction extends GenericTransaction
{
	private final static String		LABEL				= "Opening Balance";

	private transient EquityAccount	historicalBalance	= null;

	public OpeningBalanceTransaction() {
		super();
	}

	/**
	 * Create a new Opening Balance Transaction. This constructor assumes you
	 * have a Set of entries you wish to apply.
	 * 
	 * @param entries
	 * @param date
	 *            The date to use for the openning balance entries. Note that
	 *            this should probably be related to
	 *            {@link Books#_dateOfInception}
	 */
	public OpeningBalanceTransaction(Set entries, Datestamp date) {
		super(LABEL, entries);
		super.setDate(date);
	}

}