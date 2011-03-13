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

/**
 * A monetary amount, stored to two decimal places. This works because money
 * amounts (at least, as recorded in accounting) are always to this exact
 * precision. All values are represented publicly as Strings in the form
 * "0.00", that is as numbers with two and only decimal places (ie, money).
 * 
 * <p>
 * Note that amounts are scalar - that is, being debit and credit is carried
 * as an aspects of Entries in Transactions and Ledgers.
 * 
 * @author Andrew Cowie
 */
public class Amount
{
    private Amount() {};

    /**
     * Internal method. Round the given value to two decimal places, and set
     * the internal instance variable with an appropriate long value.
     * 
     * @param str
     *            The String to be parsed into a ling representing the number
     *            of cents.
     * @throws NumberFormatException
     *             if BigDecimal can't parse str
     */
    public static long stringToNumber(String str) {
        final StringBuilder buf;
        final String cents;
        final int len;
        int i;

        if (str.equals("") || str.equals("0")) {
            return 0L;
        }

        len = str.length();
        buf = new StringBuilder(str);

        i = str.indexOf('.');
        if (i == -1) {
            // no decimal point
            // ×100
            buf.append('0');
            buf.append('0');
        } else if (i + 1 == len) {
            // stupid case: trailing decimal point
            buf.deleteCharAt(i);
            // ×100
            buf.append('0');
            buf.append('0');
        } else if (i + 1 == len - 1) {
            // single decimal place
            buf.deleteCharAt(i);
            // ×10
            buf.append('0');
        } else if (i + 1 == len - 2) {
            // two decimal places
            buf.deleteCharAt(i);
        } else {
            // two decimal places
            buf.deleteCharAt(i);
            // trim trailing garbage
            buf.delete(i + 2, len);
        }

        cents = buf.toString();
        return Long.parseLong(cents, 10);
    }

    /**
     * Convert from our internal long representation to a string with two and
     * only two decimal places.
     */
    public static String numberToString(long num) {
        final StringBuilder buf;
        if (num == 0) {
            return "0.00";
        }
        buf = new StringBuilder(Long.toString(num));
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
     * Multiply this Amount by the given percentage, and return a new Amount
     * with the result.
     * 
     * @param p
     *            The percentage to mutliply by, integer from 0-100 only.
     * @return a new Amount object with a value of p percent of this Amount
     *         object's value.
     */
    public static long percent(long amount, int p) {
        final long result;

        if ((p < 0) || (p > 100)) {
            throw new IllegalArgumentException("percentage must be between 0 and 100");
        }

        if (p == 100) {
            return amount;
        }
        if (p == 0) {
            return 0;
        }

        result = Math.round(amount * p / 100.0);
        return result;
    }

    /**
     * @param str
     *            the number (a String with a point and two decimal places -
     *            ie, the result of getValue()) to be formatted.
     * @return a String that has had thousands commas inserted
     */
    public static String padComma(String str) {
        int len, period;
        StringBuilder buf;

        len = str.length();
        period = str.indexOf('.');

        if ((len == 0) || (period == -1)) {
            throw new NumberFormatException(
                    "You shouldn't call this on an arbitrary string - only on a two digit decimal String as returned by Amount.numberToString()");
        }

        buf = new StringBuilder(str);

        for (int i = period - 3; i > 0; i -= 3) {
            buf.insert(i, ',');
        }

        return buf.toString();
    }
}
