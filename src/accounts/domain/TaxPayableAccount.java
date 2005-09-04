/*
 * TaxPayableAccount.java
 * 
 * See LICENCE file for usage and redistribution terms
 * Copyright (c) 2005 Operational Dynamics
 */
package accounts.domain;

/**
 * A tax liability. This assumes that we both collect and pay tax which revolve
 * through the same account. We use two (automatically created) ledgers to
 * record it. In the example of Australian GST, GST is added to and collected
 * from sales, whereas GST paid on purchases (expenses) is redeemable. When we
 * do a BAS transaction, we can net these, and move the amount either to Accounts
 * Reivable if they owe us, or write a cheque and send off what we owe them.
 */
public class TaxPayableAccount extends LiabilityAccount
{

	public TaxPayableAccount() {
		super();
	}

	/**
	 * Creates a new Tax account with automatically created "Collected" and "Paid" ledgers. 
	 * @param title The name of the tax, to be used as the account title, ie "GST"  
	 */
	public TaxPayableAccount(String title) {
		super(title);
		addLedger(new CreditPositiveLedger("Collected"));
		addLedger(new DebitPositiveLedger("Paid"));
	}

	public String getClassString() {
		return "Tax Payable";
	}
}
