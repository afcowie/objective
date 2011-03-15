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

public class ValidateForeignCurrency extends TestCase
{
    public final void testCurrencyConstuctor() {
        Currency c = new Currency("CAD", "Canadian Dollar", "$");

        assertEquals("CAD", c.getCode());
        assertEquals("Canadian Dollar", c.getName());
        assertEquals("$", c.getSymbol());

    }

    public final void testCurrencyCodeFormat() {
        try {
            new Currency("Cad", "Canadian Dollar", "$");
            fail("Didn't throw IllegalArgumentExpception when a incorrectly formatted cuurency was used");
        } catch (IllegalArgumentException iae) {
        }
        try {
            new Currency("cad", "Canadian Dollar", "$");
            fail("Didn't throw IllegalArgumentExpception when a incorrectly formatted cuurency was used");
        } catch (IllegalArgumentException iae) {
        }
    }

    public final void testRateFormat() {
        String str;

        assertEquals("No big deal, but you'll need to adjust this test case as it's"
                + " rather carefully set up to test for 5 decimal places.", 5,
                ForeignAmount.RATE_DECIMAL_PLACES);

        str = ForeignAmount.rateToString(1.35761);
        assertEquals("1.35761", str);

        str = ForeignAmount.rateToString(1.3);
        assertEquals("1.30000", str);

        str = ForeignAmount.rateToString(1.35);
        assertEquals("1.35000", str);

        str = ForeignAmount.rateToString(1.357611);
        assertEquals("1.35761", str);

        str = ForeignAmount.rateToString(1.357617);
        assertEquals("1.35762", str);
    }

    public final void testForeignAmountCommaPadding() {
        long amount, value;
        double rate;
        String str;

        amount = 1000000;
        rate = 1.35761;
        value = ForeignAmount.calculateValue(amount, 1.35761);
        assertEquals(1357610, value);

        str = Amount.numberToString(value);
        str = Amount.padComma(str);

        assertEquals("13,576.10", str);

        amount = Amount.stringToNumber("10000.00");
        rate = 2.5143666;
        value = ForeignAmount.calculateValue(amount, rate);
        str = Amount.numberToString(value);

        assertEquals("Didn't round properly", "25143.67", str);
    }

    /*
     * TODO
     */
    public final void testAvoidDivideByZero() {
        double rate;
        try {
            rate = 1.0 / 0.0;
            assertTrue(Double.isInfinite(rate));
        } catch (ArithmeticException ae) {
            fail("Should be avoiding dividing by zero");
        }
    }
}
