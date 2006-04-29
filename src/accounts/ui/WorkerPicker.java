/*
 * WorkerPicker.java
 * 
 * See LICENCE file for usage and redistribution terms
 * Copyright (c) 2006 Operational Dynamics
 */
package accounts.ui;

import java.util.Iterator;
import java.util.List;

import org.gnu.gdk.Color;
import org.gnu.gtk.ComboBoxEntry;
import org.gnu.gtk.DataColumn;
import org.gnu.gtk.DataColumnObject;
import org.gnu.gtk.DataColumnString;
import org.gnu.gtk.Entry;
import org.gnu.gtk.HBox;
import org.gnu.gtk.ListStore;
import org.gnu.gtk.StateType;
import org.gnu.gtk.TreeIter;
import org.gnu.gtk.event.ComboBoxEvent;
import org.gnu.gtk.event.ComboBoxListener;

import accounts.domain.Employee;
import accounts.domain.Worker;
import accounts.persistence.DataClient;

/**
 * A picker allowing you to choose from a list of Workers. This delegates to and
 * wraps a ComboBoxEntry. The prototype given to the constructor constrains the
 * list. For instance, for all the Employees, do
 * 
 * <pre>
 * picker = new WorkerPicker(Employee.class);
 * </pre>
 * 
 * @author Andrew Cowie
 */
public class WorkerPicker extends HBox
{
	private transient Worker	worker;

	private DataColumnString	nameDisplay_DataColumn;
	private DataColumnObject	workerObject_DataColumn;
	private ListStore			listStore;

	private ComboBoxEntry		worker_ComboBoxEntry;

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
		workerObject_DataColumn = new DataColumnObject();

		DataColumn[] workerPicker_DataColumnArray = {
			nameDisplay_DataColumn,
			workerObject_DataColumn
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
		 * Build the UI that this Widget represnts. ComboBoxEntry automatically
		 * hooks up and packs a CellRenderer for the nominated column.
		 */

		worker_ComboBoxEntry = new ComboBoxEntry(listStore, 0);

		this.packStart(worker_ComboBoxEntry, false, false, 0);

		/*
		 * Somewhat hardcoded, it turns out that GtkComboBoxEntry have only one
		 * child, and it's a GtkEntry. Lovely. We can use that to play games
		 * with the colour and what not.
		 */
		final Entry entry = (Entry) worker_ComboBoxEntry.getChild();

		worker_ComboBoxEntry.addListener(new ComboBoxListener() {
			public void comboBoxEvent(ComboBoxEvent event) {
				if (event.getType() == ComboBoxEvent.Type.CHANGED) {
					worker_ComboBoxEntry.popup();

					worker = getSelection();

					if (worker == null) {
						entry.setTextColor(StateType.NORMAL, Color.RED);
					} else {
						entry.setTextColor(StateType.NORMAL, Color.BLACK);
					}
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
	 * @param reference
	 *            The Identifier object to be set as active.
	 * @throws IllegalArgumentException
	 *             if you are so foolish as to tell it to programmatically tell
	 *             it to select an Worker which isn't in the set of Workers
	 *             represented by this widget as constrained.
	 */
	public void setWorker(Worker worker) {
		this.worker = worker;

		TreeIter pointer = listStore.getFirstIter();
		while (pointer != null) {
			if (listStore.getValue(pointer, workerObject_DataColumn) == worker) {
				worker_ComboBoxEntry.setActiveIter(pointer);
				return;
			}
			pointer = pointer.getNextIter();
		}

		throw new IllegalArgumentException(
			"How did you manage to ask to activate a Worker object that isn't in the set of Workers represented by this picker?");
	}

	public void refresh() {
		// BUG FIXME ?
	}
}
