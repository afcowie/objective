/*
 * Credit.java
 * 
 * See LICENCE file for usage and redistribution terms
 * Copyright (c) 2005-2006 Operational Dynamics
 */
package accounts.domain;

/**
 * A credit positive ledger entry.
 * 
 * @author Andrew Cowie
 */
public class Credit extends Entry
{
	public static final String	COLOR_NORMAL	= "#0b8c06";
	public static final String	COLOR_ACTIVE	= "#a0ff97";

	public Credit() {
		super();
		/*
		 * an actual null Entry...
		 */
	}

	public Credit(Amount value, Ledger parent) {
		super(value, parent);
	}
}
