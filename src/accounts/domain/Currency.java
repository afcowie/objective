/*
 * Currency.java
 * 
 * See LICENCE file for usage and redistribution terms
 * Copyright (c) 2005-2006 Operational Dynamics
 */
package accounts.domain;

/**
 * A monetary currency. Note that we don't bother whether to record whether it
 * should be 50 Sfr or Sfr 50, as the UI will be printing them however seems
 * appropriate. By convention, we will always be printing both, ie
 * <ul>
 * <li>$50 AUD
 * <li>$50 CAD
 * <li>$50 USD
 * <li>£50 GBP
 * <li>S$50 SGD
 * <li>Rs50 INR
 * </ul>
 * Which means that you can use the same symbol (ie, "$") for more than one
 * currency.
 * 
 * @author Andrew Cowie
 */
public class Currency
{
	private String	code;
	private String	name;
	private String	symbol;

	public Currency() {
		// for searching
	}

	/**
	 * 
	 * @param code
	 *            The abbreviation or code for the currency, eg "CAD". By
	 *            international convention this must be 3 characters, all
	 *            capitals
	 * @param name
	 *            The full name of the currency, usually incorporating the name
	 *            of the country, eg "Canadian Dollar"
	 * @param symbol
	 *            The character, symbol, or letters used as a prefix or suffix
	 *            of the currency.
	 */
	public Currency(String code, String name, String symbol) {
		setCode(code);
		setName(name);
		setSymbol(symbol);
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		if (code.length() != 3) {
			throw new IllegalArgumentException(
				"Currency codes are, by international convention, three characters");
		}
		if (!code.equals(code.toUpperCase())) {
			throw new IllegalArgumentException(
				"Currency codes are, by international convention, upper case");
		}
		this.code = code;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		if (name == null) {
			throw new IllegalArgumentException("The currency name can't be null");
		}
		if (name.length() == 0) {
			throw new IllegalArgumentException("The currency name can't be empty");
		}
		this.name = name;
	}

	public String getSymbol() {
		return symbol;
	}

	public void setSymbol(String symbol) {

		this.symbol = symbol;
	}

}
