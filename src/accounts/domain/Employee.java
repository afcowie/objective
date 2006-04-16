/*
 * Employee.java
 * 
 * See LICENCE file for usage and redistribution terms
 * Copyright (c) 2005-2006 Operational Dynamics
 */
package accounts.domain;

public class Employee extends Worker
{
	/**
	 * Proper constructor; use UI to build up fields.
	 */
	public Employee() {
	}

	/**
	 * Mockup constructor for use in unit tests and demos.
	 * 
	 * @see Worker#setName(String)
	 */
	public Employee(String name) {
		setName(name);
	}
}
