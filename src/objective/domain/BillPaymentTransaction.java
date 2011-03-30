/*
 * ObjectiveAccounts, accounting for small professional services firms.
 *
 * Copyright © 2011 Operational Dynamics Consulting, Pty Ltd
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
 * A transaction that resuls from paying a bill.
 * 
 * @author Andrew Cowie
 * @see BillInvoiceTransaction
 */
public class BillPaymentTransaction extends PaymentTransaction
{
    public BillPaymentTransaction(long rowid) {
        super(rowid);
    }

    public String getClassString() {
        return "Bill Payment";
    }

    public long getType() {
        return -22;
    }
}
