/*
 * CreditPositiveLedger.java
 * 
 * See LICENCE file for usage and redistribution terms
 * Copyright (c) 2005-2006 Operational Dynamics
 */
package accounts.domain;

/**
 * An accountign ledger which is credit positive, ie the accumulated
 * depreciation of a fixed asset, or the a ledger in an accounts payable
 * account.
 * 
 * @author Andrew Cowie
 */
public class CreditPositiveLedger extends Ledger
{
    public CreditPositiveLedger() {
        super();
    }

    public CreditPositiveLedger(String name) {
        super();
        super.setName(name);
    }

    /*
     * Overrides Ledger's addToBalance(). balance from Ledger.
     */
    protected void addToBalance(Entry entry) {
        if (entry == null) {
            throw new IllegalArgumentException("Can't add null Entry to a Ledger");
        }
        if (entry instanceof Credit) {
            balance.incrementBy(entry.getAmount());
        } else {
            balance.decrementBy(entry.getAmount());
        }
    }

    protected void subtractFromBalance(Entry entry) {
        if (entry == null) {
            throw new IllegalArgumentException("Can't subtract null Entry from a Ledger");
        }
        if (entry instanceof Credit) {
            balance.decrementBy(entry.getAmount());
        } else {
            balance.incrementBy(entry.getAmount());
        }
    }

    public String getClassString() {
        return "Credit Positive";
    }
}
