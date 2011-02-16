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
package country.au.domain;

import accounts.domain.PayrollTaxIdentifier;

/**
 * Different types of scale that a given Worker specifies to indicate which
 * set of withholding tables are to be used to calculate the PayAsYouGo
 * withholding to be deducted from their salary.
 * 
 * @author Andrew Cowie
 */
public class AustralianPayrollTaxIdentifier extends PayrollTaxIdentifier
{
    public AustralianPayrollTaxIdentifier(String description, int index) {
        super(description, index);
    }

    /**
     * For searches
     */
    public AustralianPayrollTaxIdentifier() {
        super();
    }

    /**
     * A set of useful "constants" indicating previsualized uses of this
     * class. They are NOT instantiated here. For any given DataClient, you
     * can always initialize these with
     * country.au.services.AustralianPayrollTaxConstants
     */
    public static AustralianPayrollTaxIdentifier NO_TAXFREE_THRESHOLD = null;

    public static AustralianPayrollTaxIdentifier TAXFREE_THRESHOLD_WITH_LEAVE_LOADING = null;

    public static AustralianPayrollTaxIdentifier FOREIGN_RESIDENT = null;

    public static AustralianPayrollTaxIdentifier NO_TFN_PROVIDED = null;

    public static AustralianPayrollTaxIdentifier TAXFREE_THRESHOLD_NO_LEAVE_LOADING = null;
}
