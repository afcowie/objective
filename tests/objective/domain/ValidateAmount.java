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

import junit.framework.TestCase;

/**
 * Test utility functions in Amount class.
 * 
 * @see objective.domain.ValidateForeignCurrency
 * @author Andrew Cowie
 */
public class ValidateAmount extends TestCase
{

    public final void testAmountZero() {
        final String str;

        str = Amount.numberToString(0);
        assertEquals("0.00", str);
    }

    public final void testSetValue() {
        long amount;
        String str;

        amount = 110;

        assertEquals("1.10", Amount.numberToString(amount));

        amount = Amount.stringToNumber("5.6");
        assertEquals("5.60", Amount.numberToString(amount));

        try {
            Amount.stringToNumber("blah"); // should throw
            fail("Amount setValue didn't throw on bad input");
        } catch (NumberFormatException nfe) {
            // supposed to throw this
        } catch (Exception e) {
            fail("Amount setValue threw an exception, but not one that was expected");
        }

        /*
         * Check trimming. This used to be rounding.
         */

        amount = Amount.stringToNumber("1.005");
        assertEquals(100, amount);

        amount = Amount.stringToNumber("1.0049");
        assertEquals(100, amount);

        amount = Amount.stringToNumber("0.01499999999999999999999999999999999999");
        assertEquals(1, amount);

        /*
         * Check nuacnces
         */

        amount = Amount.stringToNumber(".01");
        str = Amount.numberToString(amount);
        assertEquals("0.01", str);

        amount = Amount.stringToNumber(".1");
        str = Amount.numberToString(amount);
        assertEquals("0.10", str);

        /*
         * Normal
         */

        amount = Amount.stringToNumber("49.95");
        assertEquals(4995, amount);
        str = Amount.numberToString(amount);
        assertEquals("49.95", str);
    }

    public final void testNegativeNumbers() {
        long amount;
        String str;

        amount = Amount.stringToNumber("-1");
        assertEquals(-100, amount);
        str = Amount.numberToString(amount);
        assertEquals("-1.00", str);

        /*
         * Check nuacnces
         */

        amount = Amount.stringToNumber("-0.01");
        assertEquals(-1, amount);
        str = Amount.numberToString(amount);
        assertEquals("-0.01", str);

        amount = Amount.stringToNumber("-0.1");
        assertEquals(-10, amount);
        str = Amount.numberToString(amount);
        assertEquals("-0.10", str);

        amount = Amount.stringToNumber("-.01");
        assertEquals(-1, amount);

        amount = Amount.stringToNumber("-.1");
        assertEquals(-10, amount);
    }

    public void testPercentage() {
        long amount;

        amount = Amount.percent(10000, 10);
        assertEquals(1000, amount);

        amount = Amount.percent(10000, 100);
        assertEquals(10000, amount);

        amount = Amount.percent(10000, 0);
        assertEquals(0, amount);

        amount = Amount.percent(10000, 1);
        assertEquals(100, amount);

        amount = Amount.percent(10000, 25);
        assertEquals(2500, amount);

        amount = Amount.percent(100, 33);
        assertEquals(33, amount);

        amount = Amount.percent(10, 33);
        assertEquals(3, amount);

        amount = Amount.percent(10, 66);
        assertEquals(7, amount);
    }

    public final void testPadCommaAndToString() {
        final String original;
        long amount;
        String str;

        original = "12345.67";
        amount = Amount.stringToNumber(original);

        try {
            Amount.padComma("45");
            fail("Should have thrown an exception as argument wasn't a x.xx form String");
        } catch (NumberFormatException nfe) {
        }
        try {
            Amount.padComma("");
            fail("Should have thrown an exception as argument wasn't a x.xx form String");
        } catch (NumberFormatException nfe) {
        }

        str = Amount.padComma(original);
        assertEquals("12,345.67", str);

        amount = Amount.stringToNumber("123");
        str = Amount.numberToString(amount);
        assertEquals("123.00", str);

        amount = Amount.stringToNumber("0.45");
        str = Amount.numberToString(amount);
        assertEquals("0.45", str);

        amount = Amount.stringToNumber("987654321.12");
        str = Amount.numberToString(amount);
        str = Amount.padComma(str);
        assertEquals("987,654,321.12", str);
    }
}
