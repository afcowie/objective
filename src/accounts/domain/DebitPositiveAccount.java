/*
 * CreditPositiveAccount.java
 * 
 * See LICENCE file for usage and redistribution terms
 * Copyright (c) 2005 Operational Dynamics
 */
package accounts.domain;

/**
 * An account which is Debit Positive.
 * 
 * @author Andrew Cowie
 */
public class DebitPositiveAccount extends Account
{
	/*
	 * Would have been abstract, except that having it concrete allows for
	 * testing and searching with null prototypes.
	 */

	public DebitPositiveAccount() {
		super();
	}

	public DebitPositiveAccount(String title) {
		super(title);
	}
	
	public String getClassString() {
		return "Debit Positive Account";
	}
}