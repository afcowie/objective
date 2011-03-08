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
package accounts.ui;

import objective.domain.Employee;
import objective.domain.Worker;
import objective.persistence.DataStore;

import org.gnome.gtk.ComboBox;
import org.gnome.gtk.ComboBoxEntry;
import org.gnome.gtk.DataColumn;
import org.gnome.gtk.DataColumnReference;
import org.gnome.gtk.DataColumnString;
import org.gnome.gtk.Entry;
import org.gnome.gtk.HBox;
import org.gnome.gtk.ListStore;
import org.gnome.gtk.TreeIter;


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

    private DataColumnString nameDisplayColumn;

    private DataColumnReference<Worker> workerObjectColumn;

    private ListStore listStore;

    private ComboBoxEntry workerCombo;

    /**
     * Construct a WorkerPicker.
     * 
     * @param proto
     *            a prototype to constrain the search for candidate Workers to
     *            include in the dropdown.
     */
    public WorkerPicker(DataStore data) {
        /*
         * This class is a Box subclass only so the Entry doesn't swell
         * unnessarily if set in a Table or similar widget. Otherwise, the box
         * nature is transparent.
         */
        super(false, 0);

        /*
         * We go to the considerable effort of having a TreeModel here so that
         * we can store a reference to the Worker object that is being picked.
         */
        nameDisplayColumn = new DataColumnString();
        workerObjectColumn = new DataColumnReference<Worker>();

        DataColumn[] workerPicker_DataColumnArray = {
            nameDisplayColumn,
            workerObjectColumn
        };

        listStore = new ListStore(workerPicker_DataColumnArray);

        /*
         * Poppulate.
         */

        // TODO

        Worker[] workers = new Worker[] {
            new Employee("Andrew Cowie"),
        };

        for (Worker w : workers) {
            TreeIter pointer = listStore.appendRow();
            listStore.setValue(pointer, nameDisplayColumn, w.getName());
            listStore.setValue(pointer, workerObjectColumn, w);
        }

        /*
         * Build the UI that this Widget represnts. ComboBoxEntry
         * automatically hooks up and packs a CellRenderer for the nominated
         * column.
         */

        workerCombo = new ComboBoxEntry(listStore, nameDisplayColumn);

        this.packStart(workerCombo, false, false, 0);

        /*
         * Somewhat hardcoded, it turns out that GtkComboBoxEntry have only
         * one child, and it's a GtkEntry. Lovely. We can use that to play
         * games with the colour and what not.
         */
        final Entry entry = (Entry) workerCombo.getChild();

        workerCombo.connect(new ComboBox.Changed() {
            public void onChanged(ComboBox source) {
                workerCombo.popup();

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
        TreeIter pointer = workerCombo.getActiveIter();
        if (pointer == null) {
            return null;
        }
        return listStore.getValue(pointer, workerObjectColumn);
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
            if (listStore.getValue(pointer, workerObjectColumn) == worker) {
                workerCombo.setActiveIter(pointer);
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
