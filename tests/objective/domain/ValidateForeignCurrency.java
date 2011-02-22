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

    public final void testForeignAmount() {
        Currency usd = new Currency("USD", "United States Dollar", "$");
        ForeignAmount fa = new ForeignAmount("10.00", usd, "1.35761");

        assertEquals("10.00", fa.toString());

        assertEquals("13.58", fa.getValue());
    }

    public final void testRateFormat() {
        final int expected = 5;
        final Currency usd;
        final ForeignAmount fa1, fa2, fa5, fa6a, fa6b;

        assertEquals("No big deal, but you'll need to adjust this test case as it's"
                + " rather carefully set up to test for " + expected + " decimal places.", expected,
                ForeignAmount.RATE_DECIMAL_PLACES);

        usd = new Currency("USD", "United States Dollar", "$");
        fa1 = new ForeignAmount("10.00", usd, "1.3");

        assertEquals("1.30000", fa1.getRate());

        fa2 = new ForeignAmount("10.00", usd, "1.35");
        assertEquals("1.35000", fa2.getRate());

        fa5 = new ForeignAmount("10.00", usd, "1.35761");

        assertEquals("1.35761", fa5.getRate());

        fa6a = new ForeignAmount("10.00", usd, "1.357611");
        assertEquals("1.35761", fa6a.getRate());

        fa6b = new ForeignAmount("10.00", usd, "1.357617");
        assertEquals("1.35762", fa6b.getRate());
    }

    public final void testForeignAmountCommaPadding() {
        final Currency usd;
        final ForeignAmount fa;
        final Amount a;

        usd = new Currency("USD", "United States Dollar", "$");
        fa = new ForeignAmount("10000.00", usd, "1.35761");

        assertEquals("10000.00", fa.getForeignValue());
        assertEquals("13576.10", fa.getValue());

        assertEquals("10,000.00", fa.toString());

        /*
         * Since toString() is overridden to show the ForeignAmount's value
         * with commans, if you want to see the home value with commas, just
         * quickly construct a new Amount.with that value
         */

        a = new Amount(fa.getValue());
        assertEquals("13,576.10", a.toString());
    }

    public final void testChangeRate() {
        final Currency gbp;
        final ForeignAmount fa;

        gbp = new Currency("GBP", "British Pound", "?");
        fa = new ForeignAmount("10000.00", gbp, "2.5143666");

        assertEquals("10000.00", fa.getForeignValue());
        assertEquals("Didn't round properly", "25143.67", fa.getValue());

        fa.setRate("3.141592");

        assertEquals("Changing the rate shouldn't change the face value!", "10000.00",
                fa.getForeignValue());
        assertEquals("31415.92", fa.getValue());
    }

    public final void testChangeFaceValue() {
        final Currency sgd;
        final ForeignAmount fa;

        sgd = new Currency("SGD", "Singaporean Dollar", "$");
        fa = new ForeignAmount("10000.00", sgd, "1.13571");

        assertEquals("10000.00", fa.getForeignValue());
        assertEquals("11357.10", fa.getValue());

        fa.setForeignValue("20000.00");

        assertEquals("20000.00", fa.getForeignValue());
        assertEquals("22714.20", fa.getValue());
    }

    public final void testChangeHomeValue() {
        final Currency eur;
        final ForeignAmount fa;

        eur = new Currency("EUR", "European Euro", "?");
        fa = new ForeignAmount("100.00", eur, "1.500");

        assertEquals("100.00", fa.getForeignValue());
        assertEquals("150.00", fa.getValue());
        assertEquals("1.50000", fa.getRate());

        fa.setValue("155.19");

        /*
         * New home value is what we said it should be
         */
        assertEquals("155.19", fa.getValue());
        /*
         * Exchange rate is indeed changed
         */
        assertEquals("1.55190", fa.getRate());
        /*
         * And face value is unchanged.
         */
        assertEquals("100.00", fa.getForeignValue());
    }

    public final void testClone() {
        final Currency eur, gbp;
        final ForeignAmount fa1, fa2, fa3;
        final Amount a4;
        eur = new Currency("EUR", "European Euro", "?");
        gbp = new Currency("GBP", "British Pound", "?");

        fa1 = new ForeignAmount("100.00", eur, "1.500");

        fa2 = (ForeignAmount) fa1.clone();

        assertEquals("100.00", fa2.getForeignValue());
        assertEquals(eur, fa2.getCurrency());
        assertEquals("150.00", fa2.getValue());

        fa1.setCurrency(gbp);
        fa1.setForeignValue("950.00");

        assertEquals(gbp, fa1.getCurrency());
        assertNotSame(eur, fa1.getCurrency());
        /*
         * And the clone had better still be the original...
         */
        assertEquals(eur, fa2.getCurrency());
        assertEquals("100.00", fa2.getForeignValue());
        assertEquals("150.00", fa2.getValue());

        /*
         * Not exactly certain this is how I want this to go, but it'll do.
         * For now, it means that toString will always give the face value,
         * regardless of Amount or ForeignAmount, in face currency terms.
         */
        fa3 = (ForeignAmount) fa2.clone();
        assertEquals("100.00", fa3.toString());

        /*
         * If this fails, it menas the behaviour has changed and the fully
         * qualified subclass's toString() isn't prevailing
         */
        a4 = (Amount) ((Amount) fa2).clone();
        assertNotSame("150.00", a4.toString());
        assertEquals("100.00", a4.toString());
    }

    public final void testAvoidDivideByZero() {
        final Currency eur;
        final ForeignAmount fa;
        ForeignAmount c;

        eur = new Currency("EUR", "European Euro", "\u20AC");
        fa = new ForeignAmount("0.00", eur, "1.500");

        c = null;
        try {
            c = (ForeignAmount) fa.clone();
        } catch (ArithmeticException ae) {
            fail("Should be avoiding dividing by zero");
        }
        assertNotNull(c);
        assertEquals("0.00", c.getValue());
    }

    public final void testSetValueAmount() {
        final Currency eur, gbp;
        final ForeignAmount fa, other;
        final Amount dummy;

        gbp = new Currency("GBP", "British Pound", "\u00A3");
        eur = new Currency("EUR", "European Euro", "\u20AC");

        fa = new ForeignAmount("1.00", gbp, "2.450");

        dummy = new Amount("5.00");
        fa.setForeignValue(dummy);

        assertEquals("5.00", fa.getForeignValue());

        other = new ForeignAmount("-10.00", eur, "1.50");
        fa.setForeignValue(other);

        assertEquals("-36.75", fa.getValue());
        assertEquals("-15.00", fa.getForeignValue());
    }
}
