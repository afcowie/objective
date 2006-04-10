/*
 * AustralianPayrollTaxCalculatorTest.java
 * 
 * See LICENCE file for usage and redistribution terms
 * Copyright (c) 2006 Operational Dynamics
 */
package country.au.services;

import java.io.File;
import java.io.FileNotFoundException;

import junit.framework.TestCase;
import accounts.client.ObjectiveAccounts;
import accounts.domain.Amount;
import accounts.domain.Datestamp;
import accounts.persistence.UnitOfWork;
import accounts.services.DatafileServices;
import accounts.services.NotFoundException;
import accounts.services.PayrollTaxCalculator;
import country.au.domain.AustralianPayrollTaxIdentifier;

/**
 * Unit test the PAYG tax calculator and its associated Identifiers, Finders and
 * underlying TaxTable data. Since the Australian varient requires TaxTable data
 * to be in the DataStore already, we use AustralianInitBooksCommand to set up
 * the constants and in turn store the coefficient data.
 * 
 * @author Andrew Cowie
 */
public class AustralianPayrollTaxTest extends TestCase
{
	private static final Datestamp	KNOWN_GOOD_DATE	= new Datestamp("12 Dec 05");

	public static final String		TESTS_DATABASE	= "tmp/unittests/AustralianPayrollTaxTest.yap";
	private static boolean			initialized		= false;

	private void init() {
		new File(TESTS_DATABASE).delete();
		ObjectiveAccounts.store = DatafileServices.newDatafile(TESTS_DATABASE);

		try {
			UnitOfWork uow = new UnitOfWork("init");
			AustralianInitBooksCommand aibc = new AustralianInitBooksCommand();
			aibc.execute(uow);
			uow.commit();

		} catch (Exception e) {
			fail("Exception caught trying to init(): " + e);
		}

		ObjectiveAccounts.store.close();
		initialized = true;
	}

	public void setUp() {
		if (!initialized) {
			init();
		}
		try {
			ObjectiveAccounts.store = DatafileServices.openDatafile(TESTS_DATABASE);
		} catch (FileNotFoundException fnfe) {
			fail("Where is the test database?");
		}
	}

	public void tearDown() {
		ObjectiveAccounts.store.close();
	}

	public final void testTableDataInitialized() {
		/*
		 * The init() above runs AustralianInitBooksCommand which in turn calls
		 * StoreAustralianPayrollTaxTablesCommand so we should be all set up...
		 */
		AustralianPayrollTaxTableFinder f = new AustralianPayrollTaxTableFinder(
			AustralianPayrollTaxIdentifier.NO_TAXFREE_THRESHOLD, KNOWN_GOOD_DATE);
		try {
			f.query();
		} catch (NotFoundException nfe) {
			fail("Running the tax tables store command doesn't seem to have worked.");
		}
		/*
		 * So if that threw, then we have an error condition that needs to stop
		 * the show; tax data should have been initialized already.
		 */
	}

	public final void testTaxDataFinderNotFinding() {
		/*
		 * Try it with a bogus identifier. This only needs to try query().
		 */
		AustralianPayrollTaxTableFinder bogus = new AustralianPayrollTaxTableFinder(new AustralianPayrollTaxIdentifier(
			"Bogus"), KNOWN_GOOD_DATE);
		try {
			bogus.query();
			fail("Running the tax tables finder against a bogus Identifier didn't throw like it should have.");
		} catch (NotFoundException nfe) {
			// good.
		}
		/*
		 * Try to find appropriate tax data for a date so clearly far in tha
		 * past that it predates our software.
		 */
		AustralianPayrollTaxTableFinder badDate = new AustralianPayrollTaxTableFinder(
			AustralianPayrollTaxIdentifier.NO_TAXFREE_THRESHOLD, new Datestamp("5 May 91"));
		try {
			badDate.query(); // will pass - there is a NO_TAXFREE_THRESHOLD
			badDate.getCoefficients(); // but this should throw because of the
			// date
			fail("Running the tax tables finder with an ancient date should have thrown.");
		} catch (NotFoundException nfe) {
			// good.
		}
	}

	public final void testGettersSetters() throws NotFoundException {
		Amount positiveOne = new Amount("640.22");
		Amount zero = new Amount("0.00");
		Amount negative = new Amount("-0.01");

		PayrollTaxCalculator calc = new AustralianPayrollTaxCalculator(
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
		PayrollTaxCalculator calc = new AustralianPayrollTaxCalculator(
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
		Amount weeklyEarnings = new Amount("409.00");

		PayrollTaxCalculator calc = new AustralianPayrollTaxCalculator(
			AustralianPayrollTaxIdentifier.TAXFREE_THRESHOLD_WITH_LEAVE_LOADING, KNOWN_GOOD_DATE);
		calc.setSalary(weeklyEarnings);

		// mock
		calc.setWithhold(new Amount());
		calc.setPaycheck(new Amount());

		calc.calculateGivenSalary();
		assertEquals("52.00", calc.getWithhold().toString());

	}

	public final void testCalculateWithholdGivenPayable() throws NotFoundException {
		Amount weeklyPaycheck = new Amount("231.00");

		PayrollTaxCalculator calc = new AustralianPayrollTaxCalculator(
			AustralianPayrollTaxIdentifier.TAXFREE_THRESHOLD_WITH_LEAVE_LOADING, KNOWN_GOOD_DATE);
		calc.setPaycheck(weeklyPaycheck);

		// mock
		calc.setSalary(new Amount());
		calc.setWithhold(new Amount());

		calc.calculateGivenPayable();
		assertEquals("22.00", calc.getWithhold().toString());
		assertEquals("253.00", calc.getSalary().toString());
	}
}
