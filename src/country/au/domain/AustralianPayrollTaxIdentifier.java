/*
 * AustralianPayrollTaxIdentifier.java
 * 
 * See LICENCE file for usage and redistribution terms
 * Copyright (c) 2006 Operational Dynamics
 */
package country.au.domain;

import accounts.domain.PayrollTaxIdentifier;

/**
 * Different types of scale that a given Worker specifies to indicate which set
 * of withholding tables are to be used to calculate the PayAsYouGo withholding
 * to be deducted from their salary.
 * 
 * @author Andrew Cowie
 */
public class AustralianPayrollTaxIdentifier extends PayrollTaxIdentifier
{
	public AustralianPayrollTaxIdentifier(String description, int index) {
		super(description, index);
	}

	/**
	 * For searches
	 */
	public AustralianPayrollTaxIdentifier() {
		super();
	}

	/**
	 * A set of useful "constants" indicating previsualized uses of this class.
	 * They are NOT instantiated here. For any given DataClient, you can always
	 * initialize these with country.au.services.AustralianPayrollTaxConstants
	 */
	public static AustralianPayrollTaxIdentifier	NO_TAXFREE_THRESHOLD					= null;
	public static AustralianPayrollTaxIdentifier	TAXFREE_THRESHOLD_WITH_LEAVE_LOADING	= null;
	public static AustralianPayrollTaxIdentifier	FOREIGN_RESIDENT						= null;
	public static AustralianPayrollTaxIdentifier	NO_TFN_PROVIDED							= null;
	public static AustralianPayrollTaxIdentifier	TAXFREE_THRESHOLD_NO_LEAVE_LOADING		= null;
}