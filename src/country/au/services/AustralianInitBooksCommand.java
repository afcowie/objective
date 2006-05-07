/*
 * AustralianInitBooksCommand.java
 * 
 * See LICENCE file for usage and redistribution terms
 * Copyright (c) 2006 Operational Dynamics
 */
package country.au.services;

import generic.persistence.DataClient;
import accounts.domain.Currency;
import accounts.domain.IdentifierGroup;
import accounts.services.CommandNotReadyException;
import accounts.services.InitBooksCommand;
import accounts.services.StoreIdentifierGroupCommand;
import country.au.domain.AustralianPayrollTaxIdentifier;

/**
 * Setup a set of books appropriate to an company domiciled in Australia.
 * 
 * @author Andrew Cowie
 */
public class AustralianInitBooksCommand extends InitBooksCommand
{
	/**
	 * Construct a Books, initializing Identifiers and setting currency
	 * appropriate to an Australian company.
	 */
	public AustralianInitBooksCommand() {
		super();

		home = new Currency("AUD", "Australian Dollar", "$");

		/*
		 * Initialize the Australia specific Identifiers.
		 */
		AustralianPayrollTaxIdentifier.NO_TAXFREE_THRESHOLD = new AustralianPayrollTaxIdentifier(
			"No tax-free threshold");
		AustralianPayrollTaxIdentifier.TAXFREE_THRESHOLD_NO_LEAVE_LOADING = new AustralianPayrollTaxIdentifier(
			"Tax-free threshold claimed but without leave loading");
		AustralianPayrollTaxIdentifier.TAXFREE_THRESHOLD_WITH_LEAVE_LOADING = new AustralianPayrollTaxIdentifier(
			"Tax-free threshold and leave loading claimed");
		AustralianPayrollTaxIdentifier.NO_TFN_PROVIDED = new AustralianPayrollTaxIdentifier("No TFN (or ABN) quoted");
		AustralianPayrollTaxIdentifier.FOREIGN_RESIDENT = new AustralianPayrollTaxIdentifier("Foreign resident");
	}

	/**
	 * The bulk of the implementation is in
	 * {@link InitBooksCommand#action(DataClient)}, which we call mid way
	 * through this method via super.action()
	 */
	protected void action(DataClient store) throws CommandNotReadyException {
		/*
		 * Execute the basic InitBooksCommand
		 */
		super.action(store);

		/*
		 * Now add the PAYG identifers
		 */
		IdentifierGroup grp = new IdentifierGroup("PAYG witholding types");
		grp.addIdentifier(AustralianPayrollTaxIdentifier.NO_TAXFREE_THRESHOLD);
		grp.addIdentifier(AustralianPayrollTaxIdentifier.TAXFREE_THRESHOLD_NO_LEAVE_LOADING);
		grp.addIdentifier(AustralianPayrollTaxIdentifier.TAXFREE_THRESHOLD_WITH_LEAVE_LOADING);
		grp.addIdentifier(AustralianPayrollTaxIdentifier.NO_TFN_PROVIDED);
		grp.addIdentifier(AustralianPayrollTaxIdentifier.FOREIGN_RESIDENT);

		StoreIdentifierGroupCommand sigc = new StoreIdentifierGroupCommand(grp);
		sigc.execute(store);

		/*
		 * Initialize the PAYG tables as are currently available.
		 */
		StoreAustralianPayrollTaxTablesCommand sapttc = new StoreAustralianPayrollTaxTablesCommand();
		sapttc.execute(store);
	}
}
