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
 * contacted through http://research.operationaldynamics.com/projects/objective/.
 */
package accounts.domain;

import java.util.Set;

/**
 * Marks a ledger as hosting indiviudal items to which negating payments must
 * be applied. Examples are:
 * <UL>
 * <LI>Invoices in an AccountsReceivableLedger, which result from Revenue
 * transactions.
 * <LI>Bills in an AccountsPayableLedger, which result from Expense
 * transactions.
 * <LI>Loans in a LoanLedger
 * </UL>
 * In each case, when an invoice is raised, payments are made against that
 * invoice until it is paid off. TODO, work out the use cases!
 * 
 */
public interface ItemsLedger
{
    /**
     * @return a Set of Item objects
     */
    public Set getItems();

    public void setItems(Set items);
}
