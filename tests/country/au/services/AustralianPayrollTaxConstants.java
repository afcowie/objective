/*
 * AustralianPayrollTaxConstants.java
 * 
 * See LICENCE file for usage and redistribution terms
 * Copyright (c) 2006 Operational Dynamics
 */
package country.au.services;

import generic.persistence.DataClient;

import java.util.List;

import country.au.domain.AustralianPayrollTaxIdentifier;

/**
 * A utility class to load the constants on AustralianPayrollTaxIdentifier. It's
 * for unit tests and demo setup only - the one place you should NOT use this
 * from is the actual application.
 * <code>new AustralianPayrollTaxConstants(db).loadIdentifiers();</code> ought
 * to be sufficient.
 * 
 * @author Andrew Cowie
 */
public class AustralianPayrollTaxConstants
{
	private DataClient	db;

	public AustralianPayrollTaxConstants(DataClient db) {
		this.db = db;
	}

	/**
	 * Reload the static identifiers on AustralianPayrollTaxIdentifier.
	 */
	public void loadIdentifiers() throws IllegalStateException {
		AustralianPayrollTaxIdentifier.NO_TAXFREE_THRESHOLD = fetchIdentifier(1);
		AustralianPayrollTaxIdentifier.TAXFREE_THRESHOLD_WITH_LEAVE_LOADING = fetchIdentifier(2);
		AustralianPayrollTaxIdentifier.FOREIGN_RESIDENT = fetchIdentifier(3);
		AustralianPayrollTaxIdentifier.NO_TFN_PROVIDED = fetchIdentifier(4);
		AustralianPayrollTaxIdentifier.TAXFREE_THRESHOLD_NO_LEAVE_LOADING = fetchIdentifier(7);
	}

	/**
	 * Fetch the AustralianPayrollTaxIdentifier corresponding to index i.
	 */
	private AustralianPayrollTaxIdentifier fetchIdentifier(int i) throws IllegalStateException {
		AustralianPayrollTaxIdentifier proto = new AustralianPayrollTaxIdentifier();
		proto.setIndex(i);

		List result = db.queryByExample(proto);
		if (result.size() != 1) {
			throw new IllegalStateException(
				"No (or many!) Identifier(s) found for index "
					+ i
					+ ", rather than 1. "
					+ "This probably means that there is a misalignment between AustralianPayrollTaxIdentifier, StoreAustralianPayrollTaxTablesCommand and AustralianInitBooksCommand. "
					+ "As the index is only for debug reloading, this isn't critical but needs to be fixed so that the unit test can pass.");
		}

		return (AustralianPayrollTaxIdentifier) result.get(0);
	}
}
