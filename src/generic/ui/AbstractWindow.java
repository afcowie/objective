/*
 * ObjectiveAccounts, accounting for small professional services firms.
 *
 * Copyright © 2005-2011 Operational Dynamics Consulting, Pty Ltd
 *
 * The code in this file, and the program it is a part of, is made available
 * to you by its authors as open source software: you can redistribute it
 * and/or modify it under the terms of the GNU General Public License version
 * 2 ("GPL") as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GPL for more details.
 *
 * You should have received a copy of the GPL along with this program. If not,
 * see http://www.gnu.org/licenses/. The authors of this program may be
 * contacted via http://research.operationaldynamics.com/projects/objective/.
 */
package generic.ui;

import generic.client.Master;

import java.io.FileNotFoundException;

import org.gnome.gdk.Event;
import org.gnome.glade.Glade;
import org.gnome.glade.XML;
import org.gnome.gtk.Box;
import org.gnome.gtk.VBox;
import org.gnome.gtk.Widget;
import org.gnome.gtk.Window;

/**
 * The base class in our Window hierarchy, providing default behaviour common
 * to GUI Windows. Two forms: a basic one for constructing GUI elements up
 * from Widgets programmatically, and one that allows you to load an GUI
 * description file as created by Glade. However constucted, this class then
 * maintains a useful internal reference to:
 * <ul>
 * <li>the GTK {@link org.gnu.gtk.Window} itself
 * <li>the top GUI element (by design a {@link org.gnu.gtk.Box} called
 * <code>top</code>)
 * <li>the {@link org.gnu.glade.LibGlade} parser, if applicable.
 * </ul>
 * <p>
 * AbstractWindow installs a useful default {@link LifeCycleListener}, which
 * you can leverage by overriding it completely, or by simply overriding the
 * implementations of hideHook() and deleteHook() in your subclasses.
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
    protected Window window;

    /**
     * The default LifeCycleListener that is implemented by this class. On
     * close it calls deleteHook() and on hide it calls hideHook(), both
     * overrideable. Likewise, this field is protected so you can just
     * override it in a subclass if you need to.
     */
    protected Window.DeleteEvent defaultListener;

    /**
     * These are all protected, so as to be visible to subclasses so there's
     * no need for a getter. A subclass should be smart enough to know whether
     * or not it used the Glade form before trying to use
     * gladeParser.getWidget()!
     */
    protected XML gladeParser;

    /**
     * As a convention, our standard windows all have a top level Box (usually
     * a VBox but no matter). By calling packStart() widgets can be easily
     * added to it. The programatic constrcutor sets a VBox, the glade one
     * sets up what ever is in the file. Note that the glade file needs to
     * call that uppermost {V|H}Box "top".
     */
    protected Box top;

    /**
     * A debug readable string identifying this window/whatever which can be
     * given as the name to UnitOfWork, etc. Formed out of the ClassString
     * name and a sequence number.
     */
    protected String me;

    private static int seq = 1;

    private static Object lock = new Object();

    /**
     * Setup common to both types of window:
     * <ul>
     * <li>set <code>me</code> String (for debug output)
     * <li>establish the default listener
     * </ul>
     */
    private void setup() {
        /*
         * only filled in by glade form
         */
        gladeParser = null;

        /*
         * Form a name usable for debugging, unique by sequence number which
         * we increment. GTK is single threaded so this shouldn't be a problem
         * but nevertheless guard against double tap bugs - the whole point is
         * to have useful debug information after all.
         */
        synchronized (lock) {
            me = getClassString() + "-" + seq;
            seq++;
        }

        /*
         * Setup standard listener to handle hide and delete events
         */
        defaultListener = new Window.DeleteEvent() {
            public boolean onDeleteEvent(Widget source, Event event) {
                return deleteHook();
            }
        };
    }

    /**
     * Construct a GTK Window with the appropriate (common) properties that
     * all of our windows have. This Window will then be populated
     * programatically by the subclass constructor. When that constructor is
     * finished it should call present() unless it has a reason not to.
     */
    public AbstractWindow() {
        /*
         * Setup defaultListener, etc
         */
        setup();

        /*
         * Construct Window and set default behaviour.
         */
        window = new Window();
        window.hide();

        window.connect(defaultListener);

        /*
         * Finally, create a new Box to hold things, in this case a VBox.
         */
        top = new VBox(false, 3);
        window.add(top);
    }

    /**
     * Construct a GTK Window pulling UI description from a Glade file but
     * also with the appropriate (common) properties that all our Windows
     * have. Unlike the programatic constructor, this Window has its title set
     * by LibGlade as specified in the Glade .xml UI file. This constructor
     * calls hide() to present morphing windows flickering across the screen.
     * When the subclass constructor is finished it should call present()
     * unless it has a reason not to.
     * 
     * @param whichElement
     *            The name of the top Window element (as specified in the
     *            .glade file) to be pulled and instantiated as a GTK Window
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
            gladeParser = Glade.parse(gladeFilename, whichElement); // FIXME
        } catch (FileNotFoundException e) {
            // If it can't find that glade file, we have an app
            // configuration problem or worse some UI bug, and need to abort.
            Master.abort("Can't find glade file " + gladeFilename + " for " + whichElement);
        } catch (Exception e) {
            e.printStackTrace();
            Master.abort("An internal error occured trying to read and process the glade file "
                    + gladeFilename + " for " + whichElement);
        }
        window = (Window) gladeParser.getWidget(whichElement);

        /*
         * And finish standard setup
         */

        window.hide();
        window.connect(defaultListener);

        top = (Box) gladeParser.getWidget("top");
    }

    /**
     * @param title
     *            the String to be used as the user viewable Window title.
     */
    public void setTitle(String title) {
        window.setTitle(title);
    }

    /**
     * Give a chance to do override an action on hides. This is pretty much
     * only for popup windows that are a part of Pickers.
     */
    protected void hideHook() {
    // override as necessary
    }

    /**
     * The default action to take on closing the window is to hide()
     * <i>and</i> dereference it. Override this if you want to do something
     * different!
     * 
     * @return the boolean value that you want the DELETE_EVENT to return, ie
     *         whether or not the event was handled (true) or whether it
     *         should be further propagated back up to GTK.
     */
    protected boolean deleteHook() {
        window.hide();
        window = null;
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
     * A somewhat customized rendition of getClassString() useful for debug
     * output.
     * 
     * @return a String with the package name trimmed off, any enclosing class
     *         name trimmed off (ie <code>Something$Inner</code> will return
     *         <code>Inner</code>), and any trailing "<code>Window</code>"
     *         removed.
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
