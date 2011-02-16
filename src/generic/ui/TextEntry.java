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

import org.gnome.gtk.Editable;
import org.gnome.gtk.Entry;

/**
 * A simple wrapper aroung GTK's Entry. Hooks up its listeners and fires off
 * our local ChangeListener on an actual user derived change to the Entry's
 * content, as opposed to a programatic one.
 * 
 * @author Andrew Cowie
 */
/*
 * While the "only do something if focused" pattern is a common one, one of
 * the motivations for this was to not have to avoid by fully qualified class
 * names in UI classes dealing with accounts.domain.Entry objects.
 */
public class TextEntry extends Entry
{
    private ChangeListener changeListener = null;

    /**
     * Create a new widget, hooking up listener to fire
     * {@link ChangeListener#userChangedData()} when the contents of the
     * GtkEntry are changed.
     * 
     */
    public TextEntry() {
        super();

        connect(new Entry.Changed() {
            public void onChanged(Editable source) {
                if (!((Entry) source).getHasFocus()) {
                    return;
                }

                if (changeListener != null) {
                    changeListener.userChangedData();
                }
            }
        });
    }

    public void addListener(ChangeListener listener) {
        if (changeListener != null) {
            throw new IllegalStateException("Can only add one ChangeListener");
        }
        changeListener = listener;
    }
}
