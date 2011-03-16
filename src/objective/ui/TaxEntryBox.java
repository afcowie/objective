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

import java.util.ArrayList;
import java.util.List;

import objective.domain.Currency;
import objective.domain.ForeignAmount;
import objective.domain.Tax;
import objective.persistence.DataStore;

import org.gnome.gdk.EventFocus;
import org.gnome.gtk.Alignment;
import org.gnome.gtk.ComboBox;
import org.gnome.gtk.HBox;
import org.gnome.gtk.Label;
import org.gnome.gtk.Widget;

public class TaxEntryBox extends HBox
{
    private final List<Widget> gray;

    private final AmountDisplay price;

    private final AmountEntry tax;

    private final TaxSelector selector;

    private TaxEntryBox.Updated handler;

    /**
     * The amount we're calculating tax for. Sent in from outside.
     */
    private long amount;

    public TaxEntryBox(DataStore data) {
        super(false, 3);
        Currency currency;
        final Label home;

        gray = new ArrayList<Widget>();

        price = new AmountDisplay();
        gray.add(price);

        selector = new TaxSelector(data);

        tax = new AmountEntry();
        gray.add(tax);

        currency = data.lookupCurrency("AUD");
        home = new Label(currency.getCode());
        home.setAlignment(Alignment.LEFT, Alignment.CENTER);

        gray.add(home);

        super.packStart(tax, false, false, 0);
        super.packStart(home, false, false, 3);
        super.packStart(selector, false, false, 3);

        tax.connect(new AmountEntry.Updated() {
            public void onUpdated(final long amount) {
                final Tax tag;

                tag = selector.getCode();

                if (handler != null) {
                    handler.onUpdated(tag, amount);
                }
            }
        });

        selector.connect(new ComboBox.Changed() {
            public void onChanged(ComboBox source) {
                final Tax tag;
                final long value;
                final double rate;

                tag = selector.getCode();

                rate = tag.getRate();

                value = ForeignAmount.calculateValue(amount, rate);
                tax.setAmount(value);

                grayOut();

                if (handler != null) {
                    handler.onUpdated(tag, value);
                }
            }
        });

        tax.connect(new Widget.FocusOutEvent() {
            public boolean onFocusOutEvent(Widget source, EventFocus event) {
                tax.activate();
                return false;
            };
        });

        grayOut();
    }

    /**
     * If it's non-taxible, then gray out value.
     */
    private void grayOut() {
        Tax tag;
        double rate;
        boolean state;

        tag = selector.getCode();
        rate = tag.getRate();

        if (rate == 0.0) {
            state = false;
        } else {
            state = true;
        }

        for (Widget w : gray) {
            w.setSensitive(state);
        }
    }

    public void grabFocus() {
        selector.grabFocus();
    }

    /**
     * The whole point of the Widget is, of course, to generate a
     * ForeignAmount.
     * 
     * @return the ForeignAmount as specified by the user.
     */

    public long getValue() {
        return tax.getAmount();
    }

    public void setAmount(long amount) {
        final Tax tag;
        final double rate;
        final long value;

        this.amount = amount;

        price.setAmount(amount);

        tag = this.getTax();
        rate = tag.getRate();
        value = ForeignAmount.calculateValue(amount, rate);

        tax.setAmount(value);
    }

    public void setTax(String code, long value) {
        selector.setCode(code);
        tax.setAmount(value);
    }

    public interface Updated
    {
        public void onUpdated(Tax tag, long value);
    }

    public void connect(TaxEntryBox.Updated handler) {
        if (this.handler != null) {
            throw new AssertionError();
        }
        this.handler = handler;
    }

    public Tax getTax() {
        return selector.getCode();
    }
}
