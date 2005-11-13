/*
 * ReimbursableExpensesEditorWindow.java
 * 
 * See LICENCE file for usage and redistribution terms
 * Copyright (c) 2005 Operational Dynamics
 */
package accounts.ui;

import org.gnu.gtk.ComboBoxEntry;
import org.gnu.gtk.Gtk;
import org.gnu.gtk.Table;

import accounts.persistence.UnitOfWork;

/**
 * A window where the expenses incurred by an employee
 * 
 * @author Andrew Cowie
 */
public class ReimbursableExpensesEditorWindow extends EditorWindow
{
	/*
	 * Cached widgets
	 */
	protected Table					_table;

	protected ComboBoxEntry			_who_comboboxentry;
	protected DatePicker			_datePicker;
	protected AccountPicker			_accountPicker;
	protected ForeignAmountEntryBox	_amountEntryBox;

	private UnitOfWork				_uow;

	public ReimbursableExpensesEditorWindow() {
		super("ReimbursableExpensesEditor_window", "share/ReimbursableExpensesEditorWindow.glade");

		_datePicker = new DatePicker();

		_table = (Table) _glade.getWidget("general_table");
		_table.attach(_datePicker, 1, 2, 1, 2);

		_accountPicker = new AccountPicker();

		_table.attach(_accountPicker, 1, 2, 2, 3);

		_who_comboboxentry = (ComboBoxEntry) _glade.getWidget("who_comboboxentry");
		_who_comboboxentry.appendText("Andrew Cowie");
		_who_comboboxentry.setActive(0);

		_amountEntryBox = new ForeignAmountEntryBox();
		_table.attach(_amountEntryBox, 1, 2, 3, 4);

		_accountPicker.grabFocus();
		_window.showAll();
		_window.present();

		_uow = new UnitOfWork("ReimbursableExpensesEditorWindow");
	}

	/**
	 * (overrides {@link GladeWindow#deleteHook()})
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
		_uow.cancel();
		super.cancel();
	}

	protected void ok() {
		System.out.println("Warning: ok() action not implemented");
		_uow.cancel();
		super.ok();
	}
}
