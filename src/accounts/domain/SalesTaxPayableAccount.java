/*
 * SalesTaxPayableAccount.java
 * 
 * See LICENCE file for usage and redistribution terms
 * Copyright (c) 2005-2006 Operational Dynamics
 */
package accounts.domain;

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

    public SalesTaxPayableAccount() {
        super();
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
        super(title);
        addLedger(new CreditPositiveLedger("Collected"));
        addLedger(new DebitPositiveLedger("Paid"));
    }

    public String getClassString() {
        return "Sales Tax Payable";
    }
}
