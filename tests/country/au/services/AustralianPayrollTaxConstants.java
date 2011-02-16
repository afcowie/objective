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

import java.util.List;

import country.au.domain.AustralianPayrollTaxIdentifier;

/**
 * A utility class to load the constants on AustralianPayrollTaxIdentifier.
 * It's for unit tests and demo setup only - the one place you should NOT use
 * this from is the actual application.
 * <code>new AustralianPayrollTaxConstants(db).loadIdentifiers();</code>
 * ought to be sufficient.
 * 
 * @author Andrew Cowie
 */
public class AustralianPayrollTaxConstants
{
    private DataClient db;

    public AustralianPayrollTaxConstants(DataClient db) {
        this.db = db;
    }

    /**
     * Reload the static identifiers on AustralianPayrollTaxIdentifier.
     */
    public void loadIdentifiers() throws IllegalStateException {
        AustralianPayrollTaxIdentifier.NO_TAXFREE_THRESHOLD = fetchIdentifier(1);
        AustralianPayrollTaxIdentifier.TAXFREE_THRESHOLD_WITH_LEAVE_LOADING = fetchIdentifier(2);
        AustralianPayrollTaxIdentifier.FOREIGN_RESIDENT = fetchIdentifier(3);
        AustralianPayrollTaxIdentifier.NO_TFN_PROVIDED = fetchIdentifier(4);
        AustralianPayrollTaxIdentifier.TAXFREE_THRESHOLD_NO_LEAVE_LOADING = fetchIdentifier(7);
    }

    /**
     * Fetch the AustralianPayrollTaxIdentifier corresponding to index i.
     */
    private AustralianPayrollTaxIdentifier fetchIdentifier(int i) throws IllegalStateException {
        AustralianPayrollTaxIdentifier proto = new AustralianPayrollTaxIdentifier();
        proto.setIndex(i);

        List result = db.queryByExample(proto);
        if (result.size() != 1) {
            throw new IllegalStateException(
                    "No (or many!) Identifier(s) found for index "
                            + i
                            + ", rather than 1. "
                            + "This probably means that there is a misalignment between AustralianPayrollTaxIdentifier, StoreAustralianPayrollTaxTablesCommand and AustralianInitBooksCommand. "
                            + "As the index is only for debug reloading, this isn't critical but needs to be fixed so that the unit test can pass.");
        }

        return (AustralianPayrollTaxIdentifier) result.get(0);
    }
}
