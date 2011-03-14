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
 * An amount of money in a foreign currency. ForeignAmount encapsulates the
 * notions of face value (the amount as demoninated in the foreign currency's
 * terms), currency (which country's money the face value is in), and exchange
 * rate.
 * 
 * @author Andrew Cowie
 */
public class ForeignAmount
{
    private ForeignAmount() {}

    /**
     * The decimal precision of exchange rate Strings
     */
    public static final int RATE_DECIMAL_PLACES = 5;

    /**
     * Given a String decimal rate (ie, user input), convert it to double.
     * 
     * @throws NumberFormatException
     *             if Double can't parse the rate as a decimal number
     */
    public static double stringToRate(String str) {
        double p, d, r;
        double factor;

        if (str.equals("")) {
            return 1.0;
        }

        factor = 1e5;
        p = Double.parseDouble(str);
        d = p * factor;
        r = Math.round(d);
        return r / factor;
    }

    /**
     * Convert a rate to 5-digit String form.
     */
    public static String rateToString(double rate) {
        return String.format("%-7.5f", rate);
    }

    /**
     * Cleanly calculate the value given amount and a rate.
     */
    public static long calculateValue(long amount, double rate) {
        return Math.round(amount * rate);
    }

    /**
     * Returns Infinity on divide by zero, of course. You'll need to cater for
     * that - preferably by not calling this with 0 amount.
     */
    public static double calculateRate(long amount, long value) {
        return (double) value / (double) amount;
    }
}
