/*
 * ReimbusableExpensesWindow.java
 * 
 * See LICENCE file for usage and redistribution terms
 * Copyright (c) 2005 Operational Dynamics
 */
package accounts.ui;

import org.gnu.gtk.ComboBoxEntry;
import org.gnu.gtk.Gtk;
import org.gnu.gtk.Table;

import accounts.domain.Account;
import accounts.domain.Datestamp;

/**
 * A window where the expenses incurred by an employee
 * 
 * @author Andrew Cowie
 */
public class ReimbusableExpensesWindow extends EditorWindow
{
	/*
	 * Cached widgets
	 */
	protected Table			_table;
	
	protected ComboBoxEntry	_who_comboboxentry;
	protected DatePicker	_datePicker;
	protected AccountPicker	_accountPicker;
	protected ForeignAmountEntryBox	_amountEntryBox;

	/*
	 * Original state
	 */
	private int				_original_whoIndex;
	private Datestamp		_original_date;
	private Account			_original_account;

	public ReimbusableExpensesWindow() {
		super("expenses", "share/ReimbusableExpensesWindow.glade");

		_datePicker = new DatePicker();

		_table = (Table) _glade.getWidget("general_table");
		_table.attach(_datePicker, 1, 2, 1, 2);
		_table.setSensitive(false);

		_accountPicker = new AccountPicker();

		_table.attach(_accountPicker, 1, 2, 2, 3);

		_who_comboboxentry = (ComboBoxEntry) _glade.getWidget("who_comboboxentry");
		_who_comboboxentry.appendText("Andrew Cowie");
		_who_comboboxentry.setActive(0);

		setStateAsOriginal();

		_amountEntryBox = new ForeignAmountEntryBox();
		_table.attach(_amountEntryBox, 1, 2, 3, 4);
		
		_window.showAll();
		_window.present();
	}

	private void setStateAsOriginal() {
		_original_whoIndex = _who_comboboxentry.getActive();
		_original_date = _datePicker.getDate();
		_original_account = _accountPicker.getAccount();

		// System.out.println("Original who: "+_original_whoIndex);
		// System.out.println("Original date: "+_original_date);
	}

	private void restoreOriginalState() {
		_who_comboboxentry.setActive(_original_whoIndex);
		_datePicker.setDate(_original_date);
		_accountPicker.setAccount(_original_account);
	}

	/*
	 * TODO Here's a hideous thought - maybe every open Edit window (or any
	 * window, for that matter) should have it's own connection to the
	 * DataStore, and therefore it's own view of the data and it's own
	 * transaction. That way, any Ok in one window would not have the effect of
	 * disallowing the Cancel in another. *Every* window, including read only
	 * display windows like the one showing the balance sheet, would do a
	 * refresh on all domain objects [after receiving a commit message?]. Ugh -
	 * a lot of machinery. ... No, probably not - only things that get committed
	 * on Apply are those that have had a set() call done, and that only
	 * happens, object by object, on Apply.
	 */

	/**
	 * (overrides {@link GladeWindow#deleteHook()})
	 */
	public boolean deleteHook() {
		System.out.println("Overriden close, calling Gtk.mainQuit()");
		Gtk.mainQuit();
		return false;
	}

	protected void close() {
		super.close();
		deleteHook();
	}

	protected void edit() {
		_table.setSensitive(true);
		_amountEntryBox.grabFocus();
		super.edit();
	}

	protected void cancel() {
		restoreOriginalState();
		_table.setSensitive(false);
		super.cancel();
	}

	protected void apply() {
		setStateAsOriginal();
		_table.setSensitive(false);
		super.apply();
	}
}
