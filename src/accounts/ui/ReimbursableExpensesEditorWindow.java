/*
 * ReimbursableExpensesEditorWindow.java
 * 
 * See LICENCE file for usage and redistribution terms
 * Copyright (c) 2005-2006 Operational Dynamics
 */
package accounts.ui;

import generic.ui.AbstractWindow;

import java.io.PrintWriter;

import org.gnu.gtk.ComboBoxEntry;
import org.gnu.gtk.Gtk;
import org.gnu.gtk.Table;

import accounts.domain.Account;
import accounts.domain.Books;
import accounts.domain.Currency;
import accounts.domain.ForeignAmount;

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

		accountPicker = new AccountPicker(store);

		table.attach(accountPicker, 1, 2, 2, 3);

		who_comboboxentry = (ComboBoxEntry) gladeParser.getWidget("who_comboboxentry");
		who_comboboxentry.appendText("Andrew Cowie");
		who_comboboxentry.setActive(0);

		amountEntryBox = new ForeignAmountEntryBox(store);
		table.attach(amountEntryBox, 1, 2, 3, 4);

		window.showAll();
		window.present();
	}

	/**
	 * (overrides {@link EditorWindow#deleteHook()})
	 */
	public boolean deleteHook() {
		// hide & destroy
		super.deleteHook();
		// quit
		System.out.println("Notice: deleteHook() overriden to call Gtk.mainQuit()");
		Gtk.mainQuit();
		return false;
	}

	protected void ok() {
		System.out.println("Warning: ok() action not implemented");
		store.rollback(); // FIXME change me; overrides calling commit()

		// TODO remove - just demo code.

		final PrintWriter out = new PrintWriter(System.out);
		out.println("\nDate:\t\t" + datePicker.getDate());

		final Account a = accountPicker.getAccount();
		if (a != null) {
			out.println("Account/Ledger:\t" + accountPicker.getAccount().getTitle() + "|"
				+ accountPicker.getLedger().getName());
		}

		final ForeignAmount f = amountEntryBox.getForeignAmount();
		out.print("Amount:\t\t" + f.getCurrency().getSymbol() + f.toString() + " " + f.getCurrency().getCode());

		final Books root = store.getBooks();
		final Currency home = root.getHomeCurrency();

		if (f.getCurrency() != home) {
			out.println(" [" + f.getRate() + " -> " + home.getSymbol() + f.getValue() + " " + home.getCode() + "]");
		} else {
			out.println();
		}
		out.println();

		out.flush();
		out.close();

		super.ok();
	}
}
