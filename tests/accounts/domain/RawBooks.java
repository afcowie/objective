/*
 * RawBooks.java
 * 
 * See LICENCE file for usage and redistribution terms
 * Copyright (c) 2005 Operational Dynamics
 */
package accounts.domain;

import java.util.LinkedHashSet;

/**
 * Establish a set of raw books suitable for test cases.
 * 
 * @author Andrew Cowie
 */
public class RawBooks extends Books
{
    public RawBooks() {
        accounts = new LinkedHashSet();

        CurrencyGainLossAccount gainLoss = new CurrencyGainLossAccount();
        gainLoss.setTitle("Currency Gain/Loss");
        super.setGainLossAccount(gainLoss);
    }

    // /**
    // * @param assets
    // * The assets to set.
    // */
    // public void setAssetAccounts(Set assets) {
    // _accounts.addAll(assets);
    // }
    //
    // /**
    // * @param equity
    // * The equity to set.
    // */
    // public void setEquityAcounts(Set equity) {
    // _accounts.addAll(equity);
    // }
    //
    //
    // /**
    // * @param liabilities
    // * The liabilities to set.
    // */
    // public void setLiabilityAccounts(Set liabilities) {
    // _accounts.addAll(liabilities);
    // }
    //	
    // /**
    // * @param revenues
    // * The revenues to set.
    // */
    // public void setRevenueAccounts(Set revenues) {
    // _accounts.addAll(revenues);
    // }
    //
    // /**
    // * @param expenses
    // * The expenses to set.
    // */
    // public void setExpenseAccounts(Set expenses) {
    // _accounts.addAll(expenses);
    // }
}
