/*
 * ObjectiveAccounts, accounting for small professional services firms.
 *
 * Copyright © 2005-2011 Operational Dynamics Consulting, Pty Ltd
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
package accounts.domain;

import java.text.ParseException;

/**
 * An individual loan traunch. Especially in the case of Shareholder's Loans
 * under Australian law each transaction creates a new loan which has an
 * individual (though fixed 7 year) due date, with interest acruing (but not
 * necessarily payable annually, only at the end).
 * <P>
 * Internal parameters use the traditional Time Value of Money terms: an
 * interest rate per period, a number of periods, and specification of how
 * many periods there are per year.
 * <P>
 * If you need something more exotic, subclass this.
 * 
 */
public class Loan extends Item
{
    protected String description = null;

    protected Datestamp startDate = null;

    protected Datestamp dueDate = null;

    protected String interestRate = null;

    protected int numPeriods = 0;

    protected int periodsPerYear = 0;

    public Loan() {}

    public Loan(String description, Datestamp startDate, int periodsPerYear, int numPeriods) {
        setDescription(description);
        setStartDate(startDate);
        setPeriodsPerYear(periodsPerYear);
        setNumPeriods(numPeriods);
        calculateDueDate();
    }

    /*
     * Getters and Setters --------------------------------
     */

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Datestamp getStartDate() {
        return startDate;
    }

    public void setStartDate(Datestamp date) {
        this.startDate = date;
    }

    public Datestamp getDueDate() {
        return dueDate;
    }

    /**
     * You can set the due date arbitrarily; calling this method will
     * invalidate FIXME
     * 
     * @param date
     */
    public void setDueDate(Datestamp date) {
        this.dueDate = date;
    }

    public int getPeriodsPerYear() {
        return periodsPerYear;
    }

    public void setPeriodsPerYear(int periods) {
        if (periods < 0) {
            throw new IllegalArgumentException("The number of periods must be greater than zero");
        }
        if (periods > 365) {
            throw new IllegalArgumentException(
                    "Interest compounds faster than daily? You're kidding, right?");
        }
        this.periodsPerYear = periods;
    }

    public void setNumPeriods(int periods) {
        if (periods < 0) {
            throw new IllegalArgumentException("The number of periods must be greater than zero");
        }
        numPeriods = periods;
    }

    public int getNumPeriods() {
        return numPeriods;
    }

    /*
     * A convenince to set the number of periods if the number of years is
     * known, which is actually the common use case. Assumptions:
     * periodsPerYear must already be set, and the total number of periods
     * must fit into that many years without remainder (ie, it will be
     * calculated that way, so that should be what you mean).
     */
    public void setNumYears(int years) {
        if (years < 0) {
            throw new IllegalArgumentException("The number of years must be greater than zero");
        }
        if (periodsPerYear < 1) {
            throw new IllegalStateException(
                    "The number of periods per year must already be set to use setNumYears()");
        }
        numPeriods = years * periodsPerYear;
    }

    public String getIntersestRate() {
        return interestRate;
    }

    /**
     * Set the interest rate for the loan. The rate is in per-period terms.
     * <P>
     * Once validated, will be stored in a decimal form String (ie 0.095 for
     * 9.5%).
     * 
     * @param rate
     *            a string, either in the form "0.055" or "5.5%"
     */
    public void setInterestRate(String rate) {
        // TODO validate
        this.interestRate = rate;
    }

    /**
     * This is where the assumption that a month := year / 12 would creep in.
     * TODO probably needs leap year capability, etc... replace with Calendar
     * methods?
     */
    protected void calculateDueDate() {
        final long YEARMILLISECONDS = (1000l * 86400l * 365l);

        long startTimestamp = startDate.getInternalTimestamp();

        if (numPeriods == 0) {
            throw new IllegalStateException(
                    "Not much moint in trying to calculateDueDate() if numPeriods isn't set.");
        }
        if (periodsPerYear == 0) {
            throw new IllegalStateException(
                    "Not much moint in trying to calculateDueDate() if the periodsPerYear isn't set.");
        }

        long dueTimestamp = startTimestamp + YEARMILLISECONDS / periodsPerYear * numPeriods;

        Datestamp due = new Datestamp();
        try {
            /*
             * Calling this will round it the nearest day. TODO It may well
             * need a round-up bias added.
             */
            due.setDate(dueTimestamp);
        } catch (ParseException pe) {
            throw new IllegalStateException("ParseException hit when trying to calculate the Due Date");
        }
        dueDate = due;
    }
}
