/*
 * NewAccountDialog.java
 * 
 * See LICENCE file for usage and redistribution terms
 * Copyright (c) 2005 Operational Dynamics
 */
package accounts.ui;

import generic.util.Debug;

import java.io.FileNotFoundException;

import org.gnu.glade.LibGlade;
import org.gnu.gtk.Window;
import org.gnu.gtk.event.LifeCycleEvent;
import org.gnu.gtk.event.LifeCycleListener;

import accounts.client.ObjectiveAccounts;

/**
 * Simple wrapper around the common setup when using LibGlade.
 * 
 * @author Andrew Cowie
 */
public class GladeWindow
{
	protected LibGlade			_glade				= null;
	protected Window			_window				= null;
	private LifeCycleListener	_defaultListener	= null;

	/**
	 * 
	 * @param which
	 *            The name of the top Window element (as specified in the .glade
	 *            file)
	 * @param filename
	 * @param listener
	 *            If you wish to supply your own LifeCycleListener, or null to
	 *            use the default (which calls the close() method, which is
	 *            easily overriden).
	 */
	public GladeWindow(String which, String filename) {
		// final String ownerName = this.getClass().getName();
		final String ownerName = which;
		try {
			_glade = new LibGlade(filename, this);
		} catch (FileNotFoundException e) {
			// If it can't find that glade file, we have an app
			// configuration problem or worse some UI bug, and need to abort.
			ObjectiveAccounts.abort("Can't find glade file " + filename + " for " + ownerName);
		} catch (Exception e) {
			e.printStackTrace();
			ObjectiveAccounts.abort("An internal error occured trying to read and process the glade file " + filename
					+ " for " + ownerName);
		}
		_window = (Window) _glade.getWidget(which);
		_window.hide();

		_defaultListener = new LifeCycleListener() {
			public void lifeCycleEvent(LifeCycleEvent event) {
				Debug.print("listeners", ownerName + " lifeCyleEvent " + event.getType().getName());
				if (event.getType() == LifeCycleEvent.Type.HIDE) {
					/* (overridable) */
					hideHook();
				}
			}

			public boolean lifeCycleQuery(LifeCycleEvent event) {
				Debug.print("listeners", ownerName + " lifeCyleQuery " + event.getType().getName());
				if (event.getType() == LifeCycleEvent.Type.DELETE) {
					/* call delete hook (overridable) */
					boolean handled = deleteHook();
					return handled;
				} else {
					return false;
				}
			}
		};
		_window.addListener(_defaultListener);
	}

	/**
	 * Give a chance to do override an action on hides. This is pretty much only
	 * for popup windows that are a part of Pickers.
	 */
	protected void hideHook() {
		// override as necessary
	}

	/**
	 * The default action to take on closing the window is to hide() <i>and</i>
	 * destroy() it. Override this if you want to do something different!
	 * 
	 * @return the boolean value that you want the LifeCycleListener to return,
	 *         ie whether or not the event was handled (true) or whether it
	 *         should be propagated back up to GTK.
	 */
	protected boolean deleteHook() {
		_window.hide();
		_window.destroy();
		return false;
	}

	protected void replaceDefaultListener(LifeCycleListener listener) {
		if (_defaultListener == null) {
			return;
		}
		_window.removeListener(_defaultListener);
		_window.addListener(listener);
		_defaultListener = null;
	}

	// protected Widget getWidget(String name) {
	// return _glade.getWidget(name);
	// }
	//
	// public Window getTopWindow() {
	// return _window;
	// }
	//
	// public LibGlade getLibGladeObject() {
	// return _glade;
	// }

	public void present() {
		_window.showAll();
		_window.present();
	}
}
