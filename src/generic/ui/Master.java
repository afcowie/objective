/*
 * Master.java
 * 
 * See LICENCE file for usage and redistribution terms
 * Copyright (c) 2006 Operational Dynamics
 */
package generic.ui;

import java.util.ArrayList;
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
	private static Set	callbacks	= new LinkedHashSet();
	private static Set	windows		= new LinkedHashSet();

	/**
	 * Register a window as ready for display to the user. While one of our
	 * Window subclasses can, of course, call present on itself perfectly well,
	 * we delegate here so that global state such as the list of open windows is
	 * maintained.
	 * 
	 * @param w
	 */
	static void regsiterWindow(PrimaryWindow w) {
		windows.add(w);
	}

	static void deregisterWindow(PrimaryWindow w) {
		windows.remove(w);
	}

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
			System.err.println("Terminating.");
			System.err.flush();
		}

		System.exit(1);
	}

	private static boolean	shuttingDown	= false;

	/**
	 * Go down gracefully, fist closing any open windows, and then calling each
	 * registered Hooks shutdown() method. Concludes with Gtk.mainQuit(),
	 * returning execution control to the main() method in the class the program
	 * was invoked with.
	 */
	public static void shutdown() {
		/*
		 * Avoid double-taps
		 */
		if (shuttingDown) {
			return;
		} else {
			shuttingDown = true;
		}
		System.out.flush();

		/*
		 * Cleanly dismiss any open PrimaryWindows:
		 */

		try {
			// necessary to avoid ConcurrentModificationException
			ArrayList deadmeat = new ArrayList(windows);
			Iterator iter;

			iter = deadmeat.iterator();
			while (iter.hasNext()) {
				PrimaryWindow w = (PrimaryWindow) iter.next();
				w.hide();
			}

			iter = deadmeat.iterator();
			while (iter.hasNext()) {
				PrimaryWindow w = (PrimaryWindow) iter.next();
				w.deleteHook();
			}
		} catch (Exception e) {
		} finally {
		}

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
