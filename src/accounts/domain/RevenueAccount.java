/*
 * RevenueAccount.java
 * 
 * See LICENCE file for usage and redistribution terms
 * Copyright (c) 2005 Operational Dynamics
 */
package accounts.domain;

/**
 * A revenue account.
 * 
 * @author Andrew Cowie
 */
public class RevenueAccount extends CreditPositiveAccount
{
    public RevenueAccount() {
        super();
    }

    public RevenueAccount(String title) {
        super(title);
    }

    public String getClassString() {
        return "Revenue";
    }
}
