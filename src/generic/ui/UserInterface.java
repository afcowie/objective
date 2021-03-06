/*
 * ObjectiveAccounts, accounting for small professional services firms.
 *
 * Copyright © 2006-2011 Operational Dynamics Consulting, Pty Ltd
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

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import objective.persistence.DomainObject;
import objective.ui.EditorWindow;

import org.gnome.gdk.Cursor;
import org.gnome.gdk.CursorType;

/**
 * Central touch point to cause user interface wide actions to occur. An
 * application with a GTK based graphical user interface should extend this
 * class and implement approrpriate methods to allow windows to be launched
 * and clearn shutdowns to occur. Then, when bringing up your application,
 * assign an instance of this subclass to {@link generic.client.Master#ui
 * Master.ui}
 * 
 * @see generic.client.Master
 * @author Andrew Cowie
 */
public abstract class UserInterface
{
    private Set windows = new LinkedHashSet();

    private Map idsToEditors;

    private Map editorsToIds;

    private Set updateListeners;

    private Cursor normalPointer;

    private Cursor workingPointer;

    /**
     * Register a Hooks callback which cleanly dismisses any open
     * PrimaryWindows on shutdown.
     */
    protected UserInterface() {
        idsToEditors = new HashMap();
        editorsToIds = new HashMap();

        updateListeners = new LinkedHashSet();

        normalPointer = new Cursor(CursorType.LEFT_PTR);
        workingPointer = new Cursor(CursorType.WATCH);
    }

    /**
     * Launch a new window. The primary reason for this singleton class to
     * exist is to provide a central point which disparate event handlers can
     * poke in order to cause UI windows to be launched. In extending this to
     * a concrete class which can be assigned to
     * {@link generic.client.Master#ui Master.ui}, the application will
     * implement a launch() method to take an action appropriate to the target
     * parameter passed. For example, if the target object is a Currency
     * object, then launching a UI to edit the list of currencies would be
     * appropriate.
     * 
     * @param target
     *            the object which determines what UI element is to be
     *            launched.
     */
    public void launchEditor(DomainObject target) {
        EditorWindow ew;

        long id = target.getID();
        Long ID = new Long(id);

        if (idsToEditors.containsKey(ID)) {
            ew = (EditorWindow) idsToEditors.get(ID);
            ew.present();
            return;
        }

        ew = launchEditor(id, target);

        if (ew == null) {
            return;
        }
        ew.present();

        idsToEditors.put(ID, ew);
        editorsToIds.put(ew, ID);
    }

    /**
     * This is what you implement to carry out application specific behaviour.
     * 
     * @param id
     *            the database ID of the Object you want to edit.
     * @param target
     *            the object congruent to that which you wish to edit,
     *            allowing you to determine the right kind of window to
     *            launch.
     * @return The EditorWindow that you launch (so we can keep track of it
     *         and relaunch if necessary).
     */
    protected abstract EditorWindow launchEditor(long id, Object target);

    /**
     * The complement of what happens to launch an EditorWindow - propegate
     * the result to any Widget which has registered an UpdateListener.
     * 
     * @param id
     *            the database id of the object which has changed.
     */
    protected void propegateUpdate(long id) {
        Iterator iter = updateListeners.iterator();
        while (iter.hasNext()) {
            UpdateListener u = (UpdateListener) iter.next();
            u.redisplayObject(id);
        }
    }

    public void registerListener(UpdateListener listener) {
        updateListeners.add(listener);
    }

    public void deregisterListener(UpdateListener listener) {
        updateListeners.remove(listener);
    }

    /**
     * Show the application as being in a busy state. Call this with
     * <code>true</code> when before you fire off a worker thread from an OK
     * button and then call it <code>false</code> when you are done. <i>This
     * is GTK code so make sure you call it from within the main thread
     * only.</i>
     * <p>
     * Showing as working:
     * <ul>
     * <li>sets the cursor (pointer) to a watch symbol over any showing
     * {@link PrimaryWindow}s.
     * </ul>
     * 
     * @param working
     *            true if the app is busy working away at something.
     */
    public void showAsWorking(boolean working) {
        Iterator wI = windows.iterator();

        while (wI.hasNext()) {
            PrimaryWindow w = (PrimaryWindow) wI.next();
            if (working == true) {
                w.window.getWindow().setCursor(workingPointer);
            } else {
                w.window.getWindow().setCursor(normalPointer);
            }
        }
    }
}
