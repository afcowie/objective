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

import java.text.ParseException;

import objective.domain.Datestamp;

import junit.framework.TestCase;

public class RangeCalculatorTest extends TestCase
{
    public final void testConstructorsSetters() {
        RangeCalculator calc;
        try {
            calc = new RangeCalculator();
            Datestamp one = new Datestamp("1 May 04");
            Datestamp two = new Datestamp("2 May 04");
            calc = new RangeCalculator(one, two);
        } catch (Exception bad) {
            fail("Threw " + bad);
        }
    }

    public final void testCalculateDates() throws ParseException {
        Datestamp one = new Datestamp("26 May 06");

        RangeCalculator calc = new RangeCalculator(one, one);
        assertEquals(1, calc.calculateDays());
        Datestamp two = new Datestamp("27 May 06");

        try {
            calc.setStartDate(two);
            calc.calculateDays();
            fail("calculateDays() was expected to throw UnsupportedOperationException if the start date is greater than the end date");
        } catch (UnsupportedOperationException uoe) {
            // good
        }
        calc.setStartDate(one);
        calc.setEndDate(two);
        assertEquals(2, calc.calculateDays());

        two.setDate("26 May 07");
        /*
         * (it's NOT a leap year; it's a year + one day, just as Monday to
         * Monday is eight days)
         */
        assertEquals(366, calc.calculateDays());

        Datestamp first = new Datestamp("1 Jan 03");
        Datestamp second = new Datestamp("31 Dec 03");
        calc = new RangeCalculator(first, second);
        assertEquals(365, calc.calculateDays());

    }

    public final void testCalculateWeeks() throws ParseException {
        Datestamp first = new Datestamp("1 Jan 03");
        Datestamp second = new Datestamp("31 Dec 03");

        RangeCalculator calc = new RangeCalculator(first, second);
        assertEquals(52.0, calc.calculateWeeks(), 0.001);

        second.setDate("28 Jan 03");
        assertEquals(4.0, calc.calculateWeeks(), 0.1);
    }

    public final void testCalculateMonths() {
        Datestamp alpha = new Datestamp("1 Jul 05");
        Datestamp omega = new Datestamp("31 Dec 05");

        RangeCalculator calc = new RangeCalculator(alpha, omega);
        assertEquals(6.0, calc.calculateMonths(), 0.1);

        Datestamp one = new Datestamp("31 Jul 05");
        calc.setEndDate(one);
        assertEquals(1.0, calc.calculateMonths(), 0.1);
    }

    public final void testCalculateSingleDay() {
        Datestamp une = new Datestamp("19 Jun 06");
        Datestamp deux = new Datestamp("19 Jun 06");

        RangeCalculator calc = new RangeCalculator(une, deux);
        assertEquals(1, calc.calculateDays());
        assertEquals(0.142857, calc.calculateWeeks(), 0.01);
        assertEquals(0.032876, calc.calculateMonths(), 0.01);
    }

    public final void testQuirksWithSpecificIntervals() {
        RangeCalculator range;

        /*
         * A 7 day week!
         */
        range = new RangeCalculator(new Datestamp("1 Jul 05"), new Datestamp("7 Jul 05"));
        int days = range.calculateDays();

        assertEquals(7, days);
        float weeks = range.calculateWeeks();
        assertEquals(1, weeks, 0.1);

        /*
         * Half a year in calendar terms is actually a bit longer. From 1 Jul
         * to 31 Dec is 184 days and thus 26.2 weeks, not the 26.0 you might
         * expect. Alas.
         */

        range = new RangeCalculator(new Datestamp("1 Jul 05"), new Datestamp("31 Dec 05"));
        days = range.calculateDays();

        assertEquals(184, days);
        weeks = range.calculateWeeks();
        assertEquals(26.2, weeks, 0.1);
    }
}
