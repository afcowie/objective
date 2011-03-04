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

import generic.persistence.NotActivatedException;
import generic.util.DebugException;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * The date of a transaction. An object is used both to wrap representation,
 * and to allow changing in Transaction to not have to map through Account to
 * adjust indivitial stamps in Entries.
 * 
 * @author Andrew Cowie
 */
public class Datestamp implements Comparable<Datestamp>
{
    /**
     * We use -1 for uninitialized, to guard against lacking activation, and
     * to prevent the ubiquitous 1 Jan 1970 stupidiy.
     */
    private static final long UNSET = -1;

    private long timestamp = UNSET;

    /**
     * Construct a new Datestamp. Leaves it "unset"
     */
    public Datestamp() {}

    /**
     * Store a given unix timestamp directly.
     */
    public Datestamp(long timestamp) {
        if (timestamp < -1) {
            throw new IllegalArgumentException();
        }
        this.timestamp = timestamp;
    }

    /**
     * Construct a Datestamp object with user supplied input (which will be
     * passed to setDate() for validation).
     * 
     * @param user
     *            the String to parse
     */
    public Datestamp(String user) {
        if (user == null) {
            throw new IllegalArgumentException();
        }
        try {
            setDate(user);
        } catch (ParseException pe) {
            throw new IllegalArgumentException("Hit ParseException trying to construct with " + user);
        }
    }

    public Object clone() {
        Datestamp twin = new Datestamp();
        twin.timestamp = this.timestamp;
        return twin;
    }

    /**
     * Set the date stamp to be today
     */
    public void setAsToday() {
        try {
            setDate(System.currentTimeMillis() / 1000);
        } catch (ParseException pe) {
            throw new DebugException(
                    "ParseException thrown when using setDate on System.currentTimeMillis()!");
        }
    }

    /**
     * Quickly assert whether or not this Datestamp object has been set to
     * something.
     */
    public boolean isSet() {
        if (timestamp == 0) {
            throw new IllegalStateException();
        }
        return (!(timestamp == UNSET));
    }

    /**
     * Outputs Datestamp in dd MMM yy (%e %b %y) form.
     */
    public String toString() {
        if (timestamp == UNSET) {
            return "  UNSET  ";
        }
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yy");
        return sdf.format(new Date(timestamp * 1000));
    }

    /**
     * A convenience method to allow you to quickly set the datestamp based on
     * previously generated Calendar objet.
     * 
     * @param cal
     *            a Calendar object (perhaps the result of having done a
     *            selection with a Gtk{@link org.gnu.gtk.Calendar}) which is
     *            aligned on date boundary.
     */
    public void setDate(Calendar cal) {
        /*
         * TODO validate that the cal object is in fact date aligned.
         */
        this.timestamp = cal.getTimeInMillis() / 1000;
    }

    /**
     * 
     * @param timestamp
     *            a "milliseconds since" timestamp value. Must be a day,
     *            exactly, or rather, it will be renderded down to one.
     * @throws ParseException
     *             if the timestamp passed in does't result in a date.
     */
    public void setDate(long timestamp) throws ParseException {
        /*
         * To validate (ie, reduce it down to a round day + 0 milliseconds),
         * Get a Date, render it as in our string format (which has only day
         * precision) and then parse the result back into a Datestamp.
         */
        Date roughDate = new Date(timestamp);
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yy");
        String dayMonYear = sdf.format(roughDate);
        setDate(dayMonYear);
    }

    /**
     * Parse the user's input as a Datestamp object. Validation is performed,
     * as is an attempt to guess the best competion if a properly formatted
     * Date isn't entered.
     * 
     * @param user
     *            the String to parse into a Datestamp if possible.
     */
    public void setDate(String user) throws ParseException {
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

            if (timestamp == UNSET) {
                cal.setTime(new Date());
            } else {
                cal.setTime(new Date(timestamp * 1000)); // use this
                // Datestamp's
                // year
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
        this.timestamp = d.getTime() / 1000;
    }

    /**
     * Returns a java.util.Date that is equivalent to this Datestamp.
     */
    public Date getDate() {
        if (timestamp == 0) {
            throw new NotActivatedException();
        }
        return new Date(timestamp);
    }

    /**
     * Get the internal timestamp which backs this Datestamp object. Exposed
     * only so things like sorting can be done more effectively.
     * 
     * @return a long value. Note that this class only works to day precision
     *         - hours and seconds are (supposed to be) zero.
     */
    public long getInternalTimestamp() {
        if (timestamp == 0) {
            throw new NotActivatedException();
        }
        return timestamp;
    }

    /**
     * Compare two Datestamp objects. [This method implements Comparable
     * interface]
     * 
     * @return 1 if this one is greater (newer, more recent) than the argument
     *         x, -1 if this Datestmap is less than (older) than x, and 0 if
     *         they are the same.
     */
    public int compareTo(Datestamp x) {
        if (x == null) {
            throw new NullPointerException("Can't compareTo() against null");
        }
        if (timestamp == 0) {
            throw new NotActivatedException();
        }
        Datestamp d = x;

        if (this.timestamp > d.timestamp) {
            return 1;
        } else if (this.timestamp < d.timestamp) {
            return -1;
        } else {
            return 0;
        }
    }
}