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
import objective.domain.AccountsReceivableAccount;
import objective.domain.Credit;
import objective.domain.Debit;
import objective.domain.Entry;
import objective.domain.InvoiceTransaction;
import objective.domain.Ledger;
import objective.domain.RevenueAccount;
import objective.domain.SalesInvoiceTransaction;
import objective.domain.SalesTaxPayableAccount;
import objective.persistence.DataStore;

import org.gnome.gdk.Event;
import org.gnome.gtk.Gtk;
import org.gnome.gtk.Widget;
import org.gnome.gtk.Window;

/**
 * Enter or edit an invoice, initiating money we owe to a supplier or are owed
 * by a client.
 * 
 * @author Andrew Cowie
 */
public class SalesInvoiceTransactionEditorWindow extends InvoiceTransactionEditorWindow
{
    private InvoiceTransaction invoice = null;

    /**
     * The people we have charged a fee to
     */
    private Debit entity;

    /**
     * The revenue account
     */
    private Credit income;

    /**
     * GST collected, if applicable.
     */
    private Credit friction;

    /**
     * Construct the Window. Pass <code>null</code> to create a new
     * Transaction.
     */
    public SalesInvoiceTransactionEditorWindow(final DataStore data, final InvoiceTransaction t) {
        super(data, "Sales Invoice");
        Entry[] entries;
        String str;
        Ledger gst;

        if (t == null) {
            window.setTitle("Enter new Invoice");
        } else {
            window.setTitle("Edit Invoice");
        }

        if (t == null) {
            entity = new Debit();
            income = new Credit();

            invoice = new SalesInvoiceTransaction();
        } else {
            date.setDate(t.getDate());
            entries = services.findEntries(t);

            for (Entry e : entries) {
                Ledger l;
                Account a;

                l = e.getParentLedger();
                a = l.getParentAccount();
                if (a instanceof AccountsReceivableAccount) {
                    super.setOrigin(e);
                    entity = (Debit) e;
                } else if (a instanceof SalesTaxPayableAccount) {
                    super.setTaxation(e);
                    friction = (Credit) e;
                } else if (a instanceof RevenueAccount) {
                    super.setDestination(e);
                    income = (Credit) e;
                } else {
                    throw new IllegalStateException();
                }
            }

            str = t.getDescription();
            description.setText(str);

            invoice = t;
        }
        if (friction == null) {
            friction = new Credit();

            gst = services.findLedger("GST", "Collected");
            friction.setParentLedger(gst);
        }

        super.setOperand(invoice);

        window.showAll();
    }

    /*
     * These are called by super InvoiceTransactionEditorWindow to supply the
     * corresponding Entry objects.
     */

    protected Entry getEntity() {
        return entity;
    }

    protected Entry getIncome() {
        return income;
    }

    protected Entry getFriction() {
        if (friction == null) {
            throw new AssertionError();
        }
        return friction;
    }

    public static void main(String[] args) {
        final DataStore data;
        final Window window;

        Gtk.init(args);

        data = new DataStore("schema/accounts.db");

        window = new SalesInvoiceTransactionEditorWindow(data, null);
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
