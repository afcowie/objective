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
 * contacted through http://research.operationaldynamics.com/projects/objective/.
 */
package accounts.services;

import accounts.domain.Datestamp;

/**
 * Calculate taxes owing. Use cases include working out payroll tax and income
 * tax obligations. Taxes are calculated as at a specified date, which informs
 * the calculator which set of tax tables to use in its calculation.
 * 
 * @author Andrew Cowie
 */
public abstract class TaxCalculator
{
    protected Datestamp asAtDate = null;

    /**
     * @param asAtDate
     *            A Datestamp specifying which tax data to use.
     */
    protected TaxCalculator(Datestamp asAtDate) {
        if (asAtDate == null) {
            throw new IllegalArgumentException(
                    "Can't use a null Datestamp as the asAtDate in a TaxCalculator");
        }
        this.asAtDate = asAtDate;
    }

    /**
     * Get the Datestamp describing when you're trying to calculate some taxes
     * due. This is used to select which tax tables are used, for instance.
     */
    public Datestamp getAsAtDate() {
        return asAtDate;
    }
}
