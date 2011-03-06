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
package objective.domain;

/**
 * A sales tax liability resulting from both collecting and paying a tax which
 * revolve through the same account. We use two (automatically created)
 * ledgers to record it. In the example of Australian GST, GST is added to and
 * collected from sales, whereas GST paid on purchases (expenses) is
 * redeemable. When we do a BAS Transaction, we can net these, and move the
 * amount either to Accounts Reivable if they owe us, or write a cheque and
 * send off what we owe them.
 * 
 * @author Andrew Cowie *
 * @see accounts.domain.PayrollTaxPayableAccount for the parallel account type
 *      used for collecting taxes that must simply be remitted to the Receiver
 *      General for that jurisdiction, such as PAYG.
 */
public class SalesTaxPayableAccount extends LiabilityAccount
{

    public SalesTaxPayableAccount(long rowid) {
        super(rowid);
    }

    /**
     * Creates a new Tax account with automatically created "Collected" and
     * "Paid" ledgers.
     * 
     * @param title
     *            The name of the tax, to be used as the account title, ie
     *            "GST"
     */
    public SalesTaxPayableAccount(String title) {
        super(0);
        throw new UnsupportedOperationException();
    }

    public String getClassString() {
        return "Sales Tax Payable";
    }
}
