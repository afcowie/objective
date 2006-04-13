/*
 * Hooks.java
 * 
 * See LICENCE file for usage and redistribution terms
 * Copyright (c) 2006 Operational Dynamics
 */
package generic.ui;

/**
 * A set of callbacks which can be registered with the {@link Master} global UI
 * entry point for action on specified Life Cycle conditions. Use as if it were
 * an interface but it is not complusary to override any of the particular
 * methods here; all the methods here are empty.
 * 
 * @author Andrew Cowie
 */
public class Hooks
{
	protected Hooks() {
	}

	/**
	 * Essential emergency cleanup to attempt before the application thunders
	 * in. At the end of the abort callback sequence, System.exit() will be
	 * called.
	 * 
	 * @see Master#abort(String)
	 */
	public void abort() {
		// no-op
	}

	/**
	 * Actions to take on a gracefully shutdown of the application. GTK is still
	 * active at this time; it is appropriate to close any open Windows that you
	 * have reference to and to stop Timers, etc.
	 * 
	 * @see Master#shutdown()
	 */
	public void shutdown() {
		// no-op
	}

}
