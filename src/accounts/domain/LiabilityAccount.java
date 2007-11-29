/*
 * LiabilityAccount.java
 * 
 * See LICENCE file for usage and redistribution terms
 * Copyright (c) 2005 Operational Dynamics
 */
package accounts.domain;

/**
 * A liability acount.
 * 
 * @author Andrew Cowie
 */
public class LiabilityAccount extends CreditPositiveAccount
{
    public LiabilityAccount() {
        super();
    }

    public LiabilityAccount(String title) {
        super(title);
    }

    public String getClassString() {
        return "Liability";
    }
}
