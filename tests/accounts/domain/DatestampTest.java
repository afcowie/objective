/*
 * DatestampTest.java
 * 
 * See LICENCE file for usage and redistribution terms
 * Copyright (c) 2005 Operational Dynamics
 */
package accounts.domain;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import junit.framework.TestCase;

public class DatestampTest extends TestCase
{

	public void testValidDatestampStringInput() {
		Datestamp stamp = new Datestamp();

		try {
			stamp.setDate("28 Dec 73");
		} catch (ParseException pe) {
			fail("Shouldn't have thrown ParseException" + pe.getMessage());
		}
		assertEquals("First test failed", "28 Dec 73", stamp.toString());

		/*
		 * Implicit year should feed off of the existing stamp, whatever it is
		 */
		try {
			stamp.setDate("28 Dec");
		} catch (ParseException pe) {
			fail("Shouldn't have thrown ParseException" + pe.getMessage());
		}
		assertEquals("Assumed existing year test failed", "28 Dec 73", stamp.toString());

		/*
		 * Now test the default of using the current year.
		 */
		stamp = new Datestamp();
		try {
			stamp.setDate("28 Dec");
		} catch (ParseException pe) {
			fail("Shouldn't have thrown ParseException" + pe.getMessage());
		}
		SimpleDateFormat sdf = new SimpleDateFormat("yy");
		String expectedYear = sdf.format(new Date());
		assertEquals("Current year failed", "28 Dec " + expectedYear, stamp.toString());

		/*
		 * Now test the input with only a day
		 */
		stamp = new Datestamp();
		try {
			stamp.setDate("15");
		} catch (ParseException pe) {
			fail("'15' shouldn't have thrown ParseException" + pe.getMessage());
		}
		sdf = new SimpleDateFormat("MMM");
		String expectedMonth = sdf.format(new Date());
		assertEquals("Day only failed", "15 " + expectedMonth + " " + expectedYear, stamp.toString());
	}

	public void testInvalidDatestampStringInput() {
		Datestamp stamp;
		stamp = new Datestamp();
		try {
			stamp.setDate("15 Janvier 2005");
			fail("Should have thrown exception");
		} catch (ParseException pe) {
		}

		stamp = new Datestamp();
		try {
			stamp.setDate("15 Fev");
			fail("Should have thrown exception");
		} catch (ParseException pe) {
		}

		stamp = new Datestamp();
		try {
			stamp.setDate("15 Jan 1907");
			fail("Should have parsed as " + stamp.toString());
		} catch (ParseException pe) {
		}

		
	}
}
