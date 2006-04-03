/*
 * ReimbursableExpensesEditorWindow.java
 * 
 * See LICENCE file for usage and redistribution terms
 * Copyright (c) 2005-2006 Operational Dynamics
 */
package accounts.ui;

import org.gnu.gtk.ComboBoxEntry;
import org.gnu.gtk.Gtk;
import org.gnu.gtk.Table;

import accounts.persistence.UnitOfWork;

/**
 * A Window where the expenses incurred by an Employee
 * 
 * @author Andrew Cowie
 */
public class ReimbursableExpensesEditorWindow extends EditorWindow
{
	/*
	 * Cached widgets
	 */
	protected Table					table;

	protected ComboBoxEntry			who_comboboxentry;
	protected DatePicker			datePicker;
	protected AccountPicker			accountPicker;
	protected ForeignAmountEntryBox	amountEntryBox;

	/**
	 * Construct the Window. Uses the table from the glade file extensively.
	 * Takes a UnitOfWork for itself when ready and then returns.
	 */
	public ReimbursableExpensesEditorWindow() {
		super("reimbursable", "share/ReimbursableExpensesEditorWindow.glade");

		datePicker = new DatePicker();

		table = (Table) gladeParser.getWidget("general_table");
		table.attach(datePicker, 1, 2, 1, 2);

		accountPicker = new AccountPicker();

		table.attach(accountPicker, 1, 2, 2, 3);

		who_comboboxentry = (ComboBoxEntry) gladeParser.getWidget("who_comboboxentry");
		who_comboboxentry.appendText("Andrew Cowie");
		who_comboboxentry.setActive(0);

		amountEntryBox = new ForeignAmountEntryBox();
		table.attach(amountEntryBox, 1, 2, 3, 4);

		accountPicker.grabFocus();
		window.showAll();
		window.present();

		uow = new UnitOfWork(me);
	}

	/**
	 * (overrides {@link AbstractWindow#deleteHook()})
	 */
	public boolean deleteHook() {
		// hide & destroy
		super.deleteHook();
		// quit
		System.out.println("Notice: deleteHook() overriden to call Gtk.mainQuit()");
		Gtk.mainQuit();
		return false;
	}

	protected void cancel() {
		uow.cancel();
		super.cancel();
	}

	protected void ok() {
		System.out.println("Warning: ok() action not implemented");
		uow.cancel(); // FIXME change me; overrides calling commit()
		super.ok();
	}
}
