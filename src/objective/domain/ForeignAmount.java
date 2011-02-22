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

import java.math.BigDecimal;


/**
 * An amount of money in a foreign currency. ForeignAmount encapsulates the
 * notions of face value (the amount as demoninated in the foreign currency's
 * terms), currency (which country's money the face value is in), and exchange
 * rate.
 * 
 * @author Andrew Cowie
 */
public class ForeignAmount extends Amount
{
    /**
     * The decimal precision of exchange rate Strings
     */
    public static final int RATE_DECIMAL_PLACES = 5;

    private Currency currency;

    private ForeignAmount() {
        super(0);
    }

    /**
     * The number of cents representing the foreign value we were given.
     */
    private long foreignNumber;

    /**
     * The exchange rate. Multiply by this factor to get the value in the home
     * currency.
     */
    private String rate;

    /**
     * @param faceValue
     *            The amount denominated in the foreign (origin) currency
     * @param cur
     *            The currency that the foreign amount is in
     * @param rate
     *            The exchange rate between the foreign currency and the home
     *            currency.
     */
    public ForeignAmount(String faceValue, Currency cur, String rate) {
        super(0);
        this.setForeignValue(faceValue);
        this.setCurrency(cur);
        this.setRate(rate);
    }

    /**
     * Get the face value of the ForeignAmount. Note that ForeignAmount does
     * <b>not</b> override Amount.getValue(), and that is always the value in
     * home currency terms. Most of the time, you'll want {@link #toString()}
     * as opposed to this method.
     * 
     * @return a String representing the number with two (and only two)
     *         decimal places.
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
     * Mirroring the addition to Amount of setValue(Amount), this is a hack to
     * make certain UI use cases expedient.
     * 
     * @param a
     *            a regular Amount whose literal numerical value you want to
     *            copy and use as the foreign value of this ForeignAmount.
     *            Note this is <b>not</b> setting a 1:1 exchange rate; this is
     *            just copying whatever value happens to be in the Amount
     *            object passed in. Of course, if you pass in a ForeignAmount,
     *            then it's actual internal number value (which is denominated
     *            in home currency) will be used so you will set this
     *            ForeignAmount to the value of the ForeignAmount argument.
     */
    public void setForeignValue(Amount a) {
        this.foreignNumber = a.getNumber();

        if (this.rate == null) {
            return;
        }
        BigDecimal r = new BigDecimal(this.rate);
        recalculateHomeGivenRate(r);
    }

    /**
     * Get the Currency in which this ForeignAmount is denominated.
     */
    public Currency getCurrency() {
        return currency;
    }

    /**
     * If the currency is changed, then obviously the exchange rate needs to
     * change. <b>This method does not recalculate anything, so you need to
     * call setRate() or setHomeValue()...</b>
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
        BigDecimal h = super.getBigDecimal();
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
     * Given a [new] home currency value String (and assuming a foreign
     * value), calculate a new exchange rate.
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
     * Internally, everything is stored in terms of the home currency, with
     * the foreign amount converted at the specified exchange rate. Most uses,
     * however, will want to display the original foreign value that was
     * entered. We could have overridden getValue(), but I think it better
     * that that always returns the value in home terms. So we override
     * toString() instead.
     * 
     * @return a formatted String representing the foreign amount. This string
     *         is <b>not</b> embellished with currency symbol or currency
     *         code, as that is best achieved by the GUI code. See TODO.
     */
    public String toString() {
        return padComma(numberToString(foreignNumber));
    }

    public Object clone() {
        final ForeignAmount obj;

        obj = new ForeignAmount();
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
