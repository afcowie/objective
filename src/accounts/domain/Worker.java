/*
 * Worker.java
 * 
 * See LICENCE file for usage and redistribution terms
 * Copyright (c) 2005-2006 Operational Dynamics
 */
package accounts.domain;

/**
 * Base class of employees and subcontractors. A Worker is someone we pay money
 * to - periodically or otherwise.
 */
public class Worker
{
	private String					name;

	private CreditPositiveLedger	expensesPayable;

	/**
	 * Get the [full] name of the person.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Set the name of the Worker. This is full name; no need to bother with
	 * first name/last name distinctions.
	 * 
	 * @param name
	 *            a String with the full name of the person
	 */
	public void setName(String name) {
		if ((name == null) || (name.equals(""))) {
			throw new IllegalArgumentException();
		}
		this.name = name;
	}

	/**
	 * Get the Ledger within the ReimbursableExpensesPayableAccount that refers
	 * to the amount owing this Worker.
	 */
	public Ledger getExpensesPayable() {
		return expensesPayable;
	}

	/**
	 * Set the ReimbursableExpensesPayableAccount that we want to have this
	 * Worker object point at. Obviously this is only to be called by
	 * AddWorkerCommand.
	 */
	public void setExpensesPayable(CreditPositiveLedger expensesPayable) {
		this.expensesPayable = expensesPayable;
	}
}
