/*
 * ObjectiveAccounts, accounting for small professional services firms.
 *
 * Copyright © 2006-2011 Operational Dynamics Consulting, Pty Ltd
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
 * A tax liability, that we collect and then must remit. Has one automatically
 * created ledgers to record it. In the example of Australian PAYG, this is
 * the amount withheld from paychecks. When we do a BAS Transaction, we remit
 * the withholdings collected and zero the liability as we send off the cheque
 * to ATO.
 * 
 * Ledger should be "Collected". Account title should be the name of the tax,
 * to be used as the account title, ie "PAYG"
 * 
 * @author Andrew Cowie
 * @see objective.domain.SalesTaxPayableAccount for the parallel account type
 *      used for revolving (two sided) taxes such as GST.
 */
public class PayrollTaxPayableAccount extends LiabilityAccount
{
    public PayrollTaxPayableAccount(long rowid) {
        super(rowid);
    }

    public String getClassString() {
        return "Payroll Tax Payable";
    }
}
