/*
 * ObjectiveAccounts, accounting for small professional services firms.
 *
 * Copyright © 2005-2011 Operational Dynamics Consulting, Pty Ltd
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
 * contacted through http://research.operationaldynamics.com/projects/objective/.
 */
package accounts.ui;

import generic.client.Master;
import generic.ui.EditorWindow;
import generic.util.Debug;

import java.util.Iterator;
import java.util.Set;

import org.gnome.gtk.Dialog;
import org.gnome.gtk.Entry;
import org.gnome.gtk.ErrorMessageDialog;
import org.gnome.gtk.Table;
import org.gnome.gtk.WarningMessageDialog;

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
import accounts.services.UpdateTransactionCommand;

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
    protected Table table;

    protected final WorkerPicker person_WorkerPicker;

    protected final DatePicker datePicker;

    protected final AccountPicker accountPicker;

    protected final Entry descriptionEntry;

    protected final ForeignAmountEntryBox amountEntryBox;

    private ReimbursableExpensesTransaction existing = null;

    private ReimbursableExpensesEditorWindow self;

    /**
     * Construct a window to create a new ReimbursableExpensesTransaction.
     * 
     */
    public ReimbursableExpensesEditorWindow() {
        this(0);
    }

    /**
     * Construct the Window. Uses the table from the glade file extensively.
     */
    public ReimbursableExpensesEditorWindow(long id) {
        super("reimbursable", "share/ReimbursableExpensesEditorWindow.glade");

        self = this;

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

        ReimbursableExpensesTransaction t;
        if (id == 0) {
            t = new ReimbursableExpensesTransaction();
        } else {
            t = (ReimbursableExpensesTransaction) store.fetchByID(id);
            datePicker.setDate(t.getDate());
            person_WorkerPicker.setWorker(t.getWorker());
            Set entries = t.getEntries();
            Iterator iter = entries.iterator();
            while (iter.hasNext()) {
                accounts.domain.Entry e = (accounts.domain.Entry) iter.next();
                Ledger l = e.getParentLedger();
                if (l == t.getWorker().getExpensesPayable()) {
                    // FIXME
                } else {
                    accountPicker.setAccount(l.getParentAccount());
                    accountPicker.setLedger(l);
                    amountEntryBox.setForeignAmount((ForeignAmount) e.getAmount());
                }
            }
            descriptionEntry.setText(t.getDescription());
            existing = t;
        }
    }

    protected void ok() {
        final Worker person = person_WorkerPicker.getWorker();

        if (person == null) {
            Dialog dialog;
            dialog = new WarningMessageDialog(window, "Select someone!",
                    "You need to select the person you're trying to pay first.");
            dialog.run();
            person_WorkerPicker.grabFocus();
            return;
        }

        if (descriptionEntry.getText().equals("")) {
            Dialog dialog;

            dialog = new WarningMessageDialog(
                    window,
                    "Enter a description!",
                    "It's really a good idea for each Transaction to have an appropriate description."
                            + " Try to be a bit more specific than '<i>Expenses reimbursable to Joe Smith</i>' as that won't facilitate identifying this Transaction in future searches and reports."
                            + " Perhaps '<i>Taxi from CDG to Paris Hotel</i>' instead.");
            dialog.run();
            descriptionEntry.grabFocus();
            return;
        }

        if (accountPicker.getAccount() == null) {
            Dialog dialog = new WarningMessageDialog(window, "Select an Account!",
                    "You need to select the account to which these expenses apply.");
            dialog.run();
            accountPicker.grabFocus();
            return;
        }

        /*
         * Guards passed. Fork the processing off in a Thread so the UI
         * doesn't freeze up.
         */

        window.hide();
        Master.ui.showAsWorking(true);

        new Thread() {
            public void run() {
                Debug.print("threads", "Carrying out update");
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
                        t = existing;
                        t.setDescription(descriptionEntry.getText());

                        Set entries = t.getEntries();
                        Iterator iter = entries.iterator();
                        while (iter.hasNext()) {
                            accounts.domain.Entry e = (accounts.domain.Entry) iter.next();
                            Ledger l = e.getParentLedger();
                            ForeignAmount fa = amountEntryBox.getForeignAmount();

                            if (l == t.getWorker().getExpensesPayable()) {
                                // the Credit
                                Amount ha = e.getAmount();
                                ha.setValue(fa);
                            } else {
                                // the Debit
                                e.setAmount(fa);
                                e.setParentLedger(accountPicker.getLedger());
                            }
                        }

                        UpdateTransactionCommand utc = new UpdateTransactionCommand(t);
                        utc.execute(store);
                    }

                    store.commit();

                } catch (final CommandNotReadyException cnre) {
                    Debug.print("events", "Command not ready: " + cnre.getMessage());

                    Dialog dialog = new ErrorMessageDialog(window, "Command Not Ready!",
                            cnre.getMessage());
                    dialog.run();

                    /*
                     * Leave the Window open so user can fix, as opposed to
                     * calling cancel()
                     */
                    window.present();
                }

            }
        }.start();
    }
}
