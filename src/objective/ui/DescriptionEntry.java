/*
 * ObjectiveAccounts, accounting for small professional services firms.
 *
 * Copyright © 2011 Operational Dynamics Consulting, Pty Ltd
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
package objective.ui;

import org.gnome.gdk.EventFocus;
import org.gnome.gtk.Entry;
import org.gnome.gtk.Widget;

/**
 * A simple wrapper around Entry to provide an Updated notification if the
 * value has actually changed.
 * 
 * @author Andrew Cowie
 */
public class DescriptionEntry extends Entry
{
    private DescriptionEntry.Updated handler;

    private String cache;

    public interface Updated
    {
        public void onUpdated(String description);
    }

    public DescriptionEntry() {
        super();
        cache = "";
        super.setSizeRequest(250, -1);
    }

    public void setText(String str) {
        super.setText(str);
        cache = str;
    }

    /**
     * Connect a <code>DescriptionEntry.Updated</code> handler.
     */
    public void connect(DescriptionEntry.Updated handler0) {
        if (this.handler != null) {
            throw new AssertionError();
        }

        this.handler = handler0;

        this.connect(new Entry.Activate() {
            public void onActivate(Entry source) {
                final String str;

                str = source.getText();

                if (str.equals(cache)) {
                    return;
                }
                cache = str;

                handler.onUpdated(str);
            }
        });

        this.connect(new FocusOutEvent() {
            public boolean onFocusOutEvent(Widget source, EventFocus event) {
                source.activate();
                return false;
            }
        });
    }
}
