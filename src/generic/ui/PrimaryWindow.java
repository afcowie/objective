/*
 * PrimaryWindow.java
 * 
 * See LICENCE file for usage and redistribution terms
 * Copyright (c) 2006 Operational Dynamics
 */
package generic.ui;

import generic.client.Master;

/**
 * Denote a window as being one of the primary ones which a user interacts with.
 * These windows will be registered with generic.ui.Master and thus be available
 * to be shown in drop down Window menu lists, etc. More importantly, they will
 * all be hidden immediately and asked to delete on a call to Master.shutdown().
 * 
 * @author Andrew Cowie
 */
public abstract class PrimaryWindow extends AbstractWindow
{
	/**
	 * Basic form of a PrimaryWindow. Calls AbstractWindow's no-arg constructor,
	 * then registers this window with Master.
	 */
	public PrimaryWindow() {
		super();
		Master.ui.regsiterWindow(this);
	}

	/**
	 * Glade form of a PrimaryWindow. Passes the parameters to AbstractWindow's
	 * glade constructor, then registers this window with Master.
	 */
	public PrimaryWindow(String whichElement, String gladeFilename) {
		super(whichElement, gladeFilename);
		Master.ui.regsiterWindow(this);
	}

	protected boolean deleteHook() {
		super.deleteHook();
		Master.ui.deregisterWindow(this);
		return false;
	}

	/**
	 * Quietly expose hide() so that Master can silence them while shutting
	 * down. You could just as easily access window's hide()
	 */
	protected void hide() {
		window.hide();
	}
}
