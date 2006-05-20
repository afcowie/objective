/*
 * ObjectiveAccounts.java
 * 
 * See LICENCE file for usage and redistribution terms
 * Copyright (c) 2005-2006 Operational Dynamics
 */
package accounts.client;

/**
 * The main reentry point for the whole framework. Holds a number of "global"
 * variables for use throughout the program. See the JavaDoc for the public
 * static members of this class. In particlar, note
 * <ul>
 * <li><code>ui</code>, a way to get to the top level UserInterface object
 * so you can cause program wide GUI effects.
 * </ul>
 * 
 * @author Andrew Cowie
 */
public class ObjectiveAccounts
{
	/**
	 * A String constant expressing the current version of the overall released
	 * codebase. Used by equivalence (the top level ./configure program) to find
	 * out the version to use in the names of release and snapshot tarballs.
	 * Small helper programs making use of this class (ie to access
	 * {@link ObjectiveAccounts.store}) do not need to override this constant.
	 */
	public static final String		VERSION		= "0.1.16";

	/**
	 * The copyright statement for this code and a pointer to the top level
	 * LICENCE file. Iterate over this array and print with newlines or Labels
	 * if you will, but this is here to embed it in distributed bytecode.
	 */
	public static final String[]	COPYRIGHT	= new String[] {
		"Copyright © 2005-2006 Operational Dynamics Consulting Pty Ltd, and others.",
		"The LICENCE file included with these sources lists the terms under which",
		"you may use and redistribute this code.",
												};

	// public static ObjectiveAccountsUserInterface ui = null;

	/**
	 * Go down hard. TODO Do we want to System.exit() here or elsewhere?
	 * 
	 * @param string
	 *            Message to display on abort
	 */
	public static void abort(String message) {
		System.err.println(message);
		// if (ui != null) {
		// ui.shutdown();
		// }
	}

	/**
	 * Get a statement about the copyright of the program code. [If you want to
	 * do something other than print this to a terminal, then get the individual
	 * lines by accessing the COPYRIGHT String array directly; it's public.]
	 * 
	 * @return the embedded copyright statement joined together into a single
	 *         String with its lines separated by newlines.
	 */
	public static String getCopyrightText() {
		StringBuffer buf = new StringBuffer();

		for (int i = 0; i < COPYRIGHT.length; i++) {
			buf.append(COPYRIGHT[i]);
			buf.append("\n");
		}

		return buf.toString();
	}
}
