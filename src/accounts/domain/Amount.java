/*
 * Amount.java
 * 
 * See LICENCE file for usage and redistribution terms
 * Copyright (c) 2005 Operational Dynamics
 */
package accounts.domain;

import java.math.BigDecimal;

/**
 * A monetary amount, stored to two decimal places. This works because money
 * amounts (at least, as recorded in accounting) are always to this exact
 * precision. All values are represented publicly as Strings in the form "0.00",
 * that is as numbers with two and only decimal places (ie, money).
 * <P>
 * Note that amounts are scalar - that is, being debit and credit is carried as
 * an aspects of entries in transactions/ledgers.
 * <P>
 * We use BigDecimal to do calculations which involve multiplication or
 * division, since it is needed to avoid rounding problems with floating point.
 * However, except in rare cases, we can shield most of that ugliness.
 * <P>
 * Internally, we store a long, which is the amount * 100. This was inspired by
 * {@link BigDecimal#valueOf(long, int)}- and we use that factory for
 * conversions to take advantage of it's efficiency.
 * 
 * @author Andrew Cowie
 * 
 */
public class Amount
{
	/*
	 * Instance variables ---------------------------------
	 */
	/**
	 * This is the value * 100. Or, if you prefer, the number of cents :)
	 */
	private long	number	= 0;

	/*
	 * Constructors ---------------------------------------
	 */

	public Amount() {
		// only for creating search prototypes
	}

	/**
	 * A new number. If you feed this constructor an empty string you'll get
	 * zero.
	 * 
	 * @param value
	 *            strictly, syntax is as per BigDecimal(String), but
	 *            practically, "0.00" works, and is what is expected.
	 * @throws NumberFormatException
	 */
	public Amount(String value) {
		setValue(value);
	}

	private Amount(BigDecimal value) {
		setValue(value);
	}

	/*
	 * Getters and Setters --------------------------------
	 */

	/**
	 * @value the number to set as the Amount's value. We validate (and round to
	 *        two decimal places) using BigDecimal.
	 * @throws NumberFormatException
	 *             if value can't be parsed.
	 */
	public void setValue(String value) {
		number = stringToNumber(value);
	}

	protected void setValue(BigDecimal value) {
		number = bigToNumber(value);
	}

	/**
	 * Internal method. Round the given value to two decimal places, and set the
	 * internal instance variable with an appropriate long value.
	 * 
	 * @see BigDecimal#setScale(int, int);
	 * @param str
	 *            The String to be parsed into a ling representing the number of
	 *            cents.
	 * @throws NumberFormatException
	 *             if BigDecimal can't parse str
	 */
	protected long stringToNumber(String str) {
		if (str.equals("") || str.equals("0")) {
			return 0L;
		}
		BigDecimal rawNumber = new BigDecimal(str);
		return bigToNumber(rawNumber);
	}

	/**
	 * Internal method. This is a seprate from stringToNumber() as multiply and
	 * percent need an Amount(BigDecimal) constructor, which in turns needs a
	 * setValue(BigDecimal).
	 * 
	 * @param rawNumber
	 *            the BigDecimal to be parsed.
	 * @return the number of cents, a long.
	 */
	protected long bigToNumber(BigDecimal rawNumber) {
		BigDecimal reducedNumber = rawNumber.setScale(2, BigDecimal.ROUND_HALF_UP);
		long num = reducedNumber.unscaledValue().longValue();
		return num;
	}

	/**
	 * Convert from our internal long representation to a string with two and
	 * only two decimal places.
	 */
	protected String numberToString(long num) {
		if (num == 0) {
			return "0.00";
		}
		StringBuffer buf = new StringBuffer(Long.toString(num));
		int len = buf.length();
		// +ve first, as that's most likely!
		if ((num < 10) && (num > -10)) {
			// "4" => "0.04"
			buf.insert(len - 1, "0.0");
		} else if ((num < 100) && (num > -100)) {
			// "42" => "0.42"; account for possible -ve
			buf.insert(len - 2, "0.");
		} else {
			// "1024" => "10.24"
			buf.insert(len - 2, '.');
		}
		return buf.toString();

	}

	/**
	 * Returns to two (and only two) decimal places, but with no other
	 * formatting.
	 */
	public String getValue() {
		return numberToString(this.number);
	}

	/**
	 * For use in debugging output, and in GUI display. This is equivalent to
	 * the result of getValue() since we represent our Amounts externally using
	 * Strings.
	 */
	public String toString() {
		return padComma(getValue());
	}

	/**
	 * Get at the underlying java.math.BigDecimal .
	 * 
	 * @return immutable (and possibly a reused Object).
	 */
	public BigDecimal getNumber() {
		/*
		 * This leverages the valueOf Factory method by the fact that our use of
		 * a long value as the Amount's value * 100 equates to a fixed
		 * BigDeclimal scale of 2.
		 */
		return BigDecimal.valueOf(number, 2);
	}

	/*
	 * Basic operations, use BigDecimal where necessary ---
	 */

	/**
	 * Compare this Amount with the specified Object for equality.
	 */
	public boolean equals(Object x) {
		if (x instanceof Amount) {
			Amount a = (Amount) x;
			if (this.number == a.number) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Create a new Amount, equal to this one (but a separate object, of course)
	 */
	public Object clone() {
		Amount obj = new Amount();
		obj.number = this.number;
		return obj;
	}

	/**
	 * Add value to this Amount. (Unlike BigDecimal it does NOT return a new
	 * object). Ugly name used instead of add() to avoid ambiguity.
	 */
	public void incrementBy(Amount value) {
		number += value.number;
	}

	/**
	 * Reduce Amount by value.
	 */
	public void decrementBy(Amount value) {
		number -= value.number;
	}

	/*
	 * Other mathematical operations ----------------------
	 */

	/**
	 * Multiply this Amount by the given percentage, and return a new Amount
	 * with the result.
	 * 
	 * @param p
	 *            The percentage to mutliply by, integer from 0-100 only.
	 * @return a new Amount object with a value of p percent of this Amount
	 *         object's value.
	 */
	public Amount percent(int p) {

		if ((p < 0) || (p > 100)) {
			throw new IllegalArgumentException("percentage must be between 0 and 100");
		}

		if (p == 100) {
			return (Amount) this.clone();
		}
		if (p == 0) {
			return new Amount("0");
		}

		BigDecimal fraction;
		if (p < 10) {
			fraction = new BigDecimal("0.0" + p);
		} else {
			fraction = new BigDecimal("0." + p);
		}
		BigDecimal result = fraction.multiply(getNumber());
		return new Amount(result);
	}

	/**
	 * Just a small utility method, but a often used case.
	 */
	public boolean isZero() {
		return (number == 0);
	}

	/**
	 * @param num
	 *            the number (a String with a point and two decimal places) to
	 *            be formatted.
	 * @return a String that has had thousands commas inserted
	 */
	protected String padComma(String str) {
		int len = str.length();
		int period = str.indexOf('.');

		if ((len == 0) || (period == -1)) {
			throw new NumberFormatException(
					"You shouldn't call this on an arbitrary string - only on a two digit decimal String as returned by Amount.getValue()");
		}

		StringBuffer buf = new StringBuffer(str);

		for (int i = period - 3; i > 0; i -= 3) {
			buf.insert(i, ',');
		}

		return buf.toString();
	}

}