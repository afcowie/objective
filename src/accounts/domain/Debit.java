/*
 * Debit.java
 * 
 * See LICENCE file for usage and redistribution terms
 * Copyright (c) 2005 Operational Dynamics
 */
package accounts.domain;

/**
 * A debit positive entry in a ledger.
 * 
 * @author Andrew Cowie
 */
public class Debit extends Entry
{
	public static final String	COLOR	= "steel blue";
//	public static final String	COLOR	= "midnightblue";

	public Debit() {
		super();
		/*
		 * an actual null Entry...
		 */
	}
	
	public Debit(Amount amount) {
		super(amount);
	}
}
