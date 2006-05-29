/*
 * RangeCalculatorTest.java
 * 
 * See LICENCE file for usage and redistribution terms
 * Copyright (c) 2006 Operational Dynamics
 */
package accounts.services;

import java.text.ParseException;

import accounts.domain.Datestamp;
import junit.framework.TestCase;

public class RangeCalculatorTest extends TestCase
{
	public final void testConstructorsSetters() {
		RangeCalculator calc;
		try {
			calc = new RangeCalculator();
			Datestamp one = new Datestamp("1 May 04");
			Datestamp two = new Datestamp("2 May 04");
			calc = new RangeCalculator(one, two);
		} catch (Exception bad) {
			fail("Threw " + bad);
		}
	}

	public final void testCalculateDates() throws ParseException {
		Datestamp one = new Datestamp("26 May 06");

		RangeCalculator calc = new RangeCalculator(one, one);
		assertEquals(1, calc.calculateDays());
		Datestamp two = new Datestamp("27 May 06");

		try {
			calc.setStartDate(two);
			calc.calculateDays();
			fail("calculateDays() was expected to throw UnsupportedOperationException if the start date is greater than the end date");
		} catch (UnsupportedOperationException uoe) {
			// good
		}
		calc.setStartDate(one);
		calc.setEndDate(two);
		assertEquals(2, calc.calculateDays());

		two.setDate("26 May 07");
		/*
		 * (it's NOT a leap year; it's a year + one day, just as Monday to
		 * Monday is eight days)
		 */
		assertEquals(366, calc.calculateDays());

		Datestamp first = new Datestamp("1 Jan 03");
		Datestamp second = new Datestamp("31 Dec 03");
		calc = new RangeCalculator(first, second);
		assertEquals(365, calc.calculateDays());

	}

	public final void testCalculateWeeks() throws ParseException {
		Datestamp first = new Datestamp("1 Jan 03");
		Datestamp second = new Datestamp("31 Dec 03");

		RangeCalculator calc = new RangeCalculator(first, second);
		assertEquals(52.0, calc.calculateWeeks(), 0.001);

		second.setDate("28 Jan 03");
		assertEquals(4.0, calc.calculateWeeks(), 0.001);
	}

	public final void testCalculateMonths() {
		Datestamp alpha = new Datestamp("1 Jul 05");
		Datestamp omega = new Datestamp("31 Dec 05");

		RangeCalculator calc = new RangeCalculator(alpha, omega);
		assertEquals(6.0, calc.calculateMonths(), 0.001);

		Datestamp one = new Datestamp("31 Jul 05");
		calc.setEndDate(one);
		assertEquals(1.0, calc.calculateMonths(), 0.001);
	}
}
