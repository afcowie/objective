/*
 * ForeignAmount.java
 * 
 * See LICENCE file for usage and redistribution terms
 * Copyright (c) 2005 Operational Dynamics
 */
package accounts.domain;

import java.math.BigDecimal;

public class ForeignAmount extends Amount
{
	/**
	 * The decimal precision of exchange rate Strings
	 */
	public static final int	RATE_DECIMAL_PLACES	= 5;

	private Currency		currency;
	/**
	 * The number of cents representing the foreign value we were given.
	 */
	private long			foreignNumber;
	/**
	 * The exchange rate. Multiply by this factor to get the value in the home
	 * currency.
	 */
	private String			rate;

	public ForeignAmount() {
		super();
	}

	/**
	 * .
	 * 
	 * @param faceValue
	 *            The amount denominated in the foreign (origin) currency
	 * @param cur
	 *            The currency that the foreign amount is in
	 * @param rate
	 *            The exchange rate between the foreign currency and the home
	 *            currency.
	 */
	public ForeignAmount(String foreignValue, Currency cur, String rate) {
		this.setForeignValue(foreignValue);
		this.setCurrency(cur);
		this.setRate(rate);
	}

	/**
	 * Get the face value of the ForeignAmount. Note that ForeignAmount does
	 * <b>not</b> override Amount.getValue(), and that is always the value in
	 * home currency terms. Most of the time, you'll want {@link #toString()} as
	 * opposed to this method.
	 * 
	 * @return a String representing the number with two (and only two) decimal
	 *         places.
	 */
	public String getForeignValue() {
		return numberToString(foreignNumber);
	}

	/**
	 * If rate is set, then recalculation will happen.
	 * 
	 * @param faceValue
	 *            The foreign denominated value to set
	 */
	public void setForeignValue(String faceValue) {
		this.foreignNumber = super.stringToNumber(faceValue);
		if (this.rate == null) {
			return;
		}
		BigDecimal r = new BigDecimal(this.rate);
		recalculateHomeGivenRate(r);
	}

	protected void setForeignValue(BigDecimal faceValue) {
		this.foreignNumber = super.bigToNumber(faceValue);
	}

	/**
	 * Get the Currency in which this ForeignAmount is denominated.
	 * 
	 * @return
	 */
	public Currency getCurrency() {
		return currency;
	}

	/**
	 * If the currency is changed, then obviously the exchange rate needs to
	 * change. <b>This method does not recalculate anything, so you need to call
	 * setRate() or setHomeValue()...</b>
	 * 
	 * @param cur
	 */
	public void setCurrency(Currency cur) {
		this.currency = cur;
		// TODO FIXME?
	}

	/**
	 * Get the exchange rate.
	 */
	public String getRate() {
		return rate;
	}

	/**
	 * Whenever the rate is set or changed via this method, the internal (home
	 * currency) value of the ForeignAmount is recalculated.
	 * 
	 * @throws NumberFormatException
	 *             if BigDecimal can't parse the rate as a decimal number
	 */
	public void setRate(String rate) {
		BigDecimal r = new BigDecimal(rate);
		recalculateHomeGivenRate(r);
		BigDecimal reducedNumber = r.setScale(RATE_DECIMAL_PLACES, BigDecimal.ROUND_HALF_UP);
		this.rate = reducedNumber.toString();
	}

	/*
	 * Overrides ------------------------------------------
	 */

	/**
	 * Overrides Amount's setValue() in order to cause recalculation of the
	 * exchange rate (then calls super.setValue).
	 */
	public void setValue(String homeValue) {
		super.setValue(homeValue);
		BigDecimal h = super.getNumber();
		recalculateRateGivenHome(h);
	}

	/*
	 * Internal conversion routines -----------------------
	 */

	/**
	 * Given a [new] rate and (and assuming a foreign value), calculate a new
	 * home value.
	 * 
	 * @param r
	 *            a BigDecimal representing the exchange rate
	 */
	private void recalculateHomeGivenRate(BigDecimal r) {
		BigDecimal f = new BigDecimal(numberToString(foreignNumber));
		BigDecimal h = f.multiply(r);
		super.setValue(h);
	}

	/**
	 * Given a [new] home currency value String (and assuming a foreign value),
	 * calculate a new exchange rate.
	 * 
	 * @param h
	 *            a BigDecimal representing the home value
	 */
	private void recalculateRateGivenHome(BigDecimal h) {
		BigDecimal f = new BigDecimal(numberToString(foreignNumber));
		if (foreignNumber == 0) {
			// avoid divide by zero
			return;
		}
		/*
		 * home = foreign * rate ; rate = home / foregin
		 */
		BigDecimal r = h.divide(f, RATE_DECIMAL_PLACES, BigDecimal.ROUND_HALF_UP);
		this.rate = r.toString();
	}

	/**
	 * Internally, everything is stored in terms of the home currency, with the
	 * foreign amount converted at the specified exchange rate. Most uses,
	 * however, will want to display the original foreign value that was
	 * entered. We could have overridden getValue(), but I think it better that
	 * that always returns the value in home terms. So we override toString()
	 * instead.
	 * 
	 * @return a formatted String representing the foreign amount. This string
	 *         is <b>not</b> embellished with currency symbol or currency code,
	 *         as that is best achieved by the GUI code. See TODO.
	 */
	public String toString() {
		return padComma(numberToString(foreignNumber));
	}

	public Object clone() {
		ForeignAmount obj = new ForeignAmount();
		// Copy the long
		obj.foreignNumber = this.foreignNumber;
		// Copy the reference
		obj.currency = this.currency;
		// new String
		obj.rate = new String(this.rate);
		// now that we have a new object, just set it's value
		obj.setValue(this.getValue());
		return obj;
	}
}
