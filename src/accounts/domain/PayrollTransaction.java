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
package accounts.domain;

import objective.domain.Datestamp;
import objective.domain.Employee;
import objective.domain.Entry;
import objective.domain.Transaction;

/**
 * A Transaction representing pay to an Employee.
 * 
 * @author Andrew Cowie
 */
public class PayrollTransaction extends Transaction
{
    private Employee employee;

    private PayrollTaxIdentifier taxIdentifier;

    private Datestamp fromDate;

    private Datestamp endDate;

    private Entry salary;

    private Entry withholding;

    private Entry paycheck;

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
     * Artificial constructor for mockups and unit tests only. You
     * <i>really</i> want to make sure that the entries passed to this are
     * sensible, otherwise editing this Transaction will lead to all sorts of
     * unwanted artifacts.
     * 
     * @param employee
     *            The Employee object for the person being paid
     * @param date
     *            The date the person was paid
     * @param entries
     *            an array of Entry objects. <b>Must be in order salary,
     *            withholding, paycheck</b>
     */
    public PayrollTransaction(Employee employee, PayrollTaxIdentifier identifier, Datestamp date,
            Datestamp fromDate, Datestamp endDate, Entry[] entries) {
        /*
         * The super call runs addEntries() over the array; the setters below
         * set PayrollTransaction's fields.
         */
        super("Paycheck to " + employee.getName(), date, entries);
        setEmployee(employee);
        setTaxIdentifier(identifier);
        setDate(date);

        setFromDate(fromDate);
        setEndDate(endDate);

        setSalaryEntry(entries[0]);
        setWithholdingEntry(entries[1]);
        setPaycheckEntry(entries[2]);
    }

    public String getClassString() {
        return "Payroll";
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
     *         withholdings on this particular payroll. (ie, you can change
     *         the Employee's chosen TaxIdentifiers in the future, but once
     *         made and recorded, this Transaction will keep track of how it
     *         was set at the time.
     */
    public PayrollTaxIdentifier getTaxIdentifier() {
        return taxIdentifier;
    }

    /**
     * Set the PayrollTaxIdentifier used to determine the withholdings for
     * this paycheck.
     * 
     * @param identifier
     *            Can be null if the country specific implementation doesn't
     *            use a PayrollTaxIdentifier.
     */
    public void setTaxIdentifier(PayrollTaxIdentifier identifier) {
        this.taxIdentifier = identifier;
    }

    public Datestamp getFromDate() {
        return fromDate;
    }

    public void setFromDate(Datestamp fromDate) {
        this.fromDate = fromDate;
    }

    public Datestamp getEndDate() {
        return endDate;
    }

    /**
     * Set the end of the period this pay covers. Certainly, Transaction has a
     * Datestamp, but there are cases where you might reasonably have a
     * paycheck generated and paid out on a date that is after the end of a
     * pay period.
     */
    public void setEndDate(Datestamp endDate) {
        this.endDate = endDate;
    }

    public Entry getPaycheckEntry() {
        return paycheck;
    }

    /**
     * Designate an Entry as representing the paycheck. This is just a
     * reference; you must still addEntry() when forming a Transaction.
     */
    public void setPaycheckEntry(Entry paycheck) {
        this.paycheck = paycheck;
    }

    public Entry getSalaryEntry() {
        return salary;
    }

    /**
     * Designate an Entry as representing the salary. This is just a
     * reference; you must still addEntry() when forming a Transaction.
     */
    public void setSalaryEntry(Entry salary) {
        this.salary = salary;
    }

    public Entry getWithholdingEntry() {
        return withholding;
    }

    /**
     * Designate an Entry as representing the withholding. This is just a
     * reference; you must still addEntry() when forming a Transaction.
     */
    public void setWithholdingEntry(Entry withholding) {
        this.withholding = withholding;
    }
}
