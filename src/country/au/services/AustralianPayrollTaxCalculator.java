/*
 * AustralianPayrollTaxCalculator.java
 * 
 * See LICENCE file for usage and redistribution terms
 * Copyright (c) 2006 Operational Dynamics
 */
package country.au.services;

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

	/**
	 * @param asAtDate
	 *            allows the calculator to pick which set of tax table data is
	 *            appropriate
	 * @throws IllegalStateException
	 *             thorwn from the Finder if you've managed to ask for tax data
	 *             that isn't there.
	 */
	public AustralianPayrollTaxCalculator(AustralianPayrollTaxIdentifier scale, Datestamp asAtDate)
		throws NotFoundException {

		super(asAtDate);

		AustralianPayrollTaxTableFinder finder = new AustralianPayrollTaxTableFinder(scale, asAtDate);
		this.coefficients = finder.getCoefficients();
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

		BigDecimal weekly = salary.getBigDecimal();

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

		withhold.setValue(Long.toString(rounded) + ".00");

		/*
		 * And now reset the paycheck amount.
		 */
		paycheck.setValue(salary.getValue());
		paycheck.decrementBy(withhold);
	}
}
