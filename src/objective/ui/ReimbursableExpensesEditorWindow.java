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
package objective.ui;

import objective.domain.Credit;
import objective.domain.Currency;
import objective.domain.Debit;
import objective.domain.Entry;
import objective.domain.Ledger;
import objective.domain.ReimbursableExpensesPayableAccount;
import objective.domain.ReimbursableTransaction;
import objective.domain.Transaction;
import objective.domain.Worker;
import objective.persistence.DataStore;
import objective.services.TransactionOperations;

import org.gnome.gtk.Alignment;
import org.gnome.gtk.ErrorMessageDialog;
import org.gnome.gtk.HBox;
import org.gnome.gtk.Label;
import org.gnome.gtk.MessageDialog;
import org.gnome.gtk.ResponseType;
import org.gnome.gtk.WarningMessageDialog;

/**
 * Enter or edit expenses incurred by and reimbursable to a Worker.
 * 
 * @author Andrew Cowie
 */
public class ReimbursableExpensesEditorWindow extends TransactionEditorWindow
{
    private final WorkerPicker personPicker;

    private final AccountLedgerPicker accountPicker;

    private final ForeignAmountEntryBox amountEntryBox;

    private final TaxEntryBox taxEntryBox;

    private ReimbursableTransaction existing = null;

    private TransactionOperations services;

    private Entry left, right;

    /**
     * Construct the Window.
     */
    public ReimbursableExpensesEditorWindow(final DataStore data, final ReimbursableTransaction t) {
        super(data, "Reimbursable Expenses");
        Label label;
        HBox box;
        Entry[] entries;
        Worker worker;
        String str;

        services = new TransactionOperations(data);

        window.setTitle("Enter your expenses");

        /*
         * Worker
         */

        box = new HBox(false, 6);

        label = new Label("Expenses reimbursable to:");
        label.setAlignment(Alignment.RIGHT, Alignment.CENTER);
        box.packStart(label, false, false, 0);
        group.add(label);

        personPicker = new WorkerPicker(data);
        box.packStart(personPicker, false, false, 0);

        top.packStart(box, false, false, 0);

        /*
         * Account
         */

        box = new HBox(false, 6);

        label = new Label("Account / Ledger:");
        label.setAlignment(Alignment.RIGHT, Alignment.CENTER);
        box.packStart(label, false, false, 0);
        group.add(label);

        accountPicker = new AccountLedgerPicker(data);
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

        if (t == null) {
            left = new Debit();
            right = new Credit();

            existing = new ReimbursableTransaction();
        } else {
            date.setDate(t.getDate());
            entries = services.findEntries(t);

            for (Entry e : entries) {
                Ledger l;

                l = e.getParentLedger();
                if (l.getParentAccount() instanceof ReimbursableExpensesPayableAccount) {
                    worker = services.findWorker(l);
                    personPicker.setWorker(worker);
                    right = e;
                } else {
                    accountPicker.setLedger(l);
                    amountEntryBox.setAmount(e.getAmount(), e.getCurrency(), e.getValue());
                    left = e;
                }
            }
            str = t.getDescription();
            description.setText(str);

            existing = t;
        }
        super.setOperand(existing);

        /*
         * Tax
         */

        box = new HBox(false, 6);

        label = new Label("Tax:");
        label.setAlignment(Alignment.RIGHT, Alignment.CENTER);
        box.packStart(label, false, false, 0);
        group.add(label);

        taxEntryBox = new TaxEntryBox(data);
        box.packStart(taxEntryBox, false, false, 0);

        top.packStart(box, false, false, 0);

        amountEntryBox.connect(new ForeignAmountEntryBox.Updated() {
            public void onUpdated(long amount, Currency currency, long value) {
                if (currency.getCode().equals("AUD")) {
                    taxEntryBox.setTax("GST", 0);
                    taxEntryBox.setAmount(value);
                } else {
                    taxEntryBox.setTax("N/A", 0);
                }
            }
        });

        window.showAll();
    }

    protected void doUpdate() {
        final Worker worker;
        MessageDialog dialog;
        ResponseType response;
        String str;
        final Ledger expense, payable;
        final Transaction transaction;
        final long amount, value;
        Currency currency;
        final long datestamp;

        worker = personPicker.getWorker();
        if (worker == null) {
            dialog = new ErrorMessageDialog(this, "Select someone!",
                    "You need to select the person you're trying to pay first.");
            dialog.run();
            dialog.hide();
            personPicker.grabFocus();
            return;
        }

        str = description.getText();
        if (str.equals("")) {
            dialog = new WarningMessageDialog(
                    window,
                    "Enter a description!",
                    "It's really a good idea for each Transaction to have an appropriate description."
                            + " Try to be a bit more specific than '<i>Expenses reimbursable to Joe Smith</i>' as that won't facilitate identifying this Transaction in future searches and reports."
                            + " Perhaps '<i>Taxi from CDG to Paris Hotel</i>' instead.");
            dialog.setSecondaryUseMarkup(true);
            response = dialog.run();
            dialog.hide();

            if (response == ResponseType.CANCEL) {
                description.grabFocus();
                return;
            }
        }

        expense = accountPicker.getLedger();
        if (expense == null) {
            dialog = new ErrorMessageDialog(window, "Select an Account!",
                    "You need to select the account to which these expenses apply.");
            dialog.run();
            dialog.hide();

            accountPicker.grabFocus();
            return;
        }

        amount = amountEntryBox.getAmount();
        value = amountEntryBox.getValue();

        if (value == 0) {
            dialog = new ErrorMessageDialog(window, "Enter amount!",
                    "Sorry, but you can't post a transaction with zero value.");
            dialog.run();
            dialog.hide();

            amountEntryBox.grabFocus();
            return;
        }

        /*
         * Guards passed.
         */

        window.hide();

        transaction = existing;

        payable = worker.getExpensesPayable();

        /*
         * Debit expense
         */

        left.setParentLedger(expense);
        left.setParentTransaction(transaction);
        left.setAmount(amount);
        currency = amountEntryBox.getCurrency();
        left.setCurrency(currency);
        left.setValue(value);

        /*
         * Credit employee payable, home dollars
         */

        right.setParentLedger(payable);
        right.setParentTransaction(transaction);
        right.setAmount(amount);

        right.setAmount(value);
        currency = services.findCurrencyHome();
        right.setCurrency(currency);
        right.setValue(value);

        /*
         * Transaction
         */

        datestamp = date.getDate();
        transaction.setDate(datestamp);
        transaction.setDescription(str);

        services.postTransaction(transaction, left, right);
    }
}
