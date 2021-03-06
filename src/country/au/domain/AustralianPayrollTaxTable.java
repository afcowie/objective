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

import objective.domain.Datestamp;
import generic.domain.Cascade;
import generic.util.DebugException;

/**
 * A simple table to store Australian Tax Office Pay-As-You-Go tax data.
 * <p>
 * Some assumptions:
 * <ul>
 * <li>When a new tax table comes out it superceeds the previous one (ie,
 * there's only one active at a time).
 * <li>The internal data are three doubles per row: the up to but not
 * including salary per week, coefficient a, and coefficient b.
 * </ul>
 * This class and the stored instance data will need to be evolved if the ATO
 * starts using a new algorithm for PAYG calculations.
 * 
 * @author Andrew Cowie
 * @see country.au.services.AustralianPayrollTaxCalculator
 */
public class AustralianPayrollTaxTable implements Cascade
{
    protected AustralianPayrollTaxIdentifier scale;

    protected Datestamp effective;

    private double[][] data;

    private transient int size = -1;

    private transient int i;

    /**
     * @param identifier
     *            The AustralianPayrollTaxIdentifier which specifies which
     *            witholding type this is.
     * @param effective
     *            The Datestamp of what date this table takes effect.
     * @param size
     *            The number of coefficients entries that will be in this
     *            table. Used to size the arrays and to check you've filled in
     *            as many as you expect to fill in.
     */
    public AustralianPayrollTaxTable(AustralianPayrollTaxIdentifier identifier, Datestamp effective,
            int size) {
        if (size < 1) {
            throw new IllegalArgumentException(
                    "Coefficients must be constructed to have at least one row");
        }
        this.scale = identifier;
        this.effective = effective;
        this.size = size;
        this.i = 0;
        this.data = new double[size][3];
    }

    /**
     * Construct a search prototype.
     */
    public AustralianPayrollTaxTable(AustralianPayrollTaxIdentifier identifier) {
        this.scale = identifier;
    }

    /**
     * Each line of the table is a max salary (<=) and then the coefficients a
     * and b to be used at or below that point. The last entry should have
     * ceiling set to Double.MAX_VALUE as all potential salaries will be below
     * it.
     */
    public void addCoefficients(double ceiling, double a, double b) {
        if (i == -1) {
            throw new DebugException(
                    "Wrong constructor was used - can't addCoefficients() to a search prototype");
        }
        if (i == size) {
            throw new IllegalStateException("Can't add a row - this Coefficients is full");
        }
        if (i > 0) {
            if (ceiling < data[i - 1][0]) {
                throw new IllegalArgumentException(
                        "You must add coefficients in ascending ceiling order");
            }
        }
        data[i] = new double[] {
            ceiling,
            a,
            b
        };
        i++;
    }

    public AustralianPayrollTaxIdentifier getScale() {
        return scale;
    }

    public Datestamp getEffectiveDate() {
        return effective;
    }

    public double[][] getCoefficients() {
        return data;
    }

    public String getClassString() {
        return "Australian Payroll Tax Scales";
    }

    public String toString() {
        return scale.getName() + " " + effective;
    }
}
