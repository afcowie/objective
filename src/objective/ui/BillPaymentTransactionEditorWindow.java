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

import objective.domain.Account;
import objective.domain.AccountsPayableAccount;
import objective.domain.BankAccount;
import objective.domain.BillPaymentTransaction;
import objective.domain.CardAccount;
import objective.domain.Credit;
import objective.domain.Debit;
import objective.domain.Entry;
import objective.domain.Ledger;
import objective.domain.PaymentTransaction;
import objective.domain.ReimbursableExpensesPayableAccount;
import objective.persistence.DataStore;

import org.gnome.gdk.Event;
import org.gnome.gtk.Gtk;
import org.gnome.gtk.Widget;
import org.gnome.gtk.Window;

/**
 * Enter or edit a payment made to a supplier.
 * 
 * @author Andrew Cowie
 */
public class BillPaymentTransactionEditorWindow extends PaymentTransactionEditorWindow
{
    private PaymentTransaction payment = null;

    /**
     * The people we have charged a fee to, or that we owe money to.
     */
    private Entry payable;

    /**
     * Account the payment is made from.
     */
    private Entry from;

    /**
     * Construct the Window. Pass <code>null</code> to create a new
     * Transaction.
     */
    public BillPaymentTransactionEditorWindow(final DataStore data, final PaymentTransaction t) {
        super(data, "Bill Payment");
        Entry[] entries;
        String str;

        if (t == null) {
            window.setTitle("Enter new Payment");
        } else {
            window.setTitle("Edit Payment");
        }

        if (t == null) {
            Entry e;

            e = new Debit();
            this.payable = e;

            e = new Credit();
            this.from = e;

            payment = new BillPaymentTransaction();
        } else {
            date.setDate(t.getDate());
            entries = services.findEntries(t);

            for (Entry e : entries) {
                Ledger l;
                Account a;

                l = e.getParentLedger();
                a = l.getParentAccount();
                if (a instanceof AccountsPayableAccount) {
                    super.setOrigin(e);
                    this.payable = e;
                } else if ((a instanceof BankAccount) || (a instanceof CardAccount)
                        || (a instanceof ReimbursableExpensesPayableAccount)) {
                    super.setPayment(e);
                    this.from = e;
                } else {
                    throw new IllegalStateException();
                }
            }

            str = t.getDescription();
            description.setText(str);

            payment = t;
        }
        // TODO Currency gain/loss

        super.setOperand(payment);

        window.showAll();
    }

    /*
     * These are called by super InvoiceTransactionEditorWindow to supply the
     * corresponding Entry objects.
     */

    protected Entry getEntity() {
        return payable;
    }

    protected Entry getPayment() {
        return from;
    }

    public static void main(String[] args) {
        final DataStore data;
        final Window window;

        Gtk.init(args);

        data = new DataStore("schema/accounts.db");

        window = new BillPaymentTransactionEditorWindow(data, null);
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

    // REMOVE
    protected void doCancel() {
        Gtk.mainQuit();
    }

    // REMOVE
    protected void doUpdate() {
        super.doUpdate();
        Gtk.mainQuit();
    }
}
