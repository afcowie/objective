/*
 * UserInterface.java
 * 
 * See LICENCE file for usage and redistribution terms
 * Copyright (c) 2006 Operational Dynamics
 */
package generic.ui;

import generic.client.Hooks;
import generic.client.Master;
import generic.persistence.DataClient;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Central touch point to cause user interface wide actions to occur. An
 * application with a GTK based graphical user interface should extend this
 * class and implement approrpriate methods to allow windows to be launched and
 * clearn shutdowns to occur. Then, when bringing up your application, assign an
 * instance of this subclass to {@link generic.client.Master#ui Master.ui}
 * 
 * @see generic.client.Master
 * @author Andrew Cowie
 */
public abstract class UserInterface
{
	private static Set	windows	= new LinkedHashSet();

	/**
	 * Register a window as ready for display to the user. While one of our
	 * Window subclasses can, of course, call present on itself perfectly well,
	 * we delegate here so that global state such as the list of open windows is
	 * maintained.
	 * 
	 * @param w
	 */
	protected void regsiterWindow(PrimaryWindow w) {
		windows.add(w);
	}

	protected void deregisterWindow(PrimaryWindow w) {
		windows.remove(w);
	}

	/**
	 * Register a Hooks callback which cleanly dismisses any open PrimaryWindows
	 * on shutdown.
	 */
	protected UserInterface() {
		Master.registerCallback(new Hooks() {
			public void shutdown() {
				try {
					/*
					 * EditorWindows, in normal circumstances, politely
					 * deregister themselves when being deleted. This causes the
					 * windows Set to change, which would result in
					 * ConcurrentModificationException while we're working
					 * through via the iterators, so we just quickly copy the
					 * Set into a List and work through that instead.
					 */
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
				}
			}
		});
	}

	/**
	 * Launch a new window. The primary reason for this singleton class to exist
	 * is to provide a central point which disparate event handlers can poke in
	 * order to cause UI windows to be launched. In extending this to a concrete
	 * class which can be assigned to {@link generic.client.Master#ui Master.ui},
	 * the application will implement a launch() method to take an action
	 * appropriate to the target parameter passed. For example, if the target
	 * object is a Currency object, then launching a UI to edit the list of
	 * currencies would be appropriate.
	 * 
	 * @param db
	 *            the DataClient which target resides in, used to look up the
	 *            target objects database id.
	 * @param target
	 *            the object wish determines what UI element is to be launched.
	 *            target <i>must</i> be an object activated out of the db
	 *            database connection.
	 */
	public abstract void launch(DataClient db, Object target);
}
