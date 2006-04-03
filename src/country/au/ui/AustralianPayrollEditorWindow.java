/*
 * AustralianPayrollEditorWindow.java
 * 
 * See LICENCE file for usage and redistribution terms
 * Copyright (c) 2006 Operational Dynamics
 */
package country.au.ui;

import generic.util.Debug;
import generic.util.DebugException;

import java.util.List;

import org.gnu.gtk.Entry;
import org.gnu.gtk.Gtk;
import org.gnu.gtk.Label;
import org.gnu.gtk.event.ComboBoxEvent;
import org.gnu.gtk.event.ComboBoxListener;
import org.gnu.gtk.event.EntryEvent;
import org.gnu.gtk.event.EntryListener;
import org.gnu.gtk.event.FocusEvent;
import org.gnu.gtk.event.FocusListener;

import accounts.client.ObjectiveAccounts;
import accounts.domain.Amount;
import accounts.domain.Credit;
import accounts.domain.Datestamp;
import accounts.domain.Debit;
import accounts.domain.Employee;
import accounts.domain.IdentifierGroup;
import accounts.domain.Ledger;
import accounts.domain.PayrollTransaction;
import accounts.persistence.UnitOfWork;
import accounts.services.CommandNotReadyException;
import accounts.services.NotFoundException;
import accounts.services.PostTransactionCommand;
import accounts.services.SpecificLedgerFinder;
import accounts.ui.Align;
import accounts.ui.DatePicker;
import accounts.ui.EditorWindow;
import accounts.ui.IdentifierSelector;
import accounts.ui.TwoColumnTable;
import country.au.domain.AustralianPayrollTaxIdentifier;
import country.au.services.AustralianPayrollTaxCalculator;

/**
 * Enter the salary or paycheck received by an Employee; work out the PAYG
 * withholding due, and then on ok record a PayrollTransaction.
 * 
 * @author Andrew Cowie
 */
public class AustralianPayrollEditorWindow extends EditorWindow
{
	private transient String				employeeNameField;

	/*
	 * Pointers to the Amounts we are representing so we can avoid double tap
	 * loops. It might be nice not to need these references, but then, they're
	 * only references so it doesn't matter!
	 */
	private Amount							salary		= null;
	private Amount							withholding	= null;
	private Amount							paycheck	= null;

	private AustralianPayrollTaxCalculator	calc;

	private IdentifierSelector				payg_IdentifierSelector;
	private DatePicker						endDate_Picker;
	private Entry							salaryAmount_Entry;
	private Label							withholdingAmount_Entry;
	private Entry							paycheckAmount_Entry;

	/**
	 * The last case used, either salary or paycheck, so we can recalculate
	 * appropriately if the tax identifier changes
	 */
	private transient Entry					last		= null;

	/**
	 * Construct the Window. Inherits an initialized UnitOfWork from
	 * EditorWindow. TODO differentiate between a new set of data, and editing
	 * old data. New constrcutor(PayrollTransaction)? Probably.
	 */
	public AustralianPayrollEditorWindow() {
		super("Enter payroll details");

		/*
		 * Pick employee. Mockup; replace with EmployeePicker! TODO
		 */

		Label employeeName_Label = new Label("Pick employee:");
		employeeName_Label.setAlignment(0.0, 0.5);
		top.packStart(employeeName_Label, false, false, 3);

		Entry employeeName_Entry = new Entry();
		employeeName_Entry.setText("Andrew Cowie");
		top.packStart(employeeName_Entry, false, false, 3);

		/*
		 * Pick withholding type Identifier
		 */

		Label withholdingType_Label = new Label("PAYG withholding type:");
		withholdingType_Label.setAlignment(0.0, 0.5);
		top.packStart(withholdingType_Label, false, false, 3);

		// this will be buggy the moment there is more than one! FIXME
		List found = ObjectiveAccounts.store.queryByExample(IdentifierGroup.class);
		if (found.size() != 1) {
			throw new Error("Dude, you need to fix the code to deal with reality");
		}
		IdentifierGroup grp = (IdentifierGroup) found.get(0); // FIXME

		payg_IdentifierSelector = new IdentifierSelector(grp);
		top.packStart(payg_IdentifierSelector, false, false, 0);

		/*
		 * From here we have two columns, one for the labels, and one for the
		 * entry boxes. We use a GtkTable for the layout. It's a pain to use, so
		 * we use a little helper class:
		 */

		TwoColumnTable table = new TwoColumnTable(1);
		final Align LEFT = Align.LEFT;
		final Align RIGHT = Align.RIGHT;

		/*
		 * Date picker
		 */

		Label endDate_Label = new Label("Ending at date:");
		endDate_Label.setAlignment(1.0, 0.5);

		table.attach(endDate_Label, LEFT);

		endDate_Picker = new DatePicker();
		table.attach(endDate_Picker, RIGHT);

		/*
		 * The salary entry
		 */

		Label salaryAmount_Label = new Label("Salary:");
		salaryAmount_Label.setAlignment(1.0, 0.5);
		table.attach(salaryAmount_Label, LEFT);

		salaryAmount_Entry = new Entry();
		table.attach(salaryAmount_Entry, RIGHT);

		/*
		 * The withholding entry. This one you can't set directly; it's
		 * calculated.
		 */
		Label withholdingAmount_Label = new Label("Withholding:");
		withholdingAmount_Label.setAlignment(1.0, 0.5);
		table.attach(withholdingAmount_Label, LEFT);

		// withholdingAmount_Entry = new Entry();
		// withholdingAmount_Entry.setEditable(false);
		// withholdingAmount_Entry.setSensitive(false);
		withholdingAmount_Entry = new Label("");
		withholdingAmount_Entry.setAlignment(0.0, 0.5);
		withholdingAmount_Entry.setPadding(5, 4);
		table.attach(withholdingAmount_Entry, RIGHT);

		/*
		 * The paycheck entry
		 */
		Label paycheckAmount_Label = new Label("Paycheck:");
		paycheckAmount_Label.setAlignment(1.0, 0.5);
		table.attach(paycheckAmount_Label, LEFT);

		paycheckAmount_Entry = new Entry();
		table.attach(paycheckAmount_Entry, RIGHT);

		/*
		 * And now put the table into the top Box.
		 */
		top.packStart(table, true, true, 0);

		/*
		 * Now attach the listeners: A new Calculator if the Identifier is
		 * changed; and run the Calculator if an Amount is changed in either
		 * salary or paycheck entry fields.
		 */

		payg_IdentifierSelector.addListener(new ComboBoxListener() {
			public void comboBoxEvent(ComboBoxEvent event) {
				AustralianPayrollTaxIdentifier payg = (AustralianPayrollTaxIdentifier) payg_IdentifierSelector.getSelection();
				Datestamp date = endDate_Picker.getDate();
				try {
					calc = new AustralianPayrollTaxCalculator(payg, date);

					/*
					 * Now recalculate given the existing values...
					 */

					if (last == null) {
						return;
					} else if (last == salaryAmount_Entry) {
						Debug.print("listeners", "Recalculating given salary");

						calc.setSalary(salary);
						calc.calculateGivenSalary();

						withholding = calc.getWithhold();
						withholdingAmount_Entry.setText(withholding.getValue());
						paycheck = calc.getPaycheck();
						paycheckAmount_Entry.setText(paycheck.getValue());

					} else if (last == paycheckAmount_Entry) {
						Debug.print("listeners", "Recalculating given paycheck");

						calc.setPaycheck(paycheck);
						calc.calculateGivenPayable();

						salary = calc.getSalary();
						salaryAmount_Entry.setText(salary.getValue());
						withholding = calc.getWithhold();
						withholdingAmount_Entry.setText(withholding.getValue());
					}
				} catch (NotFoundException nfe) {
					// FIXME to dialog
					throw new DebugException("Can't find tax data for identifier " + payg);
				}
			}
		});

		salaryAmount_Entry.addListener(new EntryListener() {
			public void entryEvent(EntryEvent event) {
				if (event.getType() == EntryEvent.Type.CHANGED) {
					/*
					 * Get the new salary amount and validate it
					 */

					final String text = salaryAmount_Entry.getText();
					if (!salaryAmount_Entry.hasFocus()) {
						return;
					}
					if (text.equals("")) {
						return;
					}

					try {
						salary.setValue(text);
					} catch (NumberFormatException nfe) {
						return;
					}

					Debug.print("listeners", me + " salary Entry changed, " + salary.toString());

					calc.setSalary(salary);

					/*
					 * Do the work
					 */
					calc.calculateGivenSalary();

					/*
					 * And display the results.
					 */
					withholding = calc.getWithhold();
					withholdingAmount_Entry.setText(withholding.getValue());
					paycheck = calc.getPaycheck();
					paycheckAmount_Entry.setText(paycheck.getValue());

					last = salaryAmount_Entry;
				}

				if (event.getType() == EntryEvent.Type.ACTIVATE) {
					final String text = salary.getValue();
					salaryAmount_Entry.setText(text);
					salaryAmount_Entry.setCursorPosition(text.length());
				}
			}
		});

		/*
		 * This fixes the situation where the user has enetered "263" and then
		 * leaves - the other Entries are set with Amount.getValue()'s String,
		 * but this one is not.
		 */
		salaryAmount_Entry.addListener(new FocusListener() {
			public boolean focusEvent(FocusEvent event) {
				if (event.getType() == FocusEvent.Type.FOCUS_OUT) {
					salaryAmount_Entry.setText(salary.getValue());
					salaryAmount_Entry.selectRegion(0, 0);
				}
				return false;
			};
		});

		/*
		 * Now paycheck... mirror image of salary case above.
		 */

		paycheckAmount_Entry.addListener(new EntryListener() {
			public void entryEvent(EntryEvent event) {
				if (event.getType() == EntryEvent.Type.CHANGED) {
					/*
					 * Get the new paycheck amount and validate it
					 */
					final String text = paycheckAmount_Entry.getText();
					if (!paycheckAmount_Entry.hasFocus()) {
						return;
					}
					if (text.equals("")) {
						return;
					}

					try {
						paycheck.setValue(text);
					} catch (NumberFormatException nfe) {
						return;
					}

					Debug.print("listeners", me + " paycheck Entry changed, " + paycheck.toString());

					calc.setPaycheck(paycheck);

					/*
					 * Do the work
					 */
					calc.calculateGivenPayable();

					/*
					 * And display the results.
					 */

					salary = calc.getSalary();
					salaryAmount_Entry.setText(salary.getValue());
					withholding = calc.getWithhold();
					withholdingAmount_Entry.setText(withholding.getValue());

					last = paycheckAmount_Entry;
				}

				if (event.getType() == EntryEvent.Type.ACTIVATE) {
					final String text = paycheck.getValue();
					paycheckAmount_Entry.setText(text);
					paycheckAmount_Entry.setCursorPosition(text.length());
				}
			}
		});

		paycheckAmount_Entry.addListener(new FocusListener() {
			public boolean focusEvent(FocusEvent event) {
				if (event.getType() == FocusEvent.Type.FOCUS_OUT) {
					paycheckAmount_Entry.setText(paycheck.getValue());
					paycheckAmount_Entry.selectRegion(0, 0);
				}
				return false;
			};
		});

		/*
		 * And now set a useful initial state:
		 */
		payg_IdentifierSelector.setActive(0);

		salary = new Amount(0);
		withholding = new Amount(0);
		paycheck = new Amount(0);

		// somewhat overkill, but this adapts should formatting change...
		salaryAmount_Entry.setText(salary.getValue());
		withholdingAmount_Entry.setText(withholding.getValue());
		paycheckAmount_Entry.setText(paycheck.getValue());

		present();
	}

	/*
	 * Mimic what real UI would have done
	 */
	private void mockupFields() {
		employeeNameField = "Andrew Cowie";
	}

	protected void ok() {
		mockupFields();

		/*
		 * Retrieve the actual Employee from the database.
		 */

		// Employee proto = new Employee();
		// proto.setName(employeeNameField);
		//
		// List result = ObjectiveAccounts.store.queryByExample(proto);
		// if (result.size() != 1) {
		// throw new NotFoundException("Employee " + employeeNameField + "
		// not found in database");
		// }
		// Employee storedEmployee = (Employee) result.get(0);
		Employee storedEmployee = new Employee();
		storedEmployee.setName(employeeNameField); // FIXME

		/*
		 * Get the requisite Ledgers
		 */

		try {
			SpecificLedgerFinder f = new SpecificLedgerFinder();

			// TODO this is standardized and needs to be selected (automatically
			// and/or with user guidance) and made available somewhere. On
			// books? Hm. Can't be looking it up by text string, though. That's
			// rediculous. A more use case specific Finder? Perhaps.

			f.setAccountTitle("ANZ");
			f.setLedgerName("Current");
			Ledger bankAccount = f.getLedger();

			f.setAccountTitle("Employment");
			f.setLedgerName("Salaries");
			Ledger salariesExpense = f.getLedger();

			f.setAccountTitle("PAYG");
			f.setLedgerName("Collected");
			Ledger paygOwing = f.getLedger();

			/*
			 * Form the Transaction
			 */
			PayrollTransaction t = new PayrollTransaction(storedEmployee,
				(AustralianPayrollTaxIdentifier) payg_IdentifierSelector.getSelection());
			t.setDate(endDate_Picker.getDate());

			t.addEntry(new Credit(calc.getPaycheck(), bankAccount));
			t.addEntry(new Credit(calc.getWithhold(), paygOwing));
			t.addEntry(new Debit(calc.getSalary(), salariesExpense));

			uow = new UnitOfWork(me);

			PostTransactionCommand ptc = new PostTransactionCommand(t);
			ptc.execute(uow);

			uow.commit();
			super.ok();
		} catch (NotFoundException nfe) {
			Debug.print("events", "Can't find Ledger " + nfe.getMessage());
		} catch (CommandNotReadyException cnre) {
			Debug.print("events", "Command not ready " + cnre.getMessage());
			uow.cancel();
			cancel();
		}
	}

	public boolean deleteHook() {
		// hide & destroy
		super.deleteHook();
		// quit
		System.out.println("Notice: deleteHook() overriden to call Gtk.mainQuit()");
		Gtk.mainQuit();
		return false;
	}
}
