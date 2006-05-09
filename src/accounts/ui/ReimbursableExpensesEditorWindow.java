/*
 * ReimbursableExpensesEditorWindow.java
 * 
 * See LICENCE file for usage and redistribution terms
 * Copyright (c) 2005-2006 Operational Dynamics
 */
package accounts.ui;

import generic.ui.Master;
import generic.ui.ModalDialog;
import generic.util.Debug;

import org.gnu.gtk.Entry;
import org.gnu.gtk.MessageType;
import org.gnu.gtk.Table;

import accounts.domain.Amount;
import accounts.domain.Credit;
import accounts.domain.Debit;
import accounts.domain.Employee;
import accounts.domain.ForeignAmount;
import accounts.domain.Ledger;
import accounts.domain.ReimbursableExpensesTransaction;
import accounts.domain.Worker;
import accounts.services.CommandNotReadyException;
import accounts.services.PostTransactionCommand;

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
	protected Table							table;

	protected WorkerPicker					person_WorkerPicker;
	protected DatePicker					datePicker;
	protected AccountPicker					accountPicker;
	protected Entry							descriptionEntry;
	protected ForeignAmountEntryBox			amountEntryBox;

	private ReimbursableExpensesTransaction	existing	= null;

	/**
	 * Construct the Window. Uses the table from the glade file extensively.
	 * Takes a UnitOfWork for itself when ready and then returns.
	 */
	public ReimbursableExpensesEditorWindow() {
		super("reimbursable", "share/ReimbursableExpensesEditorWindow.glade");

		datePicker = new DatePicker();

		table = (Table) gladeParser.getWidget("general_table");
		table.attach(datePicker, 1, 2, 1, 2);

		descriptionEntry = new Entry();
		table.attach(descriptionEntry, 1, 2, 2, 3);

		accountPicker = new AccountPicker(store);

		table.attach(accountPicker, 1, 2, 3, 4);

		person_WorkerPicker = new WorkerPicker(store, Employee.class);
		table.attach(person_WorkerPicker, 1, 2, 0, 1);

		amountEntryBox = new ForeignAmountEntryBox(store);
		table.attach(amountEntryBox, 1, 2, 4, 5);

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
		System.out.println("Notice: deleteHook() overriden to call Master.shutdown()");
		Master.shutdown();
		return false;
	}

	protected void ok() {
		Worker person = person_WorkerPicker.getWorker();

		if (person == null) {
			ModalDialog dialog = new ModalDialog("Select someone!",
				"You need to select the person you're trying to pay first.", MessageType.WARNING);
			dialog.run();
			person_WorkerPicker.grabFocus();
			return;
		}

		if (descriptionEntry.getText().equals("")) {
			ModalDialog dialog = new ModalDialog(
				"Enter a description!",
				"It's really a good idea for each Transaction to have an appropriate description."
					+ " Try to be a bit more specific than '<i>Expenses reimbursable to Joe Smith</i>' as that won't facilitate identifying this Transaction in future searches and reports."
					+ " Perhaps '<i>Taxi from CDG to Paris Hotel</i>' instead.", MessageType.WARNING);
			dialog.run();
			descriptionEntry.grabFocus();
			return;
		}

		if (accountPicker.getAccount() == null) {
			ModalDialog dialog = new ModalDialog("Select an Account!",
				"You need to select the account to which these expenses apply.", MessageType.WARNING);
			dialog.run();
			accountPicker.grabFocus();
			return;
		}

		try {
			Ledger expensesPayable = person.getExpensesPayable();

			ReimbursableExpensesTransaction t;

			if (existing == null) {
				t = new ReimbursableExpensesTransaction();

				t.setWorker(person);
				t.setDate(datePicker.getDate());
				t.setDescription(descriptionEntry.getText());

				ForeignAmount fa = amountEntryBox.getForeignAmount();

				Debit left = new Debit(fa, accountPicker.getLedger());
				t.addEntry(left);

				Credit right = new Credit(new Amount(fa.getValue()), expensesPayable);
				t.addEntry(right);

				PostTransactionCommand ptc = new PostTransactionCommand(t);
				ptc.execute(store);
			} else {
				throw new UnsupportedOperationException();
			}

			store.commit();
			super.ok();
		} catch (CommandNotReadyException cnre) {
			Debug.print("events", "Command not ready: " + cnre.getMessage());
			ModalDialog dialog = new ModalDialog("Command Not Ready!", cnre.getMessage(), MessageType.ERROR);
			dialog.run();

			/*
			 * Leave the Window open so user can fix, as opposed to calling
			 * cancel()
			 */
		}

		// final ForeignAmount fa = amountEntryBox.getForeignAmount();
		// out.print("Amount:\t\t" + fa.getCurrency().getSymbol() +
		// fa.toString() + " " + fa.getCurrency().getCode());
		//
		// final Books root = store.getBooks();
		// final Currency home = root.getHomeCurrency();
		//
		// if (fa.getCurrency() != home) {
		// out.println(" [" + fa.getRate() + " -> " + home.getSymbol() +
		// fa.getValue() + " " + home.getCode() + "]");
		// } else {
		// out.println();
		// }
	}
}
