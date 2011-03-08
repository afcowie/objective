/*
 * ObjectiveAccounts, accounting for small professional services firms.
 *
 * Copyright © 2006-2011 Operational Dynamics Consulting, Pty Ltd
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
package country.au.services;

import objective.domain.Datestamp;
import objective.services.NotFoundException;
import accounts.domain.Amount;
import accounts.persistence.BlankDatafileTestCase;
import accounts.services.PayrollTaxCalculator;
import accounts.services.RangeCalculator;
import country.au.domain.AustralianPayrollTaxIdentifier;

/**
 * Unit test the PAYG tax calculator and its associated Identifiers, Finders
 * and underlying TaxTable data. Since the Australian varient requires
 * TaxTable data to be in the DataClient already, we use
 * AustralianInitBooksCommand to set up the constants and in turn store the
 * coefficient data.
 * 
 * @author Andrew Cowie
 */
public class AustralianPayrollTaxTest extends BlankDatafileTestCase
{
    private static final Datestamp KNOWN_GOOD_DATE = new Datestamp("12 Dec 05");

    static {
        DATAFILE = "tmp/unittests/AustralianPayrollTaxTest.yap";
    }

    /**
     * First setup a demo database with PAYG tax data, and verify it was
     * stored.
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
         * So if that threw, then we have an error condition that needs to
         * stop the show; tax data should have been initialized already.
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
        assertEquals("95.00", calc.getWithhold().getValue());
        assertEquals("1001.33", calc.getPaycheck().getValue());
    }

    /**
     * Test a bug that crept in when we added the normalization to weeks.
     */
    public final void testCalculateCorrectSalaryWithoutCentsBug() throws NotFoundException {
        RangeCalculator range;

        new AustralianPayrollTaxConstants(rw).loadIdentifiers();

        AustralianPayrollTaxCalculator calc = new AustralianPayrollTaxCalculator(rw,
                AustralianPayrollTaxIdentifier.TAXFREE_THRESHOLD_WITH_LEAVE_LOADING, KNOWN_GOOD_DATE);

        /*
         * We have trouble because, again non-zero cents:
         */
        range = new RangeCalculator();
        range.setStartDate(new Datestamp("1 Jul 05"));
        range.setEndDate(new Datestamp("7 Jul 05"));

        float weeks = range.calculateWeeks();
        assertEquals(1.0f, weeks, 0.01);
        calc.setWeeks(weeks);

        calc.setSalary(new Amount("252.00"));
        calc.setWithhold(new Amount());
        calc.setPaycheck(new Amount());

        calc.calculateGivenSalary();
        assertEquals(0, calc.getSalary().getNumber() - 25200);
        assertEquals(0, calc.getWithhold().getNumber() - 2200);
        assertEquals(0, calc.getPaycheck().getNumber() - 23000);

        /*
         * Now try given payable:
         */

        calc.setPaycheck(new Amount("230.00"));
        calc.setSalary(new Amount());
        calc.setWithhold(new Amount());
        calc.calculateGivenPayable();

        assertEquals(0, calc.getSalary().getNumber() - 25200);
        assertEquals(0, calc.getWithhold().getNumber() - 2200);
        assertEquals(0, calc.getPaycheck().getNumber() - 23000);

        /*
         * Now test the known values case of $6000 paycheck in 6 months. It
         * turns out that approximating 26 as the number of weeks in 6 months
         * is wrong and leads to a different answer than with 1 Jul - 31 Dec.
         */

        range = new RangeCalculator();
        range.setStartDate(new Datestamp("1 Jul 05"));
        range.setEndDate(new Datestamp("31 Dec 05"));

        weeks = range.calculateWeeks();
        assertEquals(26.2, weeks, 0.1);
        calc.setWeeks(weeks);

        calc.setSalary(new Amount("6550.00"));
        // mock
        calc.setWithhold(new Amount());
        calc.setPaycheck(new Amount());

        calc.calculateGivenSalary();

        assertEquals("6550.00", calc.getSalary().getValue());
        assertEquals("550.00", calc.getWithhold().getValue());
        assertEquals("6000.00", calc.getPaycheck().getValue());

        /*
         * Now try the reverse direction
         */

        calc.setPaycheck(new Amount("6000.00"));
        // mock
        calc.setSalary(new Amount());
        calc.setWithhold(new Amount());

        calc.calculateGivenPayable();

        /*
         * And evaluate the results. The bug is that we aren't getting the
         * value back for payable that we put in, manifesting itself as
         * nonzero cents...
         */
        assertEquals("6000.00", calc.getPaycheck().getValue());
        assertEquals("550.00", calc.getWithhold().getValue());
        assertEquals("6550.00", calc.getSalary().getValue());

        last = true;
    }
}
