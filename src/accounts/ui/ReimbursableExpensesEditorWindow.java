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
 * contacted via http://research.operationaldynamics.com/projects/objective/.
 */
package accounts.ui;

import generic.client.Master;
import generic.ui.EditorWindow;

import java.util.Iterator;
import java.util.Set;

import objective.domain.Amount;
import objective.domain.Credit;
import objective.domain.Debit;
import objective.domain.Employee;
import objective.domain.ForeignAmount;
import objective.domain.Ledger;
import objective.domain.Worker;
import objective.persistence.DataStore;
import objective.services.TransactionOperations;

import org.gnome.gtk.Alignment;
import org.gnome.gtk.Dialog;
import org.gnome.gtk.Entry;
import org.gnome.gtk.ErrorMessageDialog;
import org.gnome.gtk.HBox;
import org.gnome.gtk.Label;
import org.gnome.gtk.SizeGroup;
import org.gnome.gtk.SizeGroupMode;
import org.gnome.gtk.Table;
import org.gnome.gtk.WarningMessageDialog;

import accounts.domain.ReimbursableExpensesTransaction;
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

    /**
     * Construct the Window.
     */
    public ReimbursableExpensesEditorWindow(DataStore data, long id) {
        super();
        final SizeGroup group;
        Label label;
        HBox box;

        window.setTitle("Enter your expenses");

        group = new SizeGroup(SizeGroupMode.HORIZONTAL);

        /*
         * Worker
         */

        box = new HBox(false, 6);

        label = new Label("Expenses reimbursable to:");
        label.setAlignment(Alignment.RIGHT, Alignment.CENTER);
        box.packStart(label, false, false, 0);
        group.add(label);

        person_WorkerPicker = new WorkerPicker(data, Employee.class);
        box.packStart(datePicker, false, false, 0);

        top.packStart(box, false, false, 0);

        /*
         * Date
         */

        box = new HBox(false, 6);

        label = new Label("To date:");
        label.setAlignment(Alignment.RIGHT, Alignment.CENTER);
        box.packStart(label, false, false, 0);
        group.add(label);

        datePicker = new DatePicker();
        box.packStart(datePicker, false, false, 0);

        top.packStart(box, false, false, 0);

        /*
         * Description
         */

        box = new HBox(false, 6);

        label = new Label("Description:");
        label.setAlignment(Alignment.RIGHT, Alignment.CENTER);
        box.packStart(label, false, false, 0);
        group.add(label);

        descriptionEntry = new Entry();
        box.packStart(descriptionEntry, false, false, 0);

        top.packStart(box, false, false, 0);

        /*
         * Account
         */

        box = new HBox(false, 6);

        label = new Label("Account / Ledger:");
        label.setAlignment(Alignment.RIGHT, Alignment.CENTER);
        box.packStart(label, false, false, 0);
        group.add(label);

        accountPicker = new AccountPicker(data);
        box.packStart(accountPicker, false, false, 0);

        top.packStart(box, false, false, 0);

        /*
         * Amount
         */

        box = new HBox(false, 6);

        label = new Label("Amount:");
        label.setAlignment(Alignment.RIGHT, Alignment.CENTER);
        box.packStart(label, false, false, 0);
        group.add(label);

        amountEntryBox = new ForeignAmountEntryBox(data);
        box.packStart(amountEntryBox, false, false, 0);

        top.packStart(box, false, false, 0);

        ReimbursableExpensesTransaction t;
        if (id == 0) {
            t = new ReimbursableExpensesTransaction(0);
        } else {
            TransactionOperations services;

            services = new TransactionOperations(data);
            t = (ReimbursableExpensesTransaction) services.findTransaction(id);
            datePicker.setDate(t.getDate());
            person_WorkerPicker.setWorker(t.getWorker());
            objective.domain.Entry[] entries = services.findEntries(t);

            for (objective.domain.Entry e : entries) {
                Ledger l = e.getParentLedger();
                if (l == t.getWorker().getExpensesPayable()) {
                    // FIXME
                } else {
                    accountPicker.setAccount(l.getParentAccount());
                    accountPicker.setLedger(l);
                    amountEntryBox.setAmount(e);
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
            dialog = new WarningMessageDialog(this, "Select someone!",
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
                            objective.domain.Entry e = (objective.domain.Entry) iter.next();
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
