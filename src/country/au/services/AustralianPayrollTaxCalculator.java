/*
 * AustralianPayrollTaxCalculator.java
 * 
 * See LICENCE file for usage and redistribution terms
 * Copyright (c) 2006 Operational Dynamics
 */
package country.au.services;

import generic.persistence.DataClient;
import generic.persistence.NotActivatedException;

import java.math.BigDecimal;

import accounts.domain.Amount;
import accounts.domain.Datestamp;
import accounts.services.NotFoundException;
import accounts.services.PayrollTaxCalculator;
import country.au.domain.AustralianPayrollTaxIdentifier;

/**
 * Calculate the "Pay As You Go" (PAYG) withholding to be deducted from a given
 * salary or due as a result of a given payment.
 * <p>
 * The ATO formulas are based on weekly payments; other periods have to be
 * normalized to per week.
 * 
 * @author Andrew Cowie
 */
public class AustralianPayrollTaxCalculator extends PayrollTaxCalculator
{
	double[][]	coefficients;
	float		weeks;

	/**
	 * Sets 1 as a default number of weeks.
	 * 
	 * @param store
	 *            the DataClient from which to fetch tax Identifiers and tax
	 *            table data.
	 * @param asAtDate
	 *            allows the calculator to pick which set of tax table data is
	 *            appropriate.
	 * @throws IllegalStateException
	 *             thorwn from the Finder if you've managed to ask for tax data
	 *             that isn't there.
	 */
	public AustralianPayrollTaxCalculator(DataClient store, AustralianPayrollTaxIdentifier scale,
		Datestamp asAtDate) throws NotFoundException {

		super(asAtDate);
		if (scale == null) {
			throw new NotActivatedException();
		}

		AustralianPayrollTaxTableFinder finder = new AustralianPayrollTaxTableFinder(scale, asAtDate);
		finder.query(store);
		this.coefficients = finder.getCoefficients();
		this.weeks = 1.0f;
	}

	/**
	 * @return the number of weeks that are currently in use to make this
	 *         calculation.
	 */
	public float getWeeks() {
		return weeks;
	}

	/**
	 * Set the number of weeks this pay encompasses. This is used to normalize
	 * the salary or paycheck to the magnitude of single weeks, which is the
	 * basis on which the ATO PAYG tables are defined.
	 */

	public void setWeeks(float weeks) {
		if (weeks <= 0.0) {
			throw new IllegalArgumentException("The number of weeks must be positive and non-zero");
		}
		this.weeks = weeks;
	}

	/**
	 * Calculate the withholding due as a result of a given paycheck amount. A
	 * common use case is that a given sum of money is paid/given/transfered to
	 * an employee; the appropriate amount of PAYG needs to be calculated and,
	 * added to the actual paycheck disbursed, equals the "salary" or wage that
	 * that person received.
	 * <p>
	 * This uses calculateGivenSalary() which is where the logic from the ATO
	 * tax tables is embedded.
	 */
	public void calculateGivenPayable() {
		if (paycheck.isZero()) {
			salary.setNumber(0);
			withhold.setNumber(0);
			return;
		}

		/*
		 * Brute force: start with paycheck amount (taxes payable could be zero,
		 * after all), then increment by $1 and recalculate. Limit: we will
		 * assume taxes aren't going to be more than 90% of salary, so ... FIXME
		 */

		Amount original = (Amount) paycheck.clone();
		Amount candidate = (Amount) paycheck.clone();
		Amount limit = new Amount(paycheck.getNumber() * 10);

		for (; candidate.compareTo(limit) < 0; candidate.incrementBy(new Amount("1.00"))) {
			salary.setValue(candidate);
			calculateGivenSalary();

			// System.out.println(candidate + " - " + withhold + " = " +
			// paycheck);
			if (salary.getNumber() - withhold.getNumber() >= original.getNumber()) {
				return;
			}
			paycheck.setValue(original);
		}
		throw new IllegalStateException("Couldn't calculate withholding given paycheck amount");
	}

	/**
	 * Calcualte the PAYG withholding amount given the salary that is set. This
	 * is of course the traditional use case.
	 */
	public void calculateGivenSalary() {
		if ((salary == null) || (withhold == null) || (paycheck == null)) {
			throw new IllegalStateException(
				"To use a Calculator you need to have all the parameter values set with instantiated objects");
		}

		BigDecimal num = new BigDecimal(weeks);
		BigDecimal weekly = salary.getBigDecimal().divide(num, 3, BigDecimal.ROUND_HALF_UP);

		/*
		 * The Australian PAYG formula is the linear equation y = ax - b, where
		 * x is {weekly earnings~drop cents+0.99} and then rounding y to the
		 * nearest whole dollar.
		 */

		int nocents = weekly.intValue();
		double x = nocents + 0.99;

		/*
		 * Now fetch the appropriate coefficients
		 */
		double a = 0, b = 0;

		for (int i = 0; i < coefficients.length; i++) {
			if (x > coefficients[i][0]) {
				continue;
			}
			a = coefficients[i][1];
			b = coefficients[i][2];
			break;
		}

		double y;

		y = a * x - b;

		long rounded = Math.round(y);

		/*
		 * And that ends the ATO tax algorithm. Now we have to de-normalize the
		 * value scaling it up by the number of weeks, and passing that to be
		 * the value of the withheld Amount:
		 */

		float denormalized = rounded * weeks;
		withhold.setValue(Float.toString(denormalized));

		/*
		 * And now reset the paycheck amount.
		 */
		paycheck.setValue(salary);
		paycheck.decrementBy(withhold);
	}
}
