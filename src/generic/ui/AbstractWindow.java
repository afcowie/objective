/*
 * AbstractWindow.java
 * 
 * See LICENCE file for usage and redistribution terms
 * Copyright (c) 2005-2006 Operational Dynamics
 */
package generic.ui;

import generic.util.Debug;

import java.io.FileNotFoundException;

import org.gnu.glade.LibGlade;
import org.gnu.gtk.Box;
import org.gnu.gtk.VBox;
import org.gnu.gtk.Window;
import org.gnu.gtk.event.LifeCycleEvent;
import org.gnu.gtk.event.LifeCycleListener;

/**
 * The base class in our Window hierarchy, providing default behaviour common to
 * GUI Windows. Two forms: a basic one for constructing GUI elements up from
 * Widgets programmatically, and one that allows you to load an GUI description
 * file as created by Glade. However constucted, this class then maintains a
 * useful internal reference to:
 * <ul>
 * <li>the GTK Window itself
 * <li>the top GUI element (by design a Box called "top")
 * <li>the LibGlade parser, if applicable.
 * </ul>
 * <p>
 * AbstractWindow nstalls a useful default LifeCycleHandler, which you can
 * leverage by overriding it or by overriding the implementations of hideHook()
 * and deleteHook()
 * 
 * @author Andrew Cowie
 */
public abstract class AbstractWindow
{
	/**
	 * The top level, user visible GTK Window we are wrapping. This is
	 * protected, so subclasses can use this field to access any Window
	 * properties or methods as they need.
	 */
	protected Window			window;

	/**
	 * The default LifeCycleListener that is implemented by this class. On close
	 * it calls deleteHook() and on hide it calls hideHook(), both overrideable.
	 * Likewise, this field is protected so you can just override it in a
	 * subclass if you need to.
	 */
	protected LifeCycleListener	defaultListener;

	/**
	 * These are all protected, so as to be visible to subclasses so there's no
	 * need for a getter. A subclass should be smart enough to know whether or
	 * not it used the Glade form before trying to use gladeParser.getWidget()!
	 */
	protected LibGlade			gladeParser;

	/**
	 * As a convention, our standard windows all have a top level Box (usually a
	 * VBox but no matter). By calling packStart() widgets can be easily added
	 * to it. The programatic constrcutor sets a VBox, the glade one sets up
	 * what ever is in the file. Note that the glade file needs to call that
	 * uppermost {V|H}Box "top".
	 */
	protected Box				top;

	/**
	 * A debug readable string identifying this window/whatever which can be
	 * given as the name to UnitOfWork, etc. Formed out of the ClassString name
	 * and a sequence number.
	 */
	protected String			me;
	private static int			seq		= 1;
	private static Object		lock	= new Object();

	/**
	 * Setup common to both types of window:
	 * <ul>
	 * <li>set me variable
	 * <li>establish the default listener
	 * </ul>
	 */
	private void setup() {
		/*
		 * only filled in by glade form
		 */
		gladeParser = null;

		/*
		 * Form a name usable for debugging and UnitOfWork handles, unique by
		 * sequence number which we increment. GTK is single threaded so this
		 * shouldn't be a problem but nevertheless guard against double tap bugs -
		 * the whole point is to have useful debug information after all.
		 */
		synchronized (lock) {
			me = getClassString() + "-" + seq;
			seq++;
		}

		/*
		 * Setup standard listener to handle hide and delete events
		 */
		defaultListener = new LifeCycleListener() {
			public void lifeCycleEvent(LifeCycleEvent event) {
				Debug.print("listeners", me + " lifeCyleEvent " + event.getType().getName());
				if (event.getType() == LifeCycleEvent.Type.HIDE) {
					/* (overridable) */
					hideHook();
				}
			}

			public boolean lifeCycleQuery(LifeCycleEvent event) {
				Debug.print("listeners", me + " lifeCyleQuery " + event.getType().getName());
				if (event.getType() == LifeCycleEvent.Type.DELETE) {
					/* (overridable) */
					return deleteHook();
				} else {
					return false;
				}
			}
		};
	}

	/**
	 * Construct a GTK Window with the appropriate (common) properties that all
	 * of our windows have. This Window will then be populated programatically
	 * by the subclass constructor. When that constructor is finished it should
	 * call present() unless it has a reason not to.
	 * 
	 * @param title
	 *            the String to be used as the user viewable Window title.
	 */
	public AbstractWindow(String title) {
		/*
		 * Setup defaultListener, etc
		 */
		setup();

		/*
		 * Construct Window and set default behaviour.
		 */
		window = new Window();

		window.setTitle(title);
		window.hide();

		window.addListener(defaultListener);

		/*
		 * Finally, create a new Box to hold things, in this case a VBox.
		 */
		top = new VBox(false, 3);
		top.setName("top");
		window.add(top);
	}

	/**
	 * Construct a GTK Window pulling UI description from a Glade file but also
	 * with the appropriate (common) properties that all our Windows have.
	 * Unlike the programatic constructor, this Window has its title set by
	 * LibGlade as specified in the Glade .xml UI file. This constructor calls
	 * hide() to present morphing windows flickering across the screen. When the
	 * subclass constructor is finished it should call present() unless it has a
	 * reason not to.
	 * 
	 * @param whichElement
	 *            The name of the top Window element (as specified in the .glade
	 *            file) to be pulled and instantiated as a GTK Window
	 * @param gladeFilename
	 *            The filename of the glade .xml file from which to pull the
	 *            whichElement Window from.
	 */
	public AbstractWindow(String whichElement, String gladeFilename) {
		/*
		 * Setup defaultListener, etc
		 */
		setup();

		/*
		 * Run LibGlade to parse specified .glade file
		 */

		try {
			gladeParser = new LibGlade(gladeFilename, this);
		} catch (FileNotFoundException e) {
			// If it can't find that glade file, we have an app
			// configuration problem or worse some UI bug, and need to abort.
			Master.abort("Can't find glade file " + gladeFilename + " for " + whichElement);
		} catch (Exception e) {
			e.printStackTrace();
			Master.abort("An internal error occured trying to read and process the glade file " + gladeFilename
				+ " for " + whichElement);
		}
		window = (Window) gladeParser.getWidget(whichElement);

		/*
		 * And finish standard setup
		 */

		window.hide();
		window.addListener(defaultListener);

		top = (Box) gladeParser.getWidget("top");
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
	 *         should be further propagated back up to GTK.
	 */
	protected boolean deleteHook() {
		window.hide();
		window.destroy();
		return false;
	}

	/**
	 * Ensure all widgets in this Window are showing and present (unhide) to
	 * screen.
	 */
	public void present() {
		window.showAll();
		window.present();
	}

	/**
	 * @return a String with the package name trimmed off, any enclosing class
	 *         name trimmed off (ie A$Inner will return Inner), and any trailing
	 *         "Window" removed.
	 */
	public String getClassString() {

		String className = this.getClass().getName();
		int posDot = className.lastIndexOf('.');
		if (posDot == -1) {
			return "(unknown)";
		}
		int posDollar = className.lastIndexOf('$');
		String usefulName;
		if (posDollar == -1) {
			usefulName = className.substring(posDot + 1);
		} else {
			usefulName = className.substring(posDollar + 1);
		}
		int posWindow = usefulName.lastIndexOf("Window");
		if (posWindow == -1) {
			return usefulName;
		} else {
			return usefulName.substring(0, posWindow);
		}
	}
}
