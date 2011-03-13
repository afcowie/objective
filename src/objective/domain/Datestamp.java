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
import java.util.TimeZone;

import org.freedesktop.bindings.Time;

/**
 * Utility functions for manipulating the date of a Transaction.
 * 
 * @author Andrew Cowie
 */
public class Datestamp
{
    private Datestamp() {}

    public static long getToday() {
        return System.currentTimeMillis() / 1000;
    }

    /**
     * Outputs Datestamp in dd MMM yy (%e %b %y) form.
     */
    public static String dateToString(long datestamp) {
        if (datestamp == 0) {
            return "  UNSET  ";
        }

        return Time.formatTime("%e %b %y", datestamp);
    }

    /**
     * Parse the user's input as a Datestamp object. Validation is performed,
     * as is an attempt to guess the best competion if a properly formatted
     * Date isn't entered.
     * 
     * @param user
     *            the String to parse into a Datestamp if possible.
     */
    public static long stringToDate(String user) throws ParseException {
        return stringToDate(user, 0L);
    }

    public static long stringToDate(String user, long last) throws ParseException {
        SimpleDateFormat fullsdf = new SimpleDateFormat("dd MMM yy");
        java.util.Calendar cal = java.util.Calendar.getInstance();
        fullsdf.setTimeZone(TimeZone.getTimeZone("UTC"));

        Date d = null;
        try {
            d = fullsdf.parse(user);
            /*
             * validate year
             */
            cal.setTime(d);
            if (cal.get(java.util.Calendar.YEAR) < 1970) {
                throw new IllegalArgumentException(
                        "Can't enter a date before 1970. It parses, but doesn't make sense in ObjectiveAccounts!");
            }
        } catch (ParseException pe) {
            /*
             * In the event that a year wasn't specified, then we assume the
             * current year (actually, we assume the year of whatever this
             * datestamp is set to if set). This is rather cumbersome code,
             * but it's how we get isolated bits of dates using the standard
             * Java APIs. It could well be that the original input was
             * garbage, in which case this attempt will fail as well and the
             * exception will be thrown.
             */

            if (last == 0) {
                cal.setTime(new Date());
            } else {
                // use this Datestamp's year
                cal.setTime(new Date(last * 1000));
            }

            String assumedYear = Integer.toString(cal.get(java.util.Calendar.YEAR));
            /*
             * Now try assuming the only thing left off was the year
             */
            try {
                d = fullsdf.parse(user + " " + assumedYear);
            } catch (ParseException pe2) {
                /*
                 * No? Maybe the month and year were left off? We get a two
                 * digit month out of the conversion routines, then use a
                 * slightly different sdf to try parsing it.
                 */
                String assumedMonth = Integer.toString(cal.get(java.util.Calendar.MONTH) + 1);
                SimpleDateFormat dayofmonsdf = new SimpleDateFormat("dd MM yy");

                d = dayofmonsdf.parse(user + " " + assumedMonth + " " + assumedYear);
                /*
                 * And by now, either d is set, or we have thrown an Exception
                 * a third time, which we do not catch.
                 */
            }
        } catch (IllegalArgumentException iae) {
            // rethrow, having avoided fallback ladder.
            throw new ParseException(iae.getMessage(), 0);
        }
        return d.getTime() / 1000;
    }
}
