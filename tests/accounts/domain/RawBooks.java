/*
 * ObjectiveAccounts, accounting for small professional services firms.
 *
 * Copyright © 2005-2011 Operational Dynamics Consulting, Pty Ltd
 *
 * The code in this file, and the program it is a part of, is made available
 * to you by its authors as open source software: you can redistribute it
 * and/or modify it under the terms of the GNU General Public License version
 * 2 ("GPL") as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GPL for more details.
 *
 * You should have received a copy of the GPL along with this program. If not,
 * see http://www.gnu.org/licenses/. The authors of this program may be
 * contacted via http://research.operationaldynamics.com/projects/objective/.
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
