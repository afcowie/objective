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

/**
 * Work out various characteristics about the range between two Datestamps.
 * Instead of a services layer calculator, a "Range" could be considered a
 * domain object, but its not per-se a part of any other domain objects;
 * PayrollTransactions have start and end dates, but no particular need to
 * store the way they were composed. That may change, and if it does it this
 * will have to move to the domain layer. For the mean time, you can just use
 * the getters exposed for start and end date.
 * 
 * @author Andrew Cowie
 */
public class RangeCalculator
{
    private Datestamp startDate;

    private Datestamp endDate;

    /**
     * Instantiate a new RangeCalculator. You'll have to call the setters to
     * give it start and end dates before calling its calculate methods.
     */
    public RangeCalculator() {}

    /**
     * Quickly instantiate a RangeCalculator with the specified start and end
     * dates.
     */
    public RangeCalculator(Datestamp start, Datestamp end) {
        setStartDate(start);
        setEndDate(end);
    }

    /**
     * Get the Datestamp reresenting the beginning of the selected date range
     */
    public Datestamp getStartDate() {
        return startDate;
    }

    public void setStartDate(Datestamp startDate) {
        if (startDate == null) {
            throw new IllegalArgumentException();
        }
        this.startDate = startDate;
    }

    /**
     * Get the Datestamp reresenting the end of the selected date range
     */
    public Datestamp getEndDate() {
        return endDate;
    }

    public void setEndDate(Datestamp endDate) {
        if (endDate == null) {
            throw new IllegalArgumentException();
        }
        this.endDate = endDate;
    }

    private void checkReady() throws IllegalStateException {
        if ((startDate == null) || (endDate == null)) {
            throw new IllegalStateException();
        }
    }

    /**
     * Work out the number of months the current values in this RangePicker
     * represnt.
     */
    public float calculateMonths() {
        checkReady();
        int days = calculateDays();
        float months = days / (365f / 12f);
        return months;
    }

    public float calculateWeeks() {
        checkReady();
        int days = calculateDays();

        float weeks = days / (365f / 52f);
        return weeks;
    }

    /**
     * Work out the number of days represented by these dates. Note that this
     * is <b>incluisve</b>.
     * 
     * @throws IllegalStateException
     *             if there is something wrong with the start or end date
     *             variables
     * @throws UnsupportedOperationException
     *             if you try to calculate the difference when start date is
     *             newer than end date.
     * @return the number of days encompassed by the specified start and end
     *         date (including the start and end days).
     */
    public int calculateDays() {
        checkReady();
        long startTimestamp, endTimestamp;

        /*
         * Both are milliseconds
         */
        startTimestamp = startDate.getInternalTimestamp();
        endTimestamp = endDate.getInternalTimestamp();

        /*
         * If it's the same day, then that's a range of one day
         */
        if (startTimestamp == endTimestamp) {
            return 1;
        }
        if (startTimestamp > endTimestamp) {
            throw new UnsupportedOperationException("End date must be after start date");
            // TODO: some UI for this?
        }

        /*
         * Work out the difference,
         */
        long days = (endTimestamp - startTimestamp) / (86400 * 1000);
        /*
         * and include the start day.
         */
        return (int) days + 1;
    }
}
