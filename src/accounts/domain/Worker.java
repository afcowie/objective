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
	private String	name;

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
		this.name = name;
	}
}
