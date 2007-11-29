/*
 * AssetAccount.java
 * 
 * See LICENCE file for usage and redistribution terms
 * Copyright (c) 2005 Operational Dynamics
 */
package accounts.domain;

/**
 * An asset account.
 */
public class AssetAccount extends DebitPositiveAccount
{
    public AssetAccount() {
        super();
    }

    public AssetAccount(String title) {
        super(title);
    }

    public String getClassString() {
        return "Asset";
    }
}
