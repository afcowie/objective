/*
 * CreditPositiveAccount.java
 * 
 * See LICENCE file for usage and redistribution terms
 * Copyright (c) 2005 Operational Dynamics
 */
package accounts.domain;

/**
 * An account which is credit positive, ie a liability, equity or revenue
 * account.
 * 
 * @author Andrew Cowie
 */
public class CreditPositiveAccount extends Account
{
    /*
     * Would have been abstract, except that having it concrete allows for
     * testing and searching with null prototypes.
     */

    public CreditPositiveAccount() {
        super();
    }

    public CreditPositiveAccount(String title) {
        super(title);
    }

    public boolean isDebitPositive() {
        return false;
    }

    public boolean isCreditPositive() {
        return true;
    }

    public String getClassString() {
        return "Credit Positive Account";
    }
}
