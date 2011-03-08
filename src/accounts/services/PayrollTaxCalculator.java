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
package accounts.services;

import objective.domain.Datestamp;
import accounts.domain.Amount;

/**
 * Work out the payroll tax or other emploment related deductions appropriate
 * to a given salary or paycheck receieved. Either salary or paycheck
 * withholding can be worked out given the other. There are two [abstract] use
 * cases that subclasses need to implement where the actual calculation work
 * should be done.
 * 
 * @author Andrew Cowie
 */
public abstract class PayrollTaxCalculator extends TaxCalculator
{
    protected transient Amount salary;

    protected transient Amount withhold;

    protected transient Amount paycheck;

    /**
     * @param asAtDate
     *            see {@link TaxCalculator#TaxCalculator(Datestamp)}
     */
    protected PayrollTaxCalculator(Datestamp asAtDate) {
        super(asAtDate);
    }

    public Amount getSalary() {
        return salary;
    }

    /**
     * Set the salary the Employee was given.
     * 
     * @param salary
     *            The positive Amount from which the necessary deduction will
     *            be worked out.
     */
    public void setSalary(Amount salary) {
        if (salary == null) {
            throw new IllegalArgumentException("Can't use null as the salary.");
        }
        if (salary.getNumber() < 0) {
            throw new IllegalArgumentException("Can't set a negative value as the salary.");
        }
        if (salary == paycheck) {
            throw new IllegalArgumentException(
                    "You can't use the same Amount object for paycheck and salary fields.");
        }

        this.salary = salary;
    }

    public Amount getPaycheck() {
        return paycheck;
    }

    /**
     * Set the paycheck the Employee actually received.
     * 
     * @param paid
     *            The positive Amount which is the result of subtracting
     *            neccessary withholding taxes from the salary an Employee is
     *            given. Note that this must be a different object from the
     *            salary Amount.
     */
    public void setPaycheck(Amount paid) {
        if (paid == null) {
            throw new IllegalArgumentException("Can't use null as the paid amount.");
        }
        if (paid.getNumber() < 0) {
            throw new IllegalArgumentException("Can't set a negative value as the paid amount.");
        }
        if (paid == salary) {
            throw new IllegalArgumentException(
                    "You can't use the same Amount object for paycheck and salary fields.");
        }

        this.paycheck = paid;
    }

    /**
     * Get the Amount to withhold, assuming it has been calculated.
     */
    public Amount getWithhold() {
        return withhold;
    }

    /**
     * For editing a transaction whose Entries were calculated using a
     * PayrollTaxCalculator you need to be able to set the withhold object as
     * well.
     * 
     * @param withhold
     *            the Amount object. An acceptable usage would be
     *            <code>new Amount()</code>.
     */
    public void setWithhold(Amount withhold) {
        if (withhold == null) {
            throw new IllegalArgumentException("Can't use null as the withhold Amount.");
        }
        /*
         * don't need the -ve test
         */
        if ((withhold == salary) || (withhold == paycheck)) {
            throw new IllegalArgumentException(
                    "You can't use the same Amount object for withhold, paycheck and salary fields.");
        }
        this.withhold = withhold;
    }

    public abstract void calculateGivenPayable();

    public abstract void calculateGivenSalary();

}
