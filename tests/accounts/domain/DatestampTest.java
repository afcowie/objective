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
import java.text.SimpleDateFormat;
import java.util.Date;

import junit.framework.TestCase;

public class DatestampTest extends TestCase
{

    public final void testValidDatestampStringInput() {
        Datestamp stamp = new Datestamp();

        try {
            stamp.setDate("28 Dec 73");
        } catch (ParseException pe) {
            fail("Shouldn't have thrown ParseException" + pe.getMessage());
        }
        assertEquals("First test failed", "28 Dec 73", stamp.toString());

        /*
         * Implicit year should feed off of the existing stamp, whatever it is
         */
        try {
            stamp.setDate("28 Dec");
        } catch (ParseException pe) {
            fail("Shouldn't have thrown ParseException" + pe.getMessage());
        }
        assertEquals("Assumed existing year test failed", "28 Dec 73", stamp.toString());

        /*
         * Now test the default of using the current year.
         */
        stamp = new Datestamp();
        try {
            stamp.setDate("28 Dec");
        } catch (ParseException pe) {
            fail("Shouldn't have thrown ParseException" + pe.getMessage());
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yy");
        String expectedYear = sdf.format(new Date());
        assertEquals("Current year failed", "28 Dec " + expectedYear, stamp.toString());

        /*
         * Now test the input with only a day
         */
        stamp = new Datestamp();
        try {
            stamp.setDate("15");
        } catch (ParseException pe) {
            fail("'15' shouldn't have thrown ParseException" + pe.getMessage());
        }
        sdf = new SimpleDateFormat("MMM");
        String expectedMonth = sdf.format(new Date());
        assertEquals("Day only failed", "15 " + expectedMonth + " " + expectedYear, stamp.toString());
    }

    public final void testInvalidDatestampStringInput() {
        Datestamp stamp;
        stamp = new Datestamp();
        try {
            stamp.setDate("15 Janvier 2005");
            fail("Should have thrown exception");
        } catch (ParseException pe) {
        }

        stamp = new Datestamp();
        try {
            stamp.setDate("15 Fev");
            fail("Should have thrown exception");
        } catch (ParseException pe) {
        }

        stamp = new Datestamp();
        try {
            stamp.setDate("15 Jan 1907");
            fail("Should have parsed as " + stamp.toString());
        } catch (ParseException pe) {
        }
    }

    public final void testCompareTo() {
        Datestamp andrew, katrina;
        andrew = new Datestamp("28 Dec 73");
        katrina = new Datestamp("18 Mar 74");

        assertTrue(andrew.compareTo(katrina) == -1);
        assertTrue(katrina.compareTo(andrew) == 1);
        assertTrue(andrew.compareTo(andrew) == 0);

        try {
            Object o = new Object();
            andrew.compareTo(o);
            fail("Should have bombed on a not-Datestamp");
        } catch (IllegalArgumentException iae) {
        }
    }

    public final void testUnbiasedByTimezones() {
        Datestamp one;
        one = new Datestamp("1 Jul 05");

        long internal = one.getInternalTimestamp();

        double hours = ((double) internal) / 3600;
        assertTrue(Math.round(hours) == hours);

        double days = hours / 24;
        assertTrue(Math.round(days) == days);
    }
}
