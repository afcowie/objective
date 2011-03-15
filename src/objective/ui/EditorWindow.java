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
package objective.ui;

import org.gnome.gtk.Button;
import org.gnome.gtk.ButtonBoxStyle;
import org.gnome.gtk.HButtonBox;
import org.gnome.gtk.HSeparator;
import org.gnome.gtk.Stock;
import org.gnome.gtk.VBox;
import org.gnome.gtk.Window;

/**
 * A great number of UI windows follow the pattern of being either editors or
 * complementary viewers, named in parallel.
 * 
 * <p>
 * EditorWindow subclasses must implement <code>cancel()</code> and
 * <code>ok()</code>, corresponding to the actions taken when buttons by those
 * names are pressed.
 * 
 * @author Andrew Cowie
 */
public abstract class EditorWindow extends Window
{
    protected final Window window;

    protected final VBox top;

    private Button cancel;

    private Button ok;

    /**
     * Basic form of EditorWindow. Calls PrimaryWindow's constructor, then
     * adds the button box with ok and close.
     */
    protected EditorWindow() {
        super();

        window = this;
        top = new VBox(false, 0);

        addButtons();

        window.add(top);
    }

    private void addButtons() {
        final HButtonBox buttonbox;
        final HSeparator separator;

        buttonbox = new HButtonBox();
        buttonbox.setLayout(ButtonBoxStyle.END);

        cancel = new Button(Stock.CANCEL);
        cancel.connect(new Button.Clicked() {
            public void onClicked(Button source) {
                cancel();
            }
        });
        buttonbox.add(cancel);

        ok = new Button(Stock.OK);
        ok.connect(new Button.Clicked() {
            public void onClicked(Button source) {
                ok();
            }
        });
        buttonbox.add(ok);

        top.packEnd(buttonbox, false, false, 3);
        separator = new HSeparator();
        top.packEnd(separator, false, false, 3);
    }

    protected void cancel() {
        window.hide();
        window.destroy();
    }

    protected void ok() {}
}
