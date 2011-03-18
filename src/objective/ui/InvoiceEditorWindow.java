/*
 * ObjectiveAccounts, accounting for small professional services firms.
 *
 * Copyright © 2011 Operational Dynamics Consulting, Pty Ltd
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
import objective.domain.InvoiceTransaction;
import objective.domain.Ledger;
import objective.domain.ReimbursableExpensesPayableAccount;
import objective.domain.Transaction;
import objective.persistence.DataStore;
import objective.services.TransactionOperations;

import org.gnome.gdk.Event;
import org.gnome.gtk.Alignment;
import org.gnome.gtk.ErrorMessageDialog;
import org.gnome.gtk.Gtk;
import org.gnome.gtk.HBox;
import org.gnome.gtk.Label;
import org.gnome.gtk.MessageDialog;
import org.gnome.gtk.ResponseType;
import org.gnome.gtk.WarningMessageDialog;
import org.gnome.gtk.Widget;
import org.gnome.gtk.Window;

/**
 * Enter or edit an invoice, initiating money we owe to a supplier or are owed
 * by a client.
 * 
 * @author Andrew Cowie
 */
// cloned from ReimbursableExpensesEditorWindow
public class InvoiceEditorWindow extends TransactionEditorWindow
{
    private final AccountLedgerPicker originPicker;

    private final AccountLedgerPicker accountPicker;

    private final ForeignAmountEntryBox money;

    private final TaxEntryBox tax;

    private InvoiceTransaction invoice = null;

    private TransactionOperations services;

    private Entry left, right;

    /**
     * Construct the Window. Pass <code>null</code> to create a new
     * Transaction.
     */
    public InvoiceEditorWindow(final DataStore data, final InvoiceTransaction t) {
        super(data, "Invoice");
        Label label;
        HBox box;
        Entry[] entries;
        String str;

        services = new TransactionOperations(data);

        if (t == null) {
            window.setTitle("Enter new Invoice");
        } else {
            window.setTitle("Edit Invoice");
        }

        /*
         * Client or Supplier
         */

        box = new HBox(false, 6);

        label = new Label("Client | Supplier:");
        label.setAlignment(Alignment.RIGHT, Alignment.CENTER);
        box.packStart(label, false, false, 0);
        group.add(label);

        originPicker = new AccountLedgerPicker(data);
        box.packStart(originPicker, false, false, 0);

        top.packStart(box, false, false, 0);

        /*
         * Account
         */

        box = new HBox(false, 6);

        label = new Label("Account » Ledger:");
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

        money = new ForeignAmountEntryBox(data);
        box.packStart(money, false, false, 0);

        top.packStart(box, false, false, 0);

        /*
         * Tax
         */

        box = new HBox(false, 6);

        label = new Label("Tax:");
        label.setAlignment(Alignment.RIGHT, Alignment.CENTER);
        box.packStart(label, false, false, 0);
        group.add(label);

        tax = new TaxEntryBox(data);
        box.packStart(tax, false, false, 0);

        top.packStart(box, false, false, 0);

        if (t == null) {
            left = new Debit();
            right = new Credit();

            invoice = new InvoiceTransaction();
        } else {
            date.setDate(t.getDate());
            entries = services.findEntries(t);

            for (Entry e : entries) {
                Ledger l;

                l = e.getParentLedger();
                if (l.getParentAccount() instanceof ReimbursableExpensesPayableAccount) {
                    // FIXME
                    left = e;
                } else {
                    // FIXME
                    right = e;
                }
            }
            str = t.getDescription();
            description.setText(str);

            invoice = t;
        }
        super.setOperand(invoice);

        window.showAll();
    }

    protected void doUpdate() {
        MessageDialog dialog;
        ResponseType response;
        String str;
        final Ledger expense, payable;
        final Transaction transaction;
        final long amount, value;
        Currency currency;
        final long datestamp;

        payable = originPicker.getLedger();
        if (payable == null) {
            dialog = new ErrorMessageDialog(window, "Select an Account!",
                    "You need to select the client or supplier to which this Invoice applies.");
            dialog.run();
            dialog.hide();

            originPicker.grabFocus();
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

        amount = money.getAmount();
        value = money.getValue();

        if (value == 0) {
            dialog = new ErrorMessageDialog(window, "Enter amount!",
                    "Sorry, but you can't post a transaction with zero value.");
            dialog.run();
            dialog.hide();

            money.grabFocus();
            return;
        }

        /*
         * Guards passed.
         */

        window.hide();

        transaction = invoice;

        /*
         * Debit expense
         */

        left.setParentLedger(expense);
        left.setParentTransaction(transaction);
        left.setAmount(amount);
        currency = money.getCurrency();
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

    // REMOVE
    protected void doCancel() {
        Gtk.mainQuit();
    }

    public static void main(String[] args) {
        final DataStore data;
        final Window window;

        Gtk.init(args);

        data = new DataStore("schema/accounts.db");

        window = new InvoiceEditorWindow(data, null);
        window.present();

        window.connect(new Window.DeleteEvent() {
            public boolean onDeleteEvent(Widget source, Event event) {
                Gtk.mainQuit();
                return false;
            }
        });

        Gtk.main();

        data.close();
    }
}
