/*
 * AmountTest.java
 * 
 * See LICENCE file for usage and redistribution terms
 * Copyright (c) 2005-2006 Operational Dynamics
 */
package accounts.domain;

import java.math.BigDecimal;

import junit.framework.TestCase;

/**
 * Unit test Amount class.
 * 
 * @see accounts.domain.ForeignCurrencyTest
 * @author Andrew Cowie
 */
public class AmountTest extends TestCase
{
	public void testDefaultConstructor() {
		Amount a = new Amount();
		assertEquals("0.00", a.getValue());

		Amount b = new Amount("");
		assertEquals("0.00", b.getValue());
	}

	public void testAmountZero() {
		Amount a = new Amount("0");

		/*
		 * Tests two digit translation
		 */
		assertEquals("0.00", a.getValue());

		BigDecimal num = a.getBigDecimal();
		assertNotNull(num);
		assertEquals("0.00", num.toString());

		assertTrue(a.isZero());
	}

	public void testSetValue() {
		Amount a = new Amount("1.1");
		assertEquals("1.10", a.getValue());
		assertFalse(a.isZero());

		/*
		 * Make sure setValue overrides
		 */
		a.setValue("5.6");
		assertEquals("5.60", a.getValue());

		try {
			a.setValue("blah"); // should throw
			fail("Amount setValue didn't throw on bad input");
		} catch (NumberFormatException nfe) {
			// supposed to throw this
		} catch (Exception e) {
			fail("Amount setValue threw an exception, but not one that was expected");
		}

		/*
		 * Check rounding
		 */
		Amount b = new Amount("1.005");
		assertEquals("1.01", b.getValue());

		Amount c = new Amount("1.0049");
		assertEquals("1.00", c.getValue());

		Amount d = new Amount("0.00499999999999999999999999999999999999");
		assertEquals("0.00", d.getValue());

		/*
		 * Check nuacnces of getValue
		 */
		Amount e = new Amount(".01");
		assertEquals("0.01", e.getValue());

		Amount f = new Amount(".1");
		assertEquals("0.10", f.getValue());

		/*
		 * 
		 */

		Amount g = new Amount("49.95");
		assertEquals("49.95", g.getValue());
	}

	public void testEqualsAndClone() {
		Amount a = new Amount("42");
		assertTrue(a.equals(a));

		Amount b = new Amount("42");
		assertTrue(a.equals(b));
		assertTrue(b.equals(a));

		Amount c = new Amount("1");
		assertFalse(a.equals(c));
		assertFalse(c.equals(a));

		Amount d = (Amount) c.clone();
		assertFalse(d == c);
	}

	public void testNegativeNumbers() {
		Amount a = new Amount("-1");
		assertEquals("-1.00", a.getValue());

		Amount b = new Amount("-1");
		assertTrue(a.equals(b));
		assertTrue(b.equals(a));

		Amount c = new Amount("1");
		assertFalse(a.equals(c));
		assertFalse(c.equals(a));

		/*
		 * Check nuacnces of getValue
		 */
		Amount e = new Amount("-0.01");
		assertEquals("-0.01", e.getValue());

		Amount f = new Amount("-0.1");
		assertEquals("-0.10", f.getValue());

		Amount g = new Amount("-.01");
		assertEquals("-0.01", g.getValue());

		Amount h = new Amount("-.1");
		assertEquals("-0.10", h.getValue());

	}

	public void testAddition() {
		Amount a = new Amount("1");
		Amount b = new Amount("2");

		// 1 + 2 = 3 :)
		a.incrementBy(b);

		assertEquals("3.00", a.getValue());
		assertTrue((new BigDecimal("3").compareTo(a.getBigDecimal())) == 0);

		Amount c = new Amount("-3");
		Amount d = new Amount("15");

		c.incrementBy(d);

		assertEquals("12.00", c.getValue());
		assertTrue((new BigDecimal("12").compareTo(c.getBigDecimal())) == 0);
	}

	public void testSubtraction() {
		Amount a = new Amount("5.55");
		Amount b = new Amount("3.33");

		a.decrementBy(b);

		assertEquals("2.22", a.getValue());

		Amount c = new Amount("5");
		Amount d = new Amount("-3");

		c.decrementBy(d);
		assertEquals("8.00", c.getValue());

		c.decrementBy(c);
		assertEquals("0.00", c.getValue());
		c.decrementBy(b);
		assertEquals("-3.33", c.getValue());
	}

	public void testPercentage() {
		Amount a = new Amount("100.00");

		Amount b = a.percent(10);

		assertEquals("10.00", b.getValue());

		Amount c = a.percent(100);
		assertEquals("100.00", c.getValue());

		Amount d = a.percent(1);
		assertEquals("1.00", d.getValue());
	}

	public void testToString() {
		Amount a = new Amount("12345.67");

		try {
			a.padComma("45");
			fail("Should have thrown an exception as argument wasn't a x.xx form String");
		} catch (NumberFormatException nfe) {
		}
		try {
			a.padComma("");
			fail("Should have thrown an exception as argument wasn't a x.xx form String");
		} catch (NumberFormatException nfe) {
		}

		assertEquals("12,345.67", a.toString());

		Amount b = new Amount("123");
		assertEquals("123.00", b.toString());

		Amount c = new Amount("0.45");
		assertEquals("0.45", c.toString());

		Amount d = new Amount("987654321.12");
		assertEquals("987,654,321.12", d.toString());
	}

	public void testCompareTo() {
		Amount a = new Amount("10.00");
		Amount b = new Amount("11.95");
		Amount c = new Amount("11.95");

		assertTrue(a.compareTo(b) < 0);
		assertTrue(b.compareTo(a) > 0);

		assertTrue(c.compareTo(c) == 0);
		assertTrue(b.compareTo(c) == 0);
		assertTrue(c.compareTo(b) == 0);

		Amount d = new Amount(1195);
		assertTrue(c.compareTo(d) == 0);
		Amount e = new Amount(-1195);
		assertTrue(e.compareTo(d) < 0);
	}

}