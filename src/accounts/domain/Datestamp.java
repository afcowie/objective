/*
 * Timestamp.java
 * 
 * See LICENCE file for usage and redistribution terms
 * Copyright (c) 2005 Operational Dynamics
 */
package accounts.domain;

import generic.util.DebugException;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * The date of a transaction. An object is used both to wrap representation, and
 * to allow changing in Transaction to not have to map through Account to adjust
 * indivitial stamps in Entries.
 * 
 * @author Andrew Cowie
 */
public class Datestamp
{
	/**
	 * We use -1 for uninitialized, to guard against lacking activation, and to
	 * prevent the ubiquitous 1 Jan 1970 stupidiy.
	 */
	private static long	UNSET		= -1;

	/*
	 * Instance variables ---------------------------------
	 */
	private long		_timestamp	= UNSET;

	/**
	 * Construct a new Datestamp. Leaves it "unset"
	 */
	public Datestamp() {
		// only for creating search prototypes
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
		twin._timestamp = this._timestamp;
		return twin;
	}

	/**
	 * Set the date stamp to be today
	 */
	public void setAsToday() {
		try {
			setDate(System.currentTimeMillis());
		} catch (ParseException pe) {
			throw new DebugException("ParseException thrown when using setDate on System.currentTimeMillis()!");
		}
	}

	public boolean isSet() {
		return (!(_timestamp == UNSET));
	}

	/**
	 * Outputs Datestamp in dd MMM yy (%e %b %y) form.
	 */
	public String toString() {
		SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yy");
		return sdf.format(new Date(_timestamp));
	}

	public void setDate(Calendar cal) {
		_timestamp = cal.getTimeInMillis();
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
		 * To validate (ie, reduce it down to a round day + 0 milliseconds), Get
		 * a Date, render it as in our string format (which has only day
		 * precision) and then parse the result back into a Datestamp.
		 */
		Date roughDate = new Date(timestamp);
		SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yy");
		String dayMonYear = sdf.format(roughDate);
		setDate(dayMonYear);
	}

	/**
	 * Parse the user's input as a Datestamp object. Validation is performed, as
	 * is an attempt to guess the best competion if a properly formatted Date
	 * isn't entered.
	 * 
	 * @param user
	 *            the String to parse into a Datestamp if possible.
	 */
	public void setDate(String user) throws ParseException {
		SimpleDateFormat fullsdf = new SimpleDateFormat("dd MMM yy");
		java.util.Calendar cal = java.util.Calendar.getInstance();

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
			 * datestamp is set to if set). This is rather cumbersome code, but
			 * it's how we get isolated bits of dates using the standard Java
			 * APIs. It could well be that the original input was garbage, in
			 * which case this attempt will fail as well and the exception will
			 * be thrown.
			 */

			if (_timestamp == UNSET) {
				cal.setTime(new Date());
			} else {
				cal.setTime(new Date(_timestamp)); // use this Datestamp's year
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
				 * And by now, either d is set, or we have thrown an Exception a
				 * third time, which we do not catch.
				 */
			}
		} catch (IllegalArgumentException iae) {
			// rethrow, having avoided fallback ladder.
			throw new ParseException(iae.getMessage(), 0);
		}
		_timestamp = d.getTime();
	}

	public Date getDate() {
		return new Date(_timestamp);
	}

	/* package */long getInternalTimestamp() {
		return _timestamp;
	}
}