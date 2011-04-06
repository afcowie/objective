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

import objective.domain.Currency;
import objective.domain.Entry;
import objective.domain.Ledger;
import objective.domain.Transaction;
import objective.persistence.DataStore;

import org.gnome.gtk.ErrorMessageDialog;
import org.gnome.gtk.MessageDialog;

/**
 * Enter or edit an invoice, initiating money we owe to a supplier or are owed
 * by a client.
 * 
 * @author Andrew Cowie
 */
public abstract class PaymentTransactionEditorWindow extends TransactionEditorWindow
{
    private final AccountLedgerPicker owing;

    private final AccountLedgerPicker payment;

    private final ForeignAmountEntryBox money;

    /**
     * Construct the Window.
     */
    public PaymentTransactionEditorWindow(final DataStore data, final String heading) {
        super(data, heading);

        /*
         * Client or Supplier
         */

        owing = new AccountLedgerPicker(data);
        this.addField("Client | Supplier:", owing);

        /*
         * Payment Source
         */

        payment = new AccountLedgerPicker(data);
        super.addField("Account » Ledger:", payment);

        /*
         * Amount
         */

        money = new ForeignAmountEntryBox(data);
        super.addField("Amount:", money);

        window.showAll();
    }

    /**
     * Specify the Entry that is the client or supplier to whom the Accounts
     * Receivable or Accounts Payable entry will be made. <b>This sets the
     * amounts in the ForeignAmountEntryBox</b>.
     */
    protected void setOrigin(final Entry e) {
        final Ledger l;
        final long amount, value;
        final Currency currency;

        l = e.getParentLedger();

        owing.setLedger(l);

        amount = e.getAmount();
        currency = e.getCurrency();
        value = e.getValue();

        money.setAmount(amount, currency, value);
    }

    /**
     * Specify which account this bill will be paid payment (to).
     */
    protected void setPayment(final Entry e) {
        final Ledger l;

        l = e.getParentLedger();

        payment.setLedger(l);
    }

    /**
     * The people we have charged a fee to, or that we owe money to.
     */
    protected abstract Entry getEntity();

    /**
     * The bank account, credit card, or expenses payable, as appropriate.
     */
    protected abstract Entry getPayment();

    protected void doUpdate() {
        MessageDialog dialog;
        String str;
        final Ledger expense, payable;
        final Transaction transaction;
        final long amount, value;
        Currency currency, home;
        final long datestamp;
        Entry entry;

        payable = owing.getLedger();
        if (payable == null) {
            dialog = new ErrorMessageDialog(window, "Select an Account!",
                    "You need to select the Client or Supplier to which this payment applies.");
            dialog.run();
            dialog.hide();

            owing.grabFocus();
            return;
        }

        expense = payment.getLedger();
        if (expense == null) {
            dialog = new ErrorMessageDialog(window, "Select an Account!",
                    "You need to select the account which this payment is going from or to.");
            dialog.run();
            dialog.hide();

            payment.grabFocus();
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

        transaction = super.getOperand();

        /*
         * Accounts Receivable or Accounts Payable
         */

        Entry e1, e2;

        entry = this.getEntity();

        entry.setParentLedger(payable);
        entry.setParentTransaction(transaction);
        entry.setAmount(amount);
        currency = money.getCurrency();
        entry.setCurrency(currency);
        entry.setValue(value);

        e1 = entry;

        /*
         * Bank account etc
         */

        entry = this.getPayment();

        entry.setParentLedger(expense);
        entry.setParentTransaction(transaction);

        entry.setAmount(amount);
        entry.setCurrency(currency);
        entry.setValue(value);

        e2 = entry;

        /*
         * Currency gain/loss
         */

        // TODO

        /*
         * Transaction
         */

        datestamp = date.getDate();
        transaction.setDate(datestamp);
        str = description.getText();
        transaction.setDescription(str);

        services.postTransaction(transaction, e1, e2);
    }
}
