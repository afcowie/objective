/*
 * Identifier.java
 * 
 * See LICENCE file for usage and redistribution terms
 * Copyright (c) 2006 Operational Dynamics
 */
package accounts.domain;

/**
 * A class representing "constants" in the database. The archtypical cases for
 * this are taxation codes - the identifier used in the database needs to
 * correspond to a handle as thought of by users. This class makes that simple
 * translation.
 * <p>
 * It's assumed that you'll subclass this to your particular domain.
 * 
 * @author Andrew Cowie
 */
public class Identifier implements Comparable
{
	/**
	 * Human readable (proper case, spaces) version of the Identifier constant.
	 * Default null value gives an unactivated defence.
	 */
	private String	name	= null;

	/**
	 * Constructs a new constant
	 * 
	 * @param name
	 *            String The human readable string that goes with this
	 *            identifier.
	 */
	public Identifier(String name) {
		setName(name);
	}

	public final String getName() {
		return name;
	}

	/**
	 * Change the plain text name associated with this identifier.
	 */
	public void setName(String name) {
		if ((name == null) || name.equals("")) {
			throw new IllegalArgumentException("Can't use null or blank as the identifier name");
		}
		this.name = name;
	}

	public String toString() {
		return name;
	}

	/**
	 * Override this in any subclasses.
	 */
	public String getClassString() {
		return "Identifier";
	}

	/**
	 * Implement Comparable. Note that if a subclass of Identifier adds any new
	 * fields, then it needs to override this compareTo() [presumably calling
	 * super.compareTo() as appropriate]
	 */
	public int compareTo(Object x) {
		if (x == null) {
			throw new NullPointerException("Can't compareTo() against null");
		}
		if (!(x instanceof Identifier)) {
			throw new IllegalArgumentException("Can only compare Identifier objects");
		}
		Identifier i = (Identifier) x;

		return this.name.compareTo(i.name);
	}
}