/*
 * ItemsLedger.java
 * 
 * See LICENCE file for usage and redistribution terms
 * Copyright (c) 2005-2006 Operational Dynamics
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
