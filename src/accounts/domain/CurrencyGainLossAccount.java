/*
 * CurrencyGainLossAccount.java
 * 
 * See LICENCE file for usage and redistribution terms
 * Copyright (c) 2005 Operational Dynamics
 */
package accounts.domain;

/**
 * A single ledger account to record currency gains and losses. TODO Should
 * this in fact be a multi ledger account, with one ledger per currency? En
 * verra.
 * 
 * @author Andrew Cowie
 */
public class CurrencyGainLossAccount extends RevenueAccount implements SingleLedger
{
    private CreditPositiveLedger ledger = null;

    public CurrencyGainLossAccount() {
        super();
    }

    public CurrencyGainLossAccount(String title) {
        super(title);
        ledger = new CreditPositiveLedger();
        addLedger(ledger);
    }

    public void addEntry(Entry entry) {
        ledger.addEntry(entry);
        entry.setParentLedger(ledger);
        // TODO recalc account balance?
    }

    public Ledger getLedger() {
        return ledger;
    }

    public void setLedger(Ledger ledger) {
        if (!(ledger instanceof CreditPositiveLedger)) {
            throw new IllegalArgumentException(
                    "You must use a CreditPositiveLedger for an CurrencyGainLossAccount");
        }
        ledger = (CreditPositiveLedger) ledger;
    }

    public String getClassString() {
        return "Currency Gain/Loss Account";
    }
}
