/*
 * TaxCalculator.java
 * 
 * See LICENCE file for usage and redistribution terms
 * Copyright (c) 2006 Operational Dynamics
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
