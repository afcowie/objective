/*
 * DemoWindowWrapper.java
 *
 * Copyright (c) 2007-2008 Operational Dynamics Consulting Pty Ltd
 * 
 * The code in this file, and the library it is a part of, are made available
 * to you by the authors under the terms of the "GNU General Public Licence,
 * version 2" See the LICENCE file for the terms governing usage and
 * redistribution.
 */
package demo.ui;

import org.gnome.gdk.Event;
import org.gnome.gtk.Gtk;
import org.gnome.gtk.Widget;
import org.gnome.gtk.Window;

import accounts.ui.ForeignAmountEntryBox;

public class DemoWindowWrapper
{
    public static void main(String[] args) {
        final Window w;
        final ForeignAmountEntryBox foreign;

        Gtk.init(args);

        foreign = new ForeignAmountEntryBox(null);

        w = new Window();
        w.setBorderWidth(15);
        w.add(foreign);

        w.showAll();
        w.connect(new Window.DeleteEvent() {
            public boolean onDeleteEvent(Widget source, Event event) {
                Gtk.mainQuit();
                return false;
            }
        });

        Gtk.main();
    }
}
