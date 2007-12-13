/*
 * AustralianPayrollEditorWindow.java
 * 
 * See LICENCE file for usage and redistribution terms
 * Copyright (c) 2006 Operational Dynamics
 */
package country.au.ui;

import generic.ui.Align;
import generic.ui.ChangeListener;
import generic.ui.EditorWindow;
import generic.ui.ModalDialog;
import generic.ui.TwoColumnTable;
import generic.util.Debug;

import java.util.List;

import org.gnome.gtk.HSeparator;
import org.gnome.gtk.Label;
import org.gnome.gtk.MessageType;
import org.gnome.gtk.VBox;
import org.gnome.gtk.Widget;

import accounts.domain.Amount;
import accounts.domain.Credit;
import accounts.domain.Datestamp;
import accounts.domain.Debit;
import accounts.domain.Employee;
import accounts.domain.Entry;
import accounts.domain.IdentifierGroup;
import accounts.domain.Ledger;
import accounts.domain.PayrollTransaction;
import accounts.services.CommandNotReadyException;
import accounts.services.NotFoundException;
import accounts.services.PostTransactionCommand;
import accounts.services.RangeCalculator;
import accounts.services.SpecificLedgerFinder;
import accounts.services.UpdateTransactionCommand;
import accounts.ui.AmountDisplay;
import accounts.ui.AmountEntry;
import accounts.ui.DatePicker;
import accounts.ui.IdentifierSelector;
import accounts.ui.RangePicker;
import accounts.ui.WorkerPicker;
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
    private transient Employee employee = null;

    /*
     * Pointers to the Amounts we are representing so we can avoid double tap
     * loops. It might be nice not to need these references, but then, they're
     * only references so it doesn't matter!
     */
    private Amount salary = null;

    private Amount withholding = null;

    private Amount paycheck = null;

    private AustralianPayrollTaxCalculator calc;

    private WorkerPicker employee_WorkerPicker;

    private DatePicker payDate_Picker;

    private IdentifierSelector payg_IdentifierSelector;

    private RangePicker payPeriod_RangePicker;

    private AmountEntry salary_AmountEntry;

    private AmountDisplay withholding_AmountDisplay;

    private AmountEntry paycheck_AmountEntry;

    /**
     * The last case used, either salary or paycheck, so we can recalculate
     * appropriately if the tax identifier changes
     */
    private Widget last = null;

    private PayrollTransaction existing = null;

    /**
     * Construct a Window and instantiate all the necessary widgets for a new
     * PayrollTransaction.
     */
    public AustralianPayrollEditorWindow() {
        this(0);
    }

    /**
     * Construct a Window and instantiate all the necessary widgets.
     * 
     * @param tID
     *            the object ID of a PayrollTransaction containing an
     *            Australian PAYG transaction you want to edit
     */
    /*
     * The business with ID 0 is a bit of subtrefuge. Obviously the no-arg
     * constructor uses 0, but we don't want to be public about that as in
     * general use ID 0 is meaningless and sign of an error. Here it means
     * new.
     */
    public AustralianPayrollEditorWindow(long tID) {
        super();

        PayrollTransaction t;
        if (tID == 0) {
            t = null;
        } else {
            t = (PayrollTransaction) store.fetchByID(tID);
        }

        final Label title_Label = new Label("<big><b>Pay an (Australian) Employee</b></big>");
        title_Label.setUseMarkup(true);
        title_Label.setAlignment(0.0f, 0.5f);

        top.packStart(title_Label, false, false, 3);

        final Align LEFT = Align.LEFT;
        final Align RIGHT = Align.RIGHT;
        final Align BOTH = Align.CENTER;

        /*
         * From here we have two columns, one for the labels, and one for the
         * entry boxes. We use a GtkTable for the layout. It's a pain to use,
         * so we use a little helper class:
         */
        final TwoColumnTable table = new TwoColumnTable(1);

        /*
         * Pick employee.
         */

        final Label employeeName_Label = new Label("Pay employee:");
        employeeName_Label.setAlignment(1.0f, 0.5f);
        table.attach(employeeName_Label, LEFT);

        employee_WorkerPicker = new WorkerPicker(store, Employee.class);
        table.attach(employee_WorkerPicker, RIGHT);

        final Label payDate_Label = new Label("On date:");
        payDate_Label.setAlignment(1.0f, 0.5f);
        table.attach(payDate_Label, LEFT);

        payDate_Picker = new DatePicker();
        table.attach(payDate_Picker, RIGHT);

        /*
         * Pick withholding type Identifier
         */

        final Label payg_Label = new Label("PAYG withholding type:");
        payg_Label.setAlignment(1.0f, 0.5f);
        table.attach(payg_Label, LEFT);

        // FIXME this will be buggy the moment there is more than one
        // IdentifierGroup!
        List found = store.queryByExample(IdentifierGroup.class);
        if (found.size() != 1) {
            throw new Error("Dude, you need to fix the code to deal with reality");
        }
        IdentifierGroup grp = (IdentifierGroup) found.get(0); // FIXME

        payg_IdentifierSelector = new IdentifierSelector(grp);
        table.attach(payg_IdentifierSelector, BOTH);

        /*
         * Date pickers
         */

        final VBox spacer1 = new VBox(false, 0);
        final HSeparator sep1 = new HSeparator();
        spacer1.packStart(sep1, false, false, 5);
        table.attach(spacer1, BOTH);

        final Label for_Label = new Label("<b>For work:</b>");
        for_Label.setUseMarkup(true);
        for_Label.setAlignment(0.0f, 0.5f);
        table.attach(for_Label, LEFT);

        payPeriod_RangePicker = new RangePicker();
        table.attach(payPeriod_RangePicker, BOTH);

        final VBox spacer2 = new VBox(false, 0);
        final HSeparator sep2 = new HSeparator();
        spacer2.packStart(sep2, false, false, 5);
        table.attach(spacer2, BOTH);

        /*
         * The salary entry
         */

        final Label salary_Label = new Label("Salary:");
        salary_Label.setAlignment(1.0f, 0.5f);
        table.attach(salary_Label, LEFT);

        salary_AmountEntry = new AmountEntry();
        table.attach(salary_AmountEntry, RIGHT);

        /*
         * The widget to display the withholding Amount. This one you can't
         * set directly; it's calculated.
         */
        final Label withholding_Label = new Label("Withholding:");
        withholding_Label.setAlignment(1.0f, 0.5f);
        table.attach(withholding_Label, LEFT);

        withholding_AmountDisplay = new AmountDisplay();
        table.attach(withholding_AmountDisplay, RIGHT);

        /*
         * The paycheck entry
         */
        final Label paycheck_Label = new Label("Paycheck:");
        paycheck_Label.setAlignment(1.0f, 0.5f);
        table.attach(paycheck_Label, LEFT);

        paycheck_AmountEntry = new AmountEntry();
        table.attach(paycheck_AmountEntry, RIGHT);

        /*
         * And now put the table into the top Box.
         */
        top.packStart(table, true, true, 0);

        /*
         * Hook up the listener to the PAYG IdentifierSelector. This one is
         * first because when the initial state is set, it needs to be hooked
         * up so that the appropriate calculator is instantiated.
         */

        payg_IdentifierSelector.addListener(new ComboBoxListener() {
            public void comboBoxEvent(ComboBoxEvent event) {
                if (event.getType() == ComboBoxEvent.Type.CHANGED) {
                    AustralianPayrollTaxIdentifier payg = (AustralianPayrollTaxIdentifier) payg_IdentifierSelector.getSelection();
                    Datestamp date = payPeriod_RangePicker.getStartDate();
                    try {
                        calc = new AustralianPayrollTaxCalculator(store, payg, date);
                        calc.setWeeks(payPeriod_RangePicker.getRangeCalculator().calculateWeeks());

                        // make sure new calc has the appropriate references
                        calc.setSalary(salary);
                        calc.setWithhold(withholding);
                        calc.setPaycheck(paycheck);

                        /*
                         * Now recalculate given the existing values...
                         */

                        if (last == null) {
                            return;
                        } else if (last == salary_AmountEntry) {
                            Debug.print("listeners", "Recalculating given salary");

                            calc.calculateGivenSalary();

                            withholding = calc.getWithhold();
                            withholding_AmountDisplay.setAmount(withholding);
                            paycheck = calc.getPaycheck();
                            paycheck_AmountEntry.setAmount(paycheck);

                        } else if (last == paycheck_AmountEntry) {
                            Debug.print("listeners", "Recalculating given paycheck");

                            calc.calculateGivenPayable();

                            salary = calc.getSalary();
                            salary_AmountEntry.setAmount(salary);
                            withholding = calc.getWithhold();
                            withholding_AmountDisplay.setAmount(withholding);
                        }
                    } catch (NotFoundException nfe) {
                        ModalDialog dialog = new ModalDialog(
                                window,
                                "Not found",
                                "Can't find tax data for identifier <b>"
                                        + payg
                                        + "</b> effective <b>"
                                        + calc.getAsAtDate()
                                        + "</b>. That's probably a bug (tax tables tend to be all or nothing) but please try another one.",
                                MessageType.ERROR);
                        dialog.run();
                    }
                }
            }
        });

        /*
         * Set a useful initial state. Again, differentiate between the new
         * Transaction and edit Transaction cases. Specifically, this is
         * before the listeners are attached to ensure that setting the
         * objects doesn't result in double-tap bugs.
         */

        if (t == null) {
            salary = new Amount(0);
            withholding = new Amount(0);
            paycheck = new Amount(0);

            payg_IdentifierSelector.setActive(0);

            setTitle("Enter payroll details");

            /*
             * start with selecting a person to pay.
             */
            employee_WorkerPicker.grabFocus();
        } else {
            salary = t.getSalaryEntry().getAmount();
            withholding = t.getWithholdingEntry().getAmount();
            paycheck = t.getPaycheckEntry().getAmount();

            payDate_Picker.setDate(t.getDate());
            payPeriod_RangePicker.setStartDate(t.getFromDate());
            payPeriod_RangePicker.setEndDate(t.getEndDate());

            employee_WorkerPicker.setWorker(t.getEmployee());
            payg_IdentifierSelector.setIdentifier(t.getTaxIdentifier());

            setTitle("Edit Transaction " + t.getDescription());

            /*
             * As we're editing, the likely thing is changing the amount, so
             * set focus there.
             */
            salary_AmountEntry.grabFocus();

            /*
             * And keep a reference to the passed Transaction
             */
            existing = t;
        }
        salary_AmountEntry.setAmount(salary);
        withholding_AmountDisplay.setAmount(withholding);
        paycheck_AmountEntry.setAmount(paycheck);

        /*
         * Now hook up the listeners: A new Calculator if the Identifier is
         * changed; and run the Calculator if an Amount is changed in either
         * salary or paycheck entry fields.
         */

        salary_AmountEntry.addListener(new ChangeListener() {
            public void userChangedData() {

                Debug.print("listeners", me + " in salary_AmountEntry's changed(), salary now "
                        + salary.toString());

                calc.setSalary(salary);

                /*
                 * Do the work
                 */
                calc.calculateGivenSalary();

                /*
                 * And display the results.
                 */
                withholding = calc.getWithhold();
                withholding_AmountDisplay.setAmount(withholding);
                paycheck = calc.getPaycheck();
                paycheck_AmountEntry.setAmount(paycheck);

                last = salary_AmountEntry;
            }
        });

        /*
         * Now paycheck... mirror image of salary case above.
         */
        paycheck_AmountEntry.addListener(new ChangeListener() {
            public void userChangedData() {

                Debug.print("listeners", me + " in paycheck_AmountEntry's changed(), paycheck now "
                        + paycheck.toString());

                calc.setPaycheck(paycheck);

                /*
                 * Do the work
                 */
                calc.calculateGivenPayable();

                /*
                 * And display the results.
                 */

                salary = calc.getSalary();
                salary_AmountEntry.setAmount(salary);
                withholding = calc.getWithhold();
                withholding_AmountDisplay.setAmount(withholding);

                last = paycheck_AmountEntry;
            }
        });

        payPeriod_RangePicker.addListener(new ChangeListener() {
            public void userChangedData() {
                RangeCalculator period = payPeriod_RangePicker.getRangeCalculator();
                float num = period.calculateWeeks();
                calc.setWeeks(num);

                /*
                 * Now recalculate given the existing values...
                 */

                if (last == null) {
                    return;
                } else if (last == salary_AmountEntry) {
                    Debug.print("listeners", "Recalculating given salary");

                    calc.calculateGivenSalary();

                    withholding_AmountDisplay.setAmount(withholding);
                    paycheck_AmountEntry.setAmount(paycheck);

                } else if (last == paycheck_AmountEntry) {
                    Debug.print("listeners", "Recalculating given paycheck");

                    calc.calculateGivenPayable();

                    salary_AmountEntry.setAmount(salary);
                    withholding_AmountDisplay.setAmount(withholding);
                }
            }
        });

        /*
         * TODO: if we implement PrimaryTransaction then use its value to
         * point to which Entry is last:
         */
        last = salary_AmountEntry;
    }

    protected void ok() {
        employee = (Employee) employee_WorkerPicker.getWorker();

        /*
         * Basic data guards.
         */

        if (employee == null) {
            ModalDialog dialog = new ModalDialog(window, "Select an employee!",
                    "You need to select the person you're trying to pay first.", MessageType.WARNING);
            dialog.run();
            return;
        }

        if (salary.getNumber() == 0) {
            ModalDialog dialog = new ModalDialog(window, "Enter some numbers!",
                    "Not much point in trying to commit a paycheck for 0.00, is there?",
                    MessageType.WARNING);
            dialog.run();
            /*
             * No need to throw CommandNotReadyException; while the state of
             * things is indeed not suitable, at this point we can still trap
             * it as a business logic problem, rather than a validation
             * failure.
             */
            return;
        }

        /*
         * Get the requisite Ledgers
         */

        try {
            SpecificLedgerFinder f = new SpecificLedgerFinder();

            // TODO this is standardized and needs to be selected
            // (automatically
            // and/or with user guidance) and made available somewhere. On
            // books? Hm. Can't be looking it up by text string, though.
            // That's
            // rediculous. A more use case specific Finder? Perhaps.

            f.setAccountTitle("ANZ");
            f.setLedgerName("Current");
            f.query(store);
            Ledger bankAccount = f.getLedger();

            f.setAccountTitle("Employment");
            f.setLedgerName("Salaries");
            f.query(store);
            Ledger salariesExpense = f.getLedger();

            f.setAccountTitle("PAYG");
            f.setLedgerName("Collected");
            f.query(store);
            Ledger paygOwing = f.getLedger();

            if (existing == null) {
                /*
                 * Form the Transaction
                 */
                PayrollTransaction t = new PayrollTransaction(employee,
                        (AustralianPayrollTaxIdentifier) payg_IdentifierSelector.getSelection());
                t.setDate(payDate_Picker.getDate());

                t.setFromDate(payPeriod_RangePicker.getStartDate());
                t.setEndDate(payPeriod_RangePicker.getEndDate());

                Entry e;
                e = new Credit(calc.getPaycheck(), bankAccount);
                t.addEntry(e);
                t.setPaycheckEntry(e);

                e = new Credit(calc.getWithhold(), paygOwing);
                t.addEntry(e);
                t.setWithholdingEntry(e);

                e = new Debit(calc.getSalary(), salariesExpense);
                t.addEntry(e);
                t.setSalaryEntry(e);

                PostTransactionCommand ptc = new PostTransactionCommand(t);
                ptc.execute(store);
            } else {
                PayrollTransaction t = existing;

                t.setTaxIdentifier((AustralianPayrollTaxIdentifier) payg_IdentifierSelector.getSelection());
                /*
                 * The amounts in the Entries were used directly.
                 */
                UpdateTransactionCommand utc = new UpdateTransactionCommand(t);
                utc.execute(store);
            }
            store.commit();
            super.ok();
        } catch (NotFoundException nfe) {
            Debug.print("events", "Can't find Ledger " + nfe.getMessage());
        } catch (CommandNotReadyException cnre) {
            Debug.print("events", "Command not ready: " + cnre.getMessage());
            ModalDialog dialog = new ModalDialog(window, "Command Not Ready!", cnre.getMessage(),
                    MessageType.ERROR);
            dialog.run();

            /*
             * Leave the Window open so user can fix, as opposed to calling
             * cancel()
             */
            present();
        }
    }
}
