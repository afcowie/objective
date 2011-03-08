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
package objective.ui;

import objective.domain.Worker;
import objective.persistence.DataStore;

import org.gnome.gtk.ComboBox;
import org.gnome.gtk.ComboBoxEntry;
import org.gnome.gtk.DataColumn;
import org.gnome.gtk.DataColumnReference;
import org.gnome.gtk.DataColumnString;
import org.gnome.gtk.Entry;
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
public class WorkerPicker extends ComboBoxEntry
{
    private Worker worker;

    private DataColumnString nameDisplayColumn;

    private DataColumnReference<Worker> workerObjectColumn;

    private ListStore listStore;

    private ComboBoxEntry combo;

    private WorkerPicker.Updated handler = null;

    /**
     * Construct a WorkerPicker.
     */
    public WorkerPicker(DataStore data) {
        /*
         * This class is a Box subclass only so the Entry doesn't swell
         * unnessarily if set in a Table or similar widget. Otherwise, the box
         * nature is transparent.
         */
        super();
        final Worker[] workers;
        final DataColumn[] columns;
        final Entry entry;

        /*
         * We go to the considerable effort of having a TreeModel here so that
         * we can store a reference to the Worker object that is being picked.
         */

        nameDisplayColumn = new DataColumnString();
        workerObjectColumn = new DataColumnReference<Worker>();

        columns = new DataColumn[] {
            nameDisplayColumn,
            workerObjectColumn
        };

        listStore = new ListStore(columns);

        /*
         * Poppulate.
         */

        workers = data.listWorkers();
        for (Worker w : workers) {
            TreeIter pointer = listStore.appendRow();
            listStore.setValue(pointer, nameDisplayColumn, w.getName());
            listStore.setValue(pointer, workerObjectColumn, w);
        }

        /*
         * Build the UI that this Widget repreesnts. ComboBoxEntry
         * automatically hooks up and packs a CellRenderer for the nominated
         * column.
         */

        combo = this;
        combo.setModel(listStore);
        combo.setTextColumn(nameDisplayColumn);

        /*
         * Somewhat hardcoded, it turns out that GtkComboBoxEntry have only
         * one child, and it's a GtkEntry. Lovely. We can use that to play
         * games with the colour and what not.
         */

        entry = (Entry) combo.getChild();

        combo.connect(new ComboBox.Changed() {
            public void onChanged(ComboBox source) {
                combo.popup();

                worker = getSelection();

                if (worker == null) {
                    entry.modifyText(NORMAL, RED);
                    return;
                } else {
                    entry.modifyText(NORMAL, BLACK);
                }
                if (handler != null) {
                    handler.onUpdated(worker);
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
        TreeIter pointer = combo.getActiveIter();
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
                combo.setActiveIter(pointer);
                return;
            }
        } while (pointer.iterNext());

        throw new IllegalArgumentException(
                "How did you manage to ask to activate a Worker object that isn't in the set of Workers represented by this picker?");
    }

    /**
     * Hook up a WorkedPicker.Updated handler.
     */
    /*
     * This was set up in testing; not really clear if we need this for real.
     */
    public void connect(WorkerPicker.Updated handler) {
        if (this.handler != null) {
            throw new IllegalStateException("You can't have more than one WorkerPicker.Update");
        }
        this.handler = handler;
    }

    interface Updated
    {
        void onUpdated(Worker worker);
    }
}
