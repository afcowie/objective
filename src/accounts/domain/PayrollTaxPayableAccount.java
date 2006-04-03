/*
 * PayrollTaxPayableAccount.java
 * 
 * See LICENCE file for usage and redistribution terms
 * Copyright (c) 2006 Operational Dynamics
 */
package accounts.domain;

/**
 * A tax liability, that we collect and then must remit. Has one automatically
 * created ledgers to record it. In the example of Australian PAYG, this is the
 * amount withheld from paychecks. When we do a BAS Transaction, we remit the
 * withholdings collected and zero the liability as we send off the cheque to
 * ATO.
 * 
 * @author Andrew Cowie
 * @see accounts.domain.SalesTaxPayableAccount for the parallel account type
 *      used for revolving (two sided) taxes such as GST.
 */
public class PayrollTaxPayableAccount extends LiabilityAccount
{

	public PayrollTaxPayableAccount() {
		super();
	}

	/**
	 * Creates a new Tax account with automatically created "Collected" ledger.
	 * 
	 * @param title
	 *            The name of the tax, to be used as the account title, ie
	 *            "PAYG"
	 */
	public PayrollTaxPayableAccount(String title) {
		super(title);
		addLedger(new CreditPositiveLedger("Collected"));
	}

	public String getClassString() {
		return "Payroll Tax Payable";
	}
}
