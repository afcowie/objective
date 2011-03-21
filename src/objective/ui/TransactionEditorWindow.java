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

import objective.domain.Transaction;
import objective.persistence.DataStore;
import objective.services.TransactionOperations;

import org.gnome.gtk.Alignment;
import org.gnome.gtk.HBox;
import org.gnome.gtk.HSeparator;
import org.gnome.gtk.Label;
import org.gnome.gtk.SizeGroup;
import org.gnome.gtk.SizeGroupMode;
import org.gnome.gtk.Widget;

/**
 * Enter or edit expenses incurred by and reimbursable to a Worker.
 * 
 * @author Andrew Cowie
 */
public abstract class TransactionEditorWindow extends EditorWindow
{
    protected final DatePicker date;

    protected final DescriptionEntry description;

    protected final SizeGroup group;

    protected final TransactionOperations services;

    private TransactionEditorWindow.Updated handler;

    private Transaction operand;

    /**
     * Construct the top portion of the window with Date, Description, and
     * Reference fields.
     */
    public TransactionEditorWindow(final DataStore data, String heading) {
        super(heading);
        final HSeparator separator;

        group = new SizeGroup(SizeGroupMode.HORIZONTAL);

        services = new TransactionOperations(data);

        /*
         * Date
         */

        date = new DatePicker();
        this.addField("To date:", date);

        /*
         * Description
         */

        description = new DescriptionEntry();
        this.addField("Description:", description);

        /*
         * ----
         */

        separator = new HSeparator();
        top.packStart(separator, false, false, 3);
    }

    protected void addField(String title, Widget widget) {
        final Label label;
        final HBox box;

        box = new HBox(false, 6);

        label = new Label(title);
        label.setAlignment(Alignment.RIGHT, Alignment.CENTER);
        box.packStart(label, false, false, 0);
        group.add(label);

        box.packStart(widget, false, false, 0);

        top.packStart(box, false, false, 0);
    }

    /*
     * Notify parent. Called in from EditorWindow.handleOk()
     */
    protected final void emitUpdated() {
        if (handler != null) {
            handler.onUpdated(operand);
        }
    }

    /**
     * The object of this EditorWindow's affection has changed, and been
     * committed to the database.
     * 
     * @author Andrew Cowie
     */
    public interface Updated
    {
        public void onUpdated(Transaction t);
    }

    public void connect(TransactionEditorWindow.Updated handler) {
        if (this.handler != null) {
            throw new AssertionError();
        }
        this.handler = handler;
    }

    public void setOperand(Transaction t) {
        if (t == null) {
            throw new AssertionError();
        }
        this.operand = t;
    }

    protected Transaction getOperand() {
        return operand;
    }
}
