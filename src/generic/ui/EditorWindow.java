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

import generic.persistence.DataClient;
import generic.persistence.Engine;
import generic.util.Debug;

import org.gnome.gtk.Button;
import org.gnome.gtk.ButtonBoxStyle;
import org.gnome.gtk.HButtonBox;
import org.gnome.gtk.HSeparator;
import org.gnome.gtk.Stock;

/**
 * A great number of UI windows follow the pattern of being either editors or
 * complementary viewers, named in parallel. This branch of sublcasses from
 * AbstractWindow denote the editors.
 * <p>
 * EditorWindow subclasses must implement <code>cancel()</code> and
 * <code>ok()</code>, corresponding to the actions taken when buttons by those
 * names are pressed. The default methods here just call
 * <code>deleteHook()</code>; calling <code>super.ok()</code> or
 * <code>super.cancel()</code> allows you to leverage this code.
 * 
 * @author Andrew Cowie
 */
public abstract class EditorWindow extends PrimaryWindow
{
    private Button cancel;

    private Button ok;

    /**
     * Every EditorWindow subclass needs a UnitOfWork via which to make
     * changes to the database. So here it is; take out an instance in your
     * ok() method.
     */
    protected DataClient store;

    /**
     * Basic form of EditorWindow. Calls PrimaryWindow's constructor, then
     * adds the button box with ok and close.
     */
    protected EditorWindow() {
        /*
         * Construct the basic form
         */
        super();
        store = Engine.gainClient();
        addButtons();
    }

    /**
     * Glade form of EditorWindow. Passes the parameters to PrimaryWindow's
     * glade constructor, then adds the button box with ok and close.
     * 
     * @param whichElement
     * @param gladeFilename
     */
    protected EditorWindow(String whichElement, String gladeFilename) {
        /*
         * Construct the glade form
         */
        super(whichElement, gladeFilename);
        store = Engine.gainClient();
        addButtons();
    }

    private void addButtons() {
        HButtonBox buttonbox = new HButtonBox();
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

        top.packEnd(buttonbox, false, false, 0);
        HSeparator sep = new HSeparator();
        top.packEnd(sep, false, false, 3);
    }

    /**
     * The cases of cancel and ok need to be done on a specific basis by the
     * implementing classes, as is appropriate to their situation. The default
     * implementations here simply call AbstractWindow's deleteHook() to cause
     * the Window to be dismissed. <b>These do not commit() or rollback() the
     * {@link #store} DataClient that EditorWindow provides. It's too
     * important. If you want to commit() you have to choose to do this in
     * your subclass.</b>
     */
    protected void cancel() {
        deleteHook();
    }

    protected void ok() {
        deleteHook();
    }

    /**
     * Release the DataClient back to the Engine and then call
     * {@link AbstractWindow#deleteHook()} which should hide and destroy the
     * Window. See that method for a description of the meaning of the boolean
     * return value. <b>If you override this and don't in turn call this one,
     * then you'll have to release the {@link #store} client yourself.</b>
     */
    protected boolean deleteHook() {
        super.deleteHook();
        new Thread() {
            public void run() {
                Debug.print("threads", "Releasing client");
                Engine.releaseClient(store);
                store = null;
            }
        }.start();

        return false;
    }

    protected void finalize() throws Throwable {
        if (store != null) {
            Debug.print("memory", "DataClient reference still held in " + getClassString());
            Engine.releaseClient(store);
        }
        super.finalize();
    }
}
