/*
 * Master.java
 * 
 * See LICENCE file for usage and redistribution terms
 * Copyright (c) 2006 Operational Dynamics
 */
package generic.ui;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import org.gnu.gtk.Gtk;

/**
 * A static class containing global callbacks that can be used from throughout
 * the application to cause generic user interface wide actions to occur.
 * Applications can register callbacks using subclass instances of the Hooks
 * class.
 * 
 * @author Andrew Cowie
 */
public class Master
{
	protected static Set	callbacks	= new LinkedHashSet();

	/**
	 * Register a Hooks class instance (anonymous or otherwise) to be called in
	 * the event of one of Master's static methods being activated.
	 */
	public static void registerCallback(Hooks hook) {
		callbacks.add(hook);
	}

	/**
	 * Go down hard. Concludes with System.exit(1)!
	 * 
	 * @param string
	 *            Message to display on abort
	 */
	public static void abort(String message) {
		System.out.flush();

		System.err.println(message);
		try {
			Iterator iter = callbacks.iterator();
			while (iter.hasNext()) {
				Hooks target = (Hooks) iter.next();
				target.abort();
			}
		} finally {
			System.err.println("Exiting.");
			System.err.flush();
		}

		System.exit(1);
	}

	/**
	 * Go down gracefully, calling each registered Hooks shutdown() method.
	 * Concludes with Gtk.mainQuit(), returning execution control to the main()
	 * method in the class the program was invoked with.
	 */
	public static void shutdown() {
		System.out.flush();

		try {
			Iterator iter = callbacks.iterator();
			while (iter.hasNext()) {
				Hooks target = (Hooks) iter.next();
				target.shutdown();
			}
		} finally {
		}

		Gtk.mainQuit();
	}
}