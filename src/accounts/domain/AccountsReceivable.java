/*
 * AccountsReceivable.java
 * 
 * See LICENCE file for usage and redistribution terms
 * Copyright (c) 2006 Operational Dynamics
 */
package accounts.domain;

/**
 * Trade Debetors. Holds a ledger per business Entity which owes us money.
 * Such debts presumably arise out of revenue generating transactions.
 * 
 * @author Andrew Cowie
 * @see accounts.domain.ItemsLedger
 * @see accounts.domain.AccountsPayable
 */
public class AccountsReceivable extends AssetAccount
{
    public AccountsReceivable() {
        super();
    }

    /**
     * 
     * @param title
     *            a title for the Accounts Receivable account. Australians
     *            probably prefer "Trade Debtors"
     */
    public AccountsReceivable(String title) {
        super(title);
    }

    public void addLedger(Ledger ledger) {
        if (!(ledger instanceof ClientLedger)) {
            throw new IllegalArgumentException(
                    "Ledgers added to AccountsReceivable accounts need to be ItemLedgers");
        }
        super.addLedger(ledger);
    }

    public String getClassString() {
        return "Accounts Receivable";
    }
}
