/*
 * PayrollTaxIdentifier.java
 * 
 * See LICENCE file for usage and redistribution terms
 * Copyright (c) 2006 Operational Dynamics
 */
package accounts.domain;

public class PayrollTaxIdentifier extends TaxIdentifier
{
	public PayrollTaxIdentifier(String description) {
		super(description);
	}

	public String getClassString() {
		return "Payroll Tax Scale Identifier";
	}
}
