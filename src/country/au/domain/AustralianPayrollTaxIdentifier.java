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
	public AustralianPayrollTaxIdentifier(String description) {
		super(description);
	}

	/**
	 * A set of useful "constants" indicating previsualized uses of this class.
	 * They are NOT instantiated here; rather, they are populated if the
	 * appropriate Finder runs.
	 */
	public static AustralianPayrollTaxIdentifier	TAXFREE_THRESHOLD_WITH_LEAVE_LOADING	= null;
	public static AustralianPayrollTaxIdentifier	TAXFREE_THRESHOLD_NO_LEAVE_LOADING		= null;
	public static AustralianPayrollTaxIdentifier	NO_TAXFREE_THRESHOLD					= null;
	public static AustralianPayrollTaxIdentifier	NO_TFN_PROVIDED							= null;

}