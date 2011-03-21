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
import objective.domain.InvoiceTransaction;
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
public abstract class InvoiceTransactionEditorWindow extends TransactionEditorWindow
{
    private final AccountLedgerPicker origin;

    private final AccountLedgerPicker destination;

    private final ForeignAmountEntryBox money;

    private final TaxEntryBox taxation;

    /**
     * The people we have charged a fee to, or that we owe money to.
     */
    private Entry entity;

    /**
     * The revenue or expense account, as appropriate.
     */
    private Entry income;

    /**
     * GST collected or paid, if applicable.
     */
    private Entry friction;

    /**
     * Construct the Window. Pass <code>null</code> to create a new
     * Transaction.
     */
    public InvoiceTransactionEditorWindow(final DataStore data, final InvoiceTransaction t,
            final String heading) {
        super(data, heading);

        if (t == null) {
            window.setTitle("Enter new Invoice");
        } else {
            window.setTitle("Edit Invoice");
        }

        /*
         * Client or Supplier
         */

        origin = new AccountLedgerPicker(data);
        this.addField("Client | Supplier:", origin);

        /*
         * Account
         */

        destination = new AccountLedgerPicker(data);
        super.addField("Account » Ledger:", destination);

        /*
         * Amount
         */

        money = new ForeignAmountEntryBox(data);
        super.addField("Amount:", money);

        /*
         * Tax
         */

        taxation = new TaxEntryBox(data);
        taxation.setTax("GST", 0);
        super.addField("Tax:", taxation);

        window.showAll();

        money.connect(new ForeignAmountEntryBox.Updated() {
            public void onUpdated(long amount, Currency currency, long value) {
                if (currency.getCode().equals("AUD")) {
                    taxation.setTax("GST", 0);
                    taxation.setAmount(value);
                } else {
                    taxation.setTax("N/A", 0);
                }
            }
        });
    }

    protected void setEntryEntity(Entry entry) {
        this.entity = entry;
    }

    protected void setEntryIncome(Entry entry) {
        this.income = entry;
    }

    protected void setEntryTax(Entry entry) {
        this.friction = entry;
    }

    protected void doUpdate() {
        MessageDialog dialog;
        String str;
        final Ledger expense, payable;
        final Transaction transaction;
        final long amount, value, tax;
        Currency currency, home;
        final long datestamp;

        payable = origin.getLedger();
        if (payable == null) {
            dialog = new ErrorMessageDialog(window, "Select an Account!",
                    "You need to select the Client or Supplier to which this invoice applies.");
            dialog.run();
            dialog.hide();

            origin.grabFocus();
            return;
        }

        expense = destination.getLedger();
        if (expense == null) {
            dialog = new ErrorMessageDialog(window, "Select an Account!",
                    "You need to select the account to which this revenue was earned, or to which these expenses apply.");
            dialog.run();
            dialog.hide();

            destination.grabFocus();
            return;
        }

        amount = money.getAmount();
        value = money.getValue();
        tax = taxation.getTax();

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
         * accounts receivable or payable
         */

        entity.setParentLedger(expense);
        entity.setParentTransaction(transaction);
        entity.setAmount(amount);
        currency = money.getCurrency();
        entity.setCurrency(currency);
        entity.setValue(value);

        /*
         * revenue or expense, home dollars
         */

        income.setParentLedger(payable);
        income.setParentTransaction(transaction);

        income.setAmount(value - tax);
        home = services.findCurrencyHome();
        income.setCurrency(home);
        income.setValue(value - tax);

        /*
         * taxation collected or paid, if applicable
         */

        friction.setParentTransaction(transaction);
        friction.setAmount(amount);

        friction.setAmount(tax);
        friction.setCurrency(home);
        friction.setValue(tax);

        /*
         * Transaction
         */

        datestamp = date.getDate();
        transaction.setDate(datestamp);
        str = description.getText();
        transaction.setDescription(str);

        services.postTransaction(transaction, entity, income, friction);
    }
}
