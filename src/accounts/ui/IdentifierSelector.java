/*
 * IdentifierSelector.java
 * 
 * See LICENCE file for usage and redistribution terms
 * Copyright (c) 2006 Operational Dynamics
 */
package accounts.ui;

import java.util.Iterator;
import java.util.List;

import org.gnu.gtk.CellRendererText;
import org.gnu.gtk.ComboBox;
import org.gnu.gtk.DataColumn;
import org.gnu.gtk.DataColumnObject;
import org.gnu.gtk.DataColumnString;
import org.gnu.gtk.ListStore;
import org.gnu.gtk.TreeIter;

import accounts.domain.Identifier;
import accounts.domain.IdentifierGroup;

/**
 * A ComboBox populated with Identifier objects from an IdentifierGroup,
 * allowing you to select one.
 * 
 * @author Andrew Cowie
 */
public class IdentifierSelector extends ComboBox
{
	private DataColumnString	nameDisplay_DataColumn;
	private DataColumnObject	identifierObject_DataColumn;
	private ListStore			listStore;

	/**
	 * Construct a Identifier picker given a specified IdentifierGroup.
	 * 
	 * @param group
	 *            the IdentifierGroup from whose members you want to choose an
	 *            Identifier. The assumption is made that the IdentifierGroup is
	 *            one that has been (or will be) stored, with the further
	 *            implication that the selected member is (can, will be) a valid
	 *            object reference in the database because that's where you got
	 *            it from, presumably.
	 */
	public IdentifierSelector(IdentifierGroup group) {
		super((ListStore) null);

		/*
		 * We go to the considerable effort of having a TreeModel here so that
		 * we can store a reference to the Identifier object that is being
		 * picked.
		 */
		nameDisplay_DataColumn = new DataColumnString();
		identifierObject_DataColumn = new DataColumnObject();

		DataColumn[] currencySelector_DataColumnArray = {
			nameDisplay_DataColumn,
			identifierObject_DataColumn
		};

		listStore = new ListStore(currencySelector_DataColumnArray);

		/*
		 * Poppulate
		 */
		List iL = group.getIdentifiers();
		Iterator iI = iL.iterator();
		while (iI.hasNext()) {
			Identifier i = (Identifier) iI.next();
			TreeIter pointer = listStore.appendRow();

			/*
			 * Hmm. Tried using TextOutput.wrap(i.getName(), 20) but ComboBox
			 * seems to ignore the fact that wrapping with newlines reduces the
			 * effective width of the String, instead using the raw String
			 * length to size the drop down. Damn and other comments.
			 */
			listStore.setValue(pointer, nameDisplay_DataColumn, i.getName());
			listStore.setValue(pointer, identifierObject_DataColumn, i);
		}

		/*
		 * Build the UI that this Widget represnts.
		 */
		this.setModel(listStore);

		CellRendererText name_CellRenderer = new CellRendererText();
		this.packStart(name_CellRenderer, true);
		this.addAttributeMapping(name_CellRenderer, CellRendererText.Attribute.TEXT, nameDisplay_DataColumn);
	}

	/**
	 * Get the Identifier currently selected.
	 * 
	 * @return the Identifier object that is stored in the row alongside the
	 *         displayed text which the user used to pick, or null if there
	 *         isn't anything selected.
	 */
	public Identifier getSelection() {
		TreeIter pointer = this.getActiveIter();
		if (pointer == null) {
			return null;
		}
		return (Identifier) listStore.getValue(pointer, identifierObject_DataColumn);

	}

	/**
	 * Set the specified Identifier as active in the ComboBox.
	 * 
	 * @param identifier
	 *            The Identifier object to be set as active.
	 * @throws IllegalArgumentException
	 *             if you are so foolish as to tell it to select a Currency
	 *             object which isn't in the system currency table.
	 */
	public void setIdentifier(Identifier identifier) {
		TreeIter pointer = listStore.getFirstIter();

		while (pointer != null) {
			if (listStore.getValue(pointer, identifierObject_DataColumn) == identifier) {
				this.setActiveIter(pointer);
				return;
			}
			pointer = pointer.getNextIter();
		}
		throw new IllegalArgumentException(
			"How did you manage to ask to activate a Identifier object that isn't in the IdentifierGroup represented by this picker?");
	}

	public void refresh() {
		// BUG FIXME
		/**
		 * If the IdentifierGroup is changed, this widget needs to be signalled
		 * to reload its data... it IS a TreeModel, so just update it.?.
		 */
	}

}
