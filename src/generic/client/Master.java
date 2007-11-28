/*
 * Master.java
 * 
 * See LICENCE file for usage and redistribution terms
 * Copyright (c) 2006 Operational Dynamics
 */
package generic.client;

import generic.ui.UserInterface;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import org.gnome.gtk.Gtk;

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
	private static Set			callbacks	= new LinkedHashSet();

	private static boolean		goingDown	= false;

	/**
	 * Global re-entry point to reach the application's UI. A common use case is
	 * 
	 * <pre>
	 *     Transaction t = ...
	 *     
	 *     Master.ui.launchEditor(t);
	 * </pre>
	 */
	public static UserInterface	ui;

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
	 * @param message
	 *            Message to display on the console when aborting.
	 */
	public static void abort(String message) {
		if (goingDown) {
			return;
		} else {
			goingDown = true;
		}
		System.out.flush();

		System.err.println(message);
		try {
			Iterator iter = callbacks.iterator();
			while (iter.hasNext()) {
				Hooks target = (Hooks) iter.next();
				target.abort();
			}
		} finally {
			System.err.println("Terminating.");
			System.err.flush();
		}

		System.exit(1);
	}

	/**
	 * Go down gracefully, fist closing any open windows, and then calling each
	 * registered Hooks {@link Hooks#shutdown() shutdown()} method. Concludes
	 * with {@link Gtk#mainQuit() Gtk.mainQuit()}, returning execution control
	 * to the main() method in the class the program was invoked with.
	 */
	public static void shutdown() {
		/*
		 * Avoid double-taps
		 */
		if (goingDown) {
			return;
		} else {
			goingDown = true;
		}
		System.out.flush();

		/*
		 * Run through any registered shutdown callbacks:
		 */

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
