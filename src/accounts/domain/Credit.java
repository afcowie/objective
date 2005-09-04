/*
 * Credit.java
 * 
 * See LICENCE file for usage and redistribution terms
 * Copyright (c) 2005 Operational Dynamics
 */
package accounts.domain;

/**
 * A credit positive ledger entry.
 * 
 * @author Andrew Cowie
 */
public class Credit extends Entry
{
//	public static final String	COLOR	= "medium sea green";
	public static final String	COLOR	= "sea green";

	public Credit() {
		super();
		/*
		 * an actual null Entry...
		 */
	}
	
	public Credit(Amount amount) {
		super(amount);
	}
}
