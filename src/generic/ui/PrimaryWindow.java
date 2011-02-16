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

import generic.client.Master;

/**
 * Denote a window as being one of the primary ones which a user interacts
 * with. These windows will be registered with {@link generic.client.Master}
 * and thus be available to be shown in drop down Window menu lists, etc. More
 * importantly, they will all be hidden immediately and asked to delete on a
 * call to {@link generic.client.Master#shutdown() Master.shutdown()}.
 * 
 * @author Andrew Cowie
 */
public abstract class PrimaryWindow extends AbstractWindow
{
    /**
     * Basic form of a PrimaryWindow. Calls AbstractWindow's no-arg
     * constructor, then registers this window with Master.
     */
    public PrimaryWindow() {
        super();
        Master.ui.regsiterWindow(this);
    }

    /**
     * Glade form of a PrimaryWindow. Passes the parameters to
     * AbstractWindow's glade constructor, then registers this window with
     * Master.
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
     * Quietly expose <code>hide()</code> so that Master can silence them
     * while shutting down. You could just as easily access
     * <code>window.hide()</code>
     */
    protected void hide() {
        window.hide();
    }
}
