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
package objective.domain;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.gnome.gtk.GraphicalTestCase;

public class ValidateDatestamp extends GraphicalTestCase
{
    public final void testValidDatestampStringInput() {
        long date;

        date = -1L;

        try {
            date = Datestamp.stringToDate("28 Dec 73");
        } catch (ParseException pe) {
            fail("Shouldn't have thrown ParseException" + pe.getMessage());
        }
        assertEquals("First test failed", 125884800, date);

        /*
         * Implicit year should feed off of the existing stamp, whatever it is
         */

        try {
            date = Datestamp.stringToDate("28 Dec", 94694401);
        } catch (ParseException pe) {
            fail("Shouldn't have thrown ParseException" + pe.getMessage());
        }
        assertEquals("Assumed existing year test failed", 125884800, date);

        /*
         * Now test the default of using the current year.
         */

        try {
            date = Datestamp.stringToDate("28 Dec", 0L);
        } catch (ParseException pe) {
            fail("Shouldn't have thrown ParseException" + pe.getMessage());
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yy");
        String expectedYear = sdf.format(new Date());
        assertEquals("Current year failed", "28 Dec " + expectedYear, Datestamp.dateToString(date));

        /*
         * Now test the input with only a day
         */

        try {
            date = Datestamp.stringToDate("15", 0L);
        } catch (ParseException pe) {
            fail("'15' shouldn't have thrown ParseException" + pe.getMessage());
        }
        sdf = new SimpleDateFormat("MMM");
        String expectedMonth = sdf.format(new Date());
        assertEquals("Day only failed", "15 " + expectedMonth + " " + expectedYear,
                Datestamp.dateToString(date));
    }

    public final void testInvalidDatestampStringInput() {
        try {
            Datestamp.stringToDate("15 Janvier 2005");
            fail("Should have thrown exception");
        } catch (ParseException pe) {
        }

        try {
            Datestamp.stringToDate("15 Fev");
            fail("Should have thrown exception");
        } catch (ParseException pe) {
        }

        try {
            Datestamp.stringToDate("15 Jan 1907");
        } catch (ParseException pe) {
        }
    }

    public final void testUnbiasedByTimezones() throws ParseException {
        final long internal;

        internal = Datestamp.stringToDate("1 Jul 05");

        double hours = ((double) internal) / 3600;
        assertTrue(Math.round(hours) == hours);

        double days = hours / 24;
        assertTrue(Math.round(days) == days);
    }
}
