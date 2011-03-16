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

import objective.domain.Amount;
import objective.domain.Currency;
import objective.domain.Ledger;
import objective.domain.Tax;
import objective.domain.Worker;
import objective.persistence.DataStore;

import org.gnome.gdk.Event;
import org.gnome.gtk.Gtk;
import org.gnome.gtk.HBox;
import org.gnome.gtk.VBox;
import org.gnome.gtk.Widget;
import org.gnome.gtk.Window;

class TemporaryHolder extends Window
{
    TemporaryHolder(DataStore data) {
        super();
        final VBox top;
        HBox box;
        final AccountLedgerDisplay ald;
        final AccountLedgerPicker alp;
        final WorkerPicker wp;
        final AmountEntry ae;
        final AmountDisplay ad;
        final CurrencySelector cs;
        final ForeignAmountEntryBox faeb;
        final TaxEntryBox teb;
        final DescriptionEntry de;
        final Worker worker;
        final Ledger ledger;
        final Currency jpy, gbp;

        top = new VBox(false, 6);

        /*
         * AccountLedgerDisplay
         */

        ledger = data.lookupLedger(16);
        ald = new AccountLedgerDisplay();
        ald.setLedger(ledger);

        box = new HBox(false, 0);
        box.packStart(ald, true, true, 0);
        top.packStart(box, false, false, 0);

        /*
         * AccountLedgerPicker
         */

        alp = new AccountLedgerPicker(data);

        box = new HBox(false, 0);
        box.packStart(alp, true, true, 0);
        top.packStart(box, false, false, 0);

        /*
         * WorkerPicker
         */

        worker = data.lookupWorker(1);

        wp = new WorkerPicker(data);
        wp.setWorker(worker);
        wp.connect(new WorkerPicker.Updated() {
            public void onUpdated(Worker worker) {
                System.out.println(worker);
            }
        });

        box = new HBox(false, 0);
        box.packStart(wp, false, false, 0);
        top.packStart(box, false, false, 0);

        /*
         * AmountDisplay
         */

        ad = new AmountDisplay();
        ad.setAmount(0);

        box = new HBox(false, 0);
        box.packStart(ad, false, false, 0);
        top.packStart(box, false, false, 0);

        /*
         * AmountEntry
         */

        ae = new AmountEntry();
        ae.setAmount(65910L);
        ae.connect(new AmountEntry.Updated() {
            public void onUpdated(long amount) {
                ad.setAmount(amount);
            }
        });

        box = new HBox(false, 0);
        box.packStart(ae, false, false, 0);
        top.packStart(box, false, false, 0);

        /*
         * CurrencySelector
         */

        jpy = data.lookupCurrency("INR");

        cs = new CurrencySelector(data);
        cs.setCurrency(jpy);

        box = new HBox(false, 0);
        box.packStart(cs, false, false, 0);
        top.packStart(box, false, false, 0);

        /*
         * ForeignAmountEntryBox
         */

        gbp = data.lookupCurrency("GBP");

        faeb = new ForeignAmountEntryBox(data);
        faeb.setAmount(24216, gbp, 62007);

        box = new HBox(false, 0);
        box.packStart(faeb, false, false, 0);
        top.packStart(box, false, false, 0);

        faeb.connect(new ForeignAmountEntryBox.Updated() {
            public void onUpdated(long amount, Currency currency, long value) {
                System.out.println(currency.getSymbol() + Amount.numberToString(amount) + " "
                        + currency.getCode() + " = " + Amount.numberToString(value));
            }
        });

        /*
         * TaxEntryBox
         */

        teb = new TaxEntryBox(data);
        teb.setTax("N/A", 0L);
        teb.setAmount(faeb.getValue());

        box = new HBox(false, 0);
        box.packStart(teb, false, false, 0);
        top.packStart(box, false, false, 0);

        teb.connect(new TaxEntryBox.Updated() {
            public void onUpdated(Tax tag, long value) {
                System.out.println(tag.getCode() + " " + Amount.numberToString(value));
            }
        });

        /*
         * DesecriptionEntry
         */

        de = new DescriptionEntry();
        de.setText("Remarkable");

        box = new HBox(false, 0);
        box.packStart(de, false, false, 0);
        top.packStart(box, false, false, 0);

        de.connect(new DescriptionEntry.Updated() {
            public void onUpdated(String description) {
                System.out.println(description);
            }
        });

        super.add(top);
        super.setTitle("Temporary");
        super.move(800, 50);
        super.setBorderWidth(10);
        super.showAll();

        super.connect(new Window.DeleteEvent() {
            public boolean onDeleteEvent(Widget source, Event event) {
                Gtk.mainQuit();
                return false;
            }
        });
    }

    public static void main(String[] args) {
        final DataStore data;
        final Window holder;

        Gtk.init(args);

        data = new DataStore("schema/accounts.db");

        holder = new TemporaryHolder(data);

        holder.present();
        Gtk.main();

        data.close();
    }

}
