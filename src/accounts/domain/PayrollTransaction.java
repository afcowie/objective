/*
 * PayrollTransaction.java
 * 
 * See LICENCE file for usage and redistribution terms
 * Copyright (c) 2006 Operational Dynamics
 */
package accounts.domain;

/**
 * A Transaction representing pay to an Employee.
 * 
 * <p>
 * There are no specialty methods to set the individual Entries (salary,
 * withholding, paycheck) that go with the transaction; its assumed the
 * EditorWindow which construts this Transaction can figure those out.
 * 
 * @author Andrew Cowie
 */
public class PayrollTransaction extends Transaction
{
	private Employee				employee;
	private PayrollTaxIdentifier	taxIdentifier;

	public PayrollTransaction() {
		super();
	}

	/**
	 * General use constructor.
	 * 
	 * @param employee
	 *            the Employee you're paying.
	 * @param identifier
	 *            the PayrollTaxIdentifier relevent to this paycheck.
	 */
	public PayrollTransaction(Employee employee, PayrollTaxIdentifier identifier) {
		super();
		setEmployee(employee);
		setTaxIdentifier(identifier);
	}

	/**
	 * Somewhat artificial constructor useful for mockups and unit tests
	 * 
	 * @param employee
	 *            The Employee object for the person being paid
	 * @param date
	 *            The date the person was paid
	 * @param entries
	 *            an array of Entry objects.
	 */
	public PayrollTransaction(Employee employee, Datestamp date, Entry[] entries) {
		super("Payroll to " + employee.getName(), date, entries);
	}

	public String getClassString() {
		return "Payroll Transaction";
	}

	/**
	 * @return the Employee who was (will be) paid
	 */
	public Employee getEmployee() {
		return employee;
	}

	/**
	 * @param employee
	 *            set the Employee who will receive this paycheck
	 */
	public void setEmployee(Employee employee) {
		if (employee == null) {
			throw new IllegalArgumentException("Can't set null as Employee");
		}
		this.employee = employee;
		setDescription("Paycheck to " + employee.getName());
	}

	/**
	 * @return the PayrollTaxIdentifier that was used to determine the
	 *         withholdings on this particular payroll. (ie, you can change the
	 *         Employee's chosen TaxIdentifiers in the future, but once made and
	 *         recorded, this Transaction will keep track of how it was set at
	 *         the time.
	 */
	public PayrollTaxIdentifier getTaxIdentifier() {
		return taxIdentifier;
	}

	/**
	 * Set the PayrollTaxIdentifier used to determine the withholdings for this
	 * paycheck.
	 * 
	 * @param identifier
	 *            Can be null if the country specific implementation doesn't use
	 *            a PayrollTaxIdentifier.
	 */
	public void setTaxIdentifier(PayrollTaxIdentifier identifier) {
		this.taxIdentifier = identifier;
	}
}
