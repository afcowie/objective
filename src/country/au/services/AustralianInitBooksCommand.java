/*
 * ObjectiveAccounts, accounting for small professional services firms.
 *
 * Copyright © 2006-2011 Operational Dynamics Consulting, Pty Ltd
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
         * Initialize the Australia specific Identifiers. The index used
         * corresponds to the "scale" as named in the tax table data document
         */
        AustralianPayrollTaxIdentifier.NO_TAXFREE_THRESHOLD = new AustralianPayrollTaxIdentifier(
                "No tax-free threshold", 1);
        AustralianPayrollTaxIdentifier.TAXFREE_THRESHOLD_WITH_LEAVE_LOADING = new AustralianPayrollTaxIdentifier(
                "Tax-free threshold and leave loading claimed", 2);
        AustralianPayrollTaxIdentifier.FOREIGN_RESIDENT = new AustralianPayrollTaxIdentifier(
                "Foreign resident", 3);
        AustralianPayrollTaxIdentifier.NO_TFN_PROVIDED = new AustralianPayrollTaxIdentifier(
                "No TFN (or ABN) quoted", 4);
        AustralianPayrollTaxIdentifier.TAXFREE_THRESHOLD_NO_LEAVE_LOADING = new AustralianPayrollTaxIdentifier(
                "Tax-free threshold claimed but without leave loading", 7);

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
         * Now add the PAYG identifers. Order here is some notion of most
         * used. Not really that important.
         */
        IdentifierGroup grp = new IdentifierGroup("PAYG witholding types");
        grp.addIdentifier(AustralianPayrollTaxIdentifier.NO_TAXFREE_THRESHOLD);
        grp.addIdentifier(AustralianPayrollTaxIdentifier.TAXFREE_THRESHOLD_WITH_LEAVE_LOADING);
        grp.addIdentifier(AustralianPayrollTaxIdentifier.TAXFREE_THRESHOLD_NO_LEAVE_LOADING);
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
