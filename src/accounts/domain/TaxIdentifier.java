/*
 * TaxIdentifier.java
 * 
 * See LICENCE file for usage and redistribution terms
 * Copyright (c) 2006 Operational Dynamics
 */
package accounts.domain;

/**
 * Simple hierarchy describing different tax scales & rates to be used.
 * 
 * @author Andrew Cowie
 */
public class TaxIdentifier extends Identifier
{
	public TaxIdentifier(String description) {
		super(description);
	}

	public String getClassString() {
		return "Tax Scale Identifier";
	}
}
