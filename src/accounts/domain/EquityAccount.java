/*
 * EquityAccount.java
 * 
 * See LICENCE file for usage and redistribution terms
 * Copyright (c) 2005 Operational Dynamics
 */
package accounts.domain;

/**
 * An Equity Account.
 * 
 * @author Andrew Cowie
 */
public class EquityAccount extends CreditPositiveAccount
{
    public EquityAccount() {
        super();
    }

    public EquityAccount(String title) {
        super(title);
    }

    public String getClassString() {
        return "Equity";
    }
}
