/*
 * AustralianPayrollTaxCalculatorTest.java
 * 
 * See LICENCE file for usage and redistribution terms
 * Copyright (c) 2006 Operational Dynamics
 */
package country.au.services;

import accounts.domain.Amount;
import accounts.domain.Datestamp;
import accounts.persistence.BlankDatafileTestCase;
import accounts.services.NotFoundException;
import accounts.services.PayrollTaxCalculator;
import country.au.domain.AustralianPayrollTaxIdentifier;

/**
 * Unit test the PAYG tax calculator and its associated Identifiers, Finders and
 * underlying TaxTable data. Since the Australian varient requires TaxTable data
 * to be in the DataClient already, we use AustralianInitBooksCommand to set up
 * the constants and in turn store the coefficient data.
 * 
 * @author Andrew Cowie
 */
public class AustralianPayrollTaxTest extends BlankDatafileTestCase
{
	private static final Datestamp	KNOWN_GOOD_DATE	= new Datestamp("12 Dec 05");

	static {
		DATAFILE = "tmp/unittests/AustralianPayrollTaxTest.yap";
	}

	/**
	 * First setup a demo database with PAYG tax data, and verify it was stored.
	 */
	public final void testEnsureTableDataInitialized() {
		/*
		 * Initialize a set of books with some identifiers in it already.
		 * AustralianInitBooksCommand which in turn calls
		 * StoreAustralianPayrollTaxTablesCommand...
		 */
		try {
			AustralianInitBooksCommand aibc = new AustralianInitBooksCommand();
			aibc.execute(rw);
			rw.commit();
		} catch (Exception e) {
			fail("Exception caught trying to further initialize datastore: " + e);
		}

		AustralianPayrollTaxTableFinder f = new AustralianPayrollTaxTableFinder(
			AustralianPayrollTaxIdentifier.NO_TAXFREE_THRESHOLD, KNOWN_GOOD_DATE);
		try {
			f.query(rw);
		} catch (NotFoundException nfe) {
			fail("Running the tax tables store command doesn't seem to have worked.");
		}
		/*
		 * So if that threw, then we have an error condition that needs to stop
		 * the show; tax data should have been initialized already.
		 */
		try {
			double[][] coefficients = f.getCoefficients();
			assertNotNull(coefficients);

			int NO_TAXFREE_THRESHOLD_length = 4;
			assertEquals(
				"If this failed, simply make sure that the NO_TAXFREE_THRESHOLD_length variable is the length of the NO_TAXFREE_THRESHOLD data for "
					+ KNOWN_GOOD_DATE + "; we were expecting " + NO_TAXFREE_THRESHOLD_length, 4,
				coefficients.length);
			for (int i = 0; i < NO_TAXFREE_THRESHOLD_length; i++) {
				assertEquals(3, coefficients[i].length);
			}
		} catch (NotFoundException nfe) {
			fail("Huh? This was supposed to be good data.");
		}
	}

	public final void testTaxDataFinderNotFinding() {
		new AustralianPayrollTaxConstants(rw).loadIdentifiers();
		/*
		 * Try it with a bogus identifier. This only needs to try query().
		 */
		AustralianPayrollTaxTableFinder bogus = new AustralianPayrollTaxTableFinder(
			new AustralianPayrollTaxIdentifier("Bogus", 0), KNOWN_GOOD_DATE);
		try {
			bogus.query(rw);
			fail("Running the tax tables finder against a bogus Identifier didn't throw like it should have.");
		} catch (NotFoundException nfe) {
			// good.
		}
		/*
		 * Try to find appropriate tax data for a date so clearly far in tha
		 * past that it predates our software.
		 */
		assertNotNull(AustralianPayrollTaxIdentifier.NO_TAXFREE_THRESHOLD);

		AustralianPayrollTaxTableFinder badDate = new AustralianPayrollTaxTableFinder(
			AustralianPayrollTaxIdentifier.NO_TAXFREE_THRESHOLD, new Datestamp("5 May 91"));
		try {
			// will pass - there is a NO_TAXFREE_THRESHOLD
			badDate.query(rw);
			// but this should throw because of the date
			badDate.getCoefficients();
			fail("Running the tax tables finder with an ancient date should have thrown.");
		} catch (NotFoundException nfe) {
			// good.
		}
	}

	public final void testGettersSetters() throws NotFoundException {
		new AustralianPayrollTaxConstants(rw).loadIdentifiers();

		Amount positiveOne = new Amount("640.22");
		Amount zero = new Amount("0.00");
		Amount negative = new Amount("-0.01");

		PayrollTaxCalculator calc = new AustralianPayrollTaxCalculator(rw,
			AustralianPayrollTaxIdentifier.NO_TAXFREE_THRESHOLD, KNOWN_GOOD_DATE);

		try {
			calc.setSalary(zero);
		} catch (Exception e) {
			fail("setSalary threw " + e + " with a zero Amount. Drat");
		}

		try {
			calc.setSalary(negative);
			fail("setSalary failed to throw an exception on a negative value");
		} catch (IllegalArgumentException iae) {
		}
		try {
			calc.setSalary(positiveOne);
		} catch (IllegalArgumentException iae) {
			fail("setSalary threw an exception on a postive value");
		}

		/*
		 * Now paycheck
		 */
		try {
			calc.setPaycheck(negative);
			fail("setPaycheck failed to throw an exception on a negative value");
		} catch (IllegalArgumentException iae) {
		}

		/*
		 * Make sure paycheck's object identity check is ok
		 */
		try {
			calc.setPaycheck(positiveOne);
			fail("setPaycheck failed to throw an exception on an already used Amount");
		} catch (IllegalArgumentException iae) {
		}

		Amount positiveTwo = new Amount("500.00");
		try {
			calc.setPaycheck(positiveTwo);
		} catch (IllegalArgumentException iae) {
			fail("setSalary threw an exception on a postive value");
		}

		/*
		 * Now make sure other object identity tests passes
		 */
		try {
			calc.setSalary(positiveTwo);
			fail("setSalaryfailed to throw an exception on an already used Amount");
		} catch (IllegalArgumentException iae) {
		}

		assertSame(calc.getSalary(), positiveOne);
		assertSame(calc.getPaycheck(), positiveTwo);
	}

	/**
	 * Bug that cropped up when we switched from using new Amounts to calls to
	 * setValue()
	 */
	public final void testCalculatorAllFactorsSet() throws NotFoundException {
		new AustralianPayrollTaxConstants(rw).loadIdentifiers();

		PayrollTaxCalculator calc = new AustralianPayrollTaxCalculator(rw,
			AustralianPayrollTaxIdentifier.TAXFREE_THRESHOLD_WITH_LEAVE_LOADING, KNOWN_GOOD_DATE);
		try {
			calc.calculateGivenSalary();
			fail("Should have thrown IllegalStateException");
		} catch (NullPointerException npe) {
			fail("Threw NullPointerException (since withhold not set)");
		} catch (IllegalStateException ise) {
			// that's what should happen
		}

		calc.setSalary(new Amount());

		try {
			calc.calculateGivenSalary();
			fail("Should have thrown IllegalStateException");
		} catch (NullPointerException npe) {
			fail("Threw NullPointerException (since withhold not set)");
		} catch (IllegalStateException ise) {
			// that's what should happen
		}

		calc.setPaycheck(new Amount());

		try {
			calc.calculateGivenSalary();
			fail("Should have thrown IllegalStateException");
		} catch (NullPointerException npe) {
			fail("Threw NullPointerException (since withhold not set)");
		} catch (IllegalStateException ise) {
			// that's what should happen
		}

		calc.setWithhold(new Amount());

		try {
			calc.calculateGivenSalary();
			// should now succeed
		} catch (Exception e) {
			fail("Unexpected exception where Calculator should now function");
		}

		assertEquals(0, calc.getWithhold().getNumber());
	}

	public final void testCalculateWithholdGivenSalary() throws NotFoundException {
		new AustralianPayrollTaxConstants(rw).loadIdentifiers();

		Amount weeklyEarnings = new Amount("409.00");

		PayrollTaxCalculator calc = new AustralianPayrollTaxCalculator(rw,
			AustralianPayrollTaxIdentifier.TAXFREE_THRESHOLD_WITH_LEAVE_LOADING, KNOWN_GOOD_DATE);
		calc.setSalary(weeklyEarnings);

		// mock
		calc.setWithhold(new Amount());
		calc.setPaycheck(new Amount());

		calc.calculateGivenSalary();
		assertEquals("52.00", calc.getWithhold().toString());

	}

	public final void testCalculateWithholdGivenPayable() throws NotFoundException {
		new AustralianPayrollTaxConstants(rw).loadIdentifiers();

		Amount weeklyPaycheck = new Amount("231.00");

		PayrollTaxCalculator calc = new AustralianPayrollTaxCalculator(rw,
			AustralianPayrollTaxIdentifier.TAXFREE_THRESHOLD_WITH_LEAVE_LOADING, KNOWN_GOOD_DATE);
		calc.setPaycheck(weeklyPaycheck);

		// mock
		calc.setSalary(new Amount());
		calc.setWithhold(new Amount());

		calc.calculateGivenPayable();
		assertEquals("22.00", calc.getWithhold().getValue());
		assertEquals("253.00", calc.getSalary().getValue());
	}

	public final void testAgainstOfficialSampleData() throws NotFoundException {
		new AustralianPayrollTaxConstants(rw).loadIdentifiers();

		PayrollTaxCalculator calc;

		calc = new AustralianPayrollTaxCalculator(rw,
			AustralianPayrollTaxIdentifier.TAXFREE_THRESHOLD_NO_LEAVE_LOADING, KNOWN_GOOD_DATE);
		calc.setWithhold(new Amount());
		calc.setPaycheck(new Amount());

		calc.setSalary(new Amount("111.00"));
		calc.calculateGivenSalary();
		assertEquals("0.00", calc.getWithhold().getValue());

		calc.setSalary(new Amount("300.00"));
		calc.calculateGivenSalary();
		assertEquals("28.00", calc.getWithhold().getValue());

		calc.setSalary(new Amount("1587.00"));
		calc.calculateGivenSalary();
		assertEquals("466.00", calc.getWithhold().getValue());

		calc.setSalary(new Amount("1828.00"));
		calc.calculateGivenSalary();
		assertEquals("571.00", calc.getWithhold().getValue());

		calc = new AustralianPayrollTaxCalculator(rw, AustralianPayrollTaxIdentifier.NO_TFN_PROVIDED,
			KNOWN_GOOD_DATE);
		calc.setWithhold(new Amount());
		calc.setPaycheck(new Amount());

		calc.setSalary(new Amount("50.00"));
		calc.calculateGivenSalary();
		assertEquals("24.00", calc.getWithhold().getValue());

		calc.setSalary(new Amount("100.00"));
		calc.calculateGivenSalary();
		assertEquals("48.00", calc.getWithhold().getValue());

		calc.setSalary(new Amount("1000.00"));
		calc.calculateGivenSalary();
		assertEquals("485.00", calc.getWithhold().getValue());

		calc.setSalary(new Amount("10000.00"));
		calc.calculateGivenSalary();
		assertEquals("4850.00", calc.getWithhold().getValue());
	}

	/**
	 * When all the above tests were written, they were built around the ATO's
	 * tables' normalization of pay amounts to 1 week. Now weadd a parameter to
	 * the calculator to specify the number of weeks involved, which should
	 * simply scale the returned results.
	 */
	public final void testCalculateWithWeeks() throws NotFoundException {
		new AustralianPayrollTaxConstants(rw).loadIdentifiers();

		Amount knownSalary = new Amount("6572.00");

		AustralianPayrollTaxCalculator calc = new AustralianPayrollTaxCalculator(rw,
			AustralianPayrollTaxIdentifier.TAXFREE_THRESHOLD_WITH_LEAVE_LOADING, KNOWN_GOOD_DATE);
		calc.setWeeks(26);
		calc.setSalary(knownSalary);

		// mock
		calc.setPaycheck(new Amount());
		calc.setWithhold(new Amount());

		calc.calculateGivenSalary();
		assertEquals("572.00", calc.getWithhold().getValue());
		assertEquals("6000.00", calc.getPaycheck().getValue());

		/*
		 * Now try a month
		 */
		knownSalary = new Amount("1096.33");

		calc = new AustralianPayrollTaxCalculator(rw,
			AustralianPayrollTaxIdentifier.TAXFREE_THRESHOLD_WITH_LEAVE_LOADING, KNOWN_GOOD_DATE);
		calc.setWeeks(52.0f / 12.0f);
		calc.setSalary(knownSalary);

		// mock
		calc.setPaycheck(new Amount());
		calc.setWithhold(new Amount());

		calc.calculateGivenSalary();
		assertEquals("95.33", calc.getWithhold().getValue());
		assertEquals("1001.00", calc.getPaycheck().getValue());

		last = true;
	}
}
