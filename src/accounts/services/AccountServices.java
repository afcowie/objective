/*
 * AccountServices.java
 * 
 * See LICENCE file for usage and redistribution terms
 * Copyright (c) 2005 Operational Dynamics
 */
package accounts.services;

/**
 * Facade to expose service layer for manipulating accounts. This will be static
 * until there's a reason for it not to be.
 * 
 * @author Andrew Cowie
 */
public class AccountServices
{

	public static void newAccount() {
		throw new UnsupportedOperationException();
	}

	/*
	 * TODO IDEA: updateAccount will cause balances to be recaluclated... ...
	 * updates to GUI outside of a single window will be after refresh to
	 * database?... or perhaps account balances will be updated on (and only on)
	 * Transaction commit?
	 */
	
}