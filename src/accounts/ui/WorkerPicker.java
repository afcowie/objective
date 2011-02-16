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
 * contacted through http://research.operationaldynamics.com/projects/objective/.
 */
package accounts.ui;

import generic.persistence.DataClient;

import java.util.Iterator;
import java.util.List;

import org.gnome.gtk.ComboBox;
import org.gnome.gtk.ComboBoxEntry;
import org.gnome.gtk.DataColumn;
import org.gnome.gtk.DataColumnReference;
import org.gnome.gtk.DataColumnString;
import org.gnome.gtk.Entry;
import org.gnome.gtk.HBox;
import org.gnome.gtk.ListStore;
import org.gnome.gtk.TreeIter;

import accounts.domain.Employee;
import accounts.domain.Worker;

import static org.gnome.gdk.Color.BLACK;
import static org.gnome.gdk.Color.RED;
import static org.gnome.gtk.StateType.NORMAL;

/**
 * A picker allowing you to choose from a list of Workers. This delegates to
 * and wraps a ComboBoxEntry. The prototype given to the constructor
 * constrains the list. For instance, for all the Employees, do
 * 
 * <pre>
 * picker = new WorkerPicker(Employee.class);
 * </pre>
 * 
 * @author Andrew Cowie
 */
public class WorkerPicker extends HBox
{
    private transient Worker worker;

    private DataColumnString nameDisplay_DataColumn;

    private DataColumnReference workerObject_DataColumn;

    private ListStore listStore;

    private ComboBoxEntry worker_ComboBoxEntry;

    /**
     * Construct a WorkerPicker.
     * 
     * @param proto
     *            a prototype to constrain the search for candidate Workers to
     *            include in the dropdown.
     */
    public WorkerPicker(DataClient store, Object proto) {
        /*
         * This class is a Box subclass only so the Entry doesn't swell
         * unnessarily if set in a Table or similar widget. Otherwise, the box
         * nature is transparent.
         */
        super(false, 0);

        if (!((proto instanceof Worker) || (proto instanceof Class))) {
            throw new IllegalArgumentException();
        }

        /*
         * We go to the considerable effort of having a TreeModel here so that
         * we can store a reference to the Worker object that is being picked.
         */
        nameDisplay_DataColumn = new DataColumnString();
        workerObject_DataColumn = new DataColumnReference();

        DataColumn[] workerPicker_DataColumnArray = {
                nameDisplay_DataColumn, workerObject_DataColumn
        };

        listStore = new ListStore(workerPicker_DataColumnArray);

        /*
         * Poppulate.
         */

        List eL = store.queryByExample(proto);

        if (eL.size() == 0) {
            // TODO replace with NotFoundException
            throw new IllegalStateException(
                    "You've managed to try and instantiate a WorkerPicker with a prototype which resulted in no results. This may well be a valid state, but the application is going to need to deal with this.");
        }

        Iterator eI = eL.iterator();
        while (eI.hasNext()) {
            Employee e = (Employee) eI.next();
            TreeIter pointer = listStore.appendRow();
            listStore.setValue(pointer, nameDisplay_DataColumn, e.getName());
            listStore.setValue(pointer, workerObject_DataColumn, e);
        }

        /*
         * Build the UI that this Widget represnts. ComboBoxEntry
         * automatically hooks up and packs a CellRenderer for the nominated
         * column.
         */

        worker_ComboBoxEntry = new ComboBoxEntry(listStore, nameDisplay_DataColumn);

        this.packStart(worker_ComboBoxEntry, false, false, 0);

        /*
         * Somewhat hardcoded, it turns out that GtkComboBoxEntry have only
         * one child, and it's a GtkEntry. Lovely. We can use that to play
         * games with the colour and what not.
         */
        final Entry entry = (Entry) worker_ComboBoxEntry.getChild();

        worker_ComboBoxEntry.connect(new ComboBox.Changed() {
            public void onChanged(ComboBox source) {
                worker_ComboBoxEntry.popup();

                worker = getSelection();

                if (worker == null) {
                    entry.modifyText(NORMAL, RED);
                } else {
                    entry.modifyText(NORMAL, BLACK);
                }
            }
        });
    }

    /**
     * Get the Worker currently selected in the ComboBoxEntry.
     * 
     * @return the Worker object that is stored in the row alongside the
     *         displayed text which the user used to pick, or null if there
     *         isn't anything selected.
     */
    private Worker getSelection() {
        TreeIter pointer = worker_ComboBoxEntry.getActiveIter();
        if (pointer == null) {
            return null;
        }
        return (Worker) listStore.getValue(pointer, workerObject_DataColumn);
    }

    /**
     * @return the Worker object currently selected (by user or
     *         programmatically) according to this Widget.
     */
    public Worker getWorker() {
        return worker;
    }

    /**
     * Set the specified Worker object as the one to be active in this Widget.
     * 
     * @throws IllegalArgumentException
     *             if you are so foolish as to tell it to programmatically
     *             tell it to select an Worker which isn't in the set of
     *             Workers represented by this widget as constrained.
     */
    public void setWorker(Worker worker) {
        TreeIter pointer;

        this.worker = worker;

        pointer = listStore.getIterFirst();
        do {
            if (listStore.getValue(pointer, workerObject_DataColumn) == worker) {
                worker_ComboBoxEntry.setActiveIter(pointer);
                return;
            }
        } while (pointer.iterNext());

        throw new IllegalArgumentException(
                "How did you manage to ask to activate a Worker object that isn't in the set of Workers represented by this picker?");
    }

    public void refresh() {
    // BUG FIXME ?
    }
}
