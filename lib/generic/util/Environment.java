/*
 * Environment.java
 * 
 * See LICENCE file for usage and redistribution terms
 * Copyright (c) 2005 Operational Dynamics
 */
package generic.util;

/**
 * Pull values from environment. This is a seperate class largely so the
 * deprecation warning will be isolated from mainline code. The getenv call
 * could be replaced with some JNI that does the same thing.
 * 
 * @author Andrew Cowie
 */
public class Environment
{
	/**
	 * Get an environment variable from the inherited (Linux) environment. Thiw
	 * would wrap the [deprecated] System.getenv() call, but that throws Error
	 * in modern JDKs. So, instead, grab property columns. Grr. Really need to
	 * reimplement something with JNI.
	 * 
	 * @param variableName
	 *            the name of the variable whose value you want.
	 * @return the value of the environment variable, normalized to null if
	 *         empty or not found.
	 */
	public static String getenv(final String variableName) {
		if ((variableName == null) || (variableName.equals(""))) {
			throw new IllegalArgumentException("Can't get an empty or null environment variable");
		}

//		String candidate = System.getenv(variableName);
		String candidate = System.getProperty(variableName);

		if ((candidate == null) || (candidate.equals(""))) {
			return null;
		} else {
			return candidate;
		}
	}
}
