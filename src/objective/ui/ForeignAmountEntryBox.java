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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import objective.domain.Currency;
import objective.domain.ForeignAmount;
import objective.persistence.DataStore;

import org.gnome.gdk.Color;
import org.gnome.gdk.EventFocus;
import org.gnome.gtk.ComboBox;
import org.gnome.gtk.Editable;
import org.gnome.gtk.Entry;
import org.gnome.gtk.HBox;
import org.gnome.gtk.Label;
import org.gnome.gtk.StateType;
import org.gnome.gtk.Widget;

public class ForeignAmountEntryBox extends HBox
{
    private final Currency home;

    private Currency lastCurrency;

    private final List<Widget> gray;

    private final AmountEntry foreign;

    private final Entry exchange;

    private final AmountEntry local;

    private final CurrencySelector selector;

    private ForeignAmountEntryBox.Updated handler;

    private static final Map<Currency, String> lastRates;

    static {
        lastRates = new HashMap<Currency, String>();
    }

    public ForeignAmountEntryBox(DataStore data) {
        super(false, 3);
        Label x_Label, equals_Label, homeCode_Label;

        home = data.lookupCurrency("AUD");

        if (lastCurrency == null) {
            lastCurrency = home;
        }

        gray = new ArrayList<Widget>();

        foreign = new AmountEntry();
        foreign.setAmount(0); // dummy
        foreign.modifyText(StateType.NORMAL, Color.RED);
        packStart(foreign, false, false, 0);

        selector = new CurrencySelector(data);
        selector.setCurrency(lastCurrency);
        packStart(selector, false, false, 0);

        // \u00d7 is ×
        x_Label = new Label("\u00d7");
        packStart(x_Label, false, false, 0);
        gray.add(x_Label);

        exchange = new Entry();
        exchange.setWidthChars(8);
        exchange.setText("1.00000");
        packStart(exchange, false, false, 0);
        gray.add(exchange);

        equals_Label = new Label("=");
        packStart(equals_Label, false, false, 0);
        gray.add(equals_Label);

        local = new AmountEntry();
        packStart(local, false, false, 0);
        gray.add(local);

        homeCode_Label = new Label(home.getCode());
        packStart(homeCode_Label, false, false, 0);
        gray.add(homeCode_Label);

        foreign.connect(new AmountEntry.Updated() {
            public void onUpdated(final long amount) {
                final Currency currency;
                final String str;
                final double rate;
                final long value;

                if (amount == 0) {
                    foreign.modifyText(StateType.NORMAL, Color.RED);
                    return;
                } else {
                    foreign.modifyText(StateType.NORMAL, Color.BLACK);
                }

                currency = selector.getCurrency();

                str = exchange.getText();
                rate = ForeignAmount.stringToRate(str);
                value = ForeignAmount.calculateValue(amount, rate);

                /*
                 * When you change the foreign amount, you just change the
                 * home value.
                 */

                local.setAmount(value);

                if (handler != null) {
                    handler.onUpdated(amount, currency, value);
                }
            }
        });

        selector.connect(new ComboBox.Changed() {
            public void onChanged(ComboBox source) {
                final Currency currency;
                final long amount, value;
                final double rate;
                String str;

                currency = selector.getCurrency();

                str = lastRates.get(currency);
                if (str == null) {
                    rate = 1.0;
                    str = ForeignAmount.rateToString(rate);
                    lastRates.put(currency, str);
                } else {
                    rate = ForeignAmount.stringToRate(str);
                }

                amount = foreign.getAmount();

                exchange.setText(str);

                value = ForeignAmount.calculateValue(amount, rate);
                local.setAmount(value);
                if (value == 0) {
                    local.modifyText(StateType.NORMAL, Color.RED);
                }

                grayOut();

                if (handler != null) {
                    handler.onUpdated(amount, currency, value);
                }
            }
        });

        /*
         * All we do here is colour the Entry
         */

        exchange.connect(new Entry.Changed() {
            public void onChanged(Editable editable) {
                final Entry source;
                final String str;
                final double rate;
                final long amount, value;

                source = (Entry) editable;
                if (!source.getHasFocus()) {
                    return;
                }

                str = exchange.getText();
                try {
                    rate = ForeignAmount.stringToRate(str);
                    exchange.modifyText(StateType.NORMAL, Color.BLACK);
                } catch (NumberFormatException nfe) {
                    exchange.modifyText(StateType.NORMAL, Color.RED);
                    return;
                }

                amount = foreign.getAmount();
                value = ForeignAmount.calculateValue(amount, rate);
                local.setAmount(value);
            }
        });

        /*
         * If focus leaves or user presses enter, then apply the formatting
         * inherent in ForeignAmount's rate String.
         */

        exchange.connect(new Entry.Activate() {
            public void onActivate(Entry source) {
                final String str, formatted;
                final double rate;
                final long amount, value;
                final Currency currency;

                str = exchange.getText();

                rate = ForeignAmount.stringToRate(str);
                formatted = ForeignAmount.rateToString(rate);

                exchange.setText(formatted);

                amount = foreign.getAmount();

                currency = selector.getCurrency();

                value = ForeignAmount.calculateValue(amount, rate);
                local.setAmount(value);

                lastRates.put(currency, formatted);

                exchange.setPosition(str.length());
                exchange.selectRegion(0, 0);
                exchange.modifyText(StateType.NORMAL, Color.BLACK);

                if (handler != null) {
                    handler.onUpdated(amount, currency, value);
                }
            }
        });

        exchange.connect(new Widget.FocusOutEvent() {
            public boolean onFocusOutEvent(Widget source, EventFocus event) {
                exchange.activate();
                return false;
            };
        });

        local.connect(new AmountEntry.Updated() {
            public void onUpdated(final long value) {
                final long amount;
                final Currency currency;
                final double rate;
                final String str;

                if (!local.getHasFocus()) {
                    return;
                }

                amount = foreign.getAmount();

                if (value == 0) {
                    local.modifyText(StateType.NORMAL, Color.RED);
                    return;
                } else {
                    local.modifyText(StateType.NORMAL, Color.BLACK);
                }

                /*
                 * No need to set faceValue - after all, changing the home
                 * value manually only imacts the effective exchange rate.
                 */

                currency = selector.getCurrency();

                rate = ForeignAmount.calculateRate(amount, value);
                str = ForeignAmount.rateToString(rate);
                exchange.setText(str);
                lastRates.put(currency, str);

                if (handler != null) {
                    handler.onUpdated(amount, currency, value);
                }
            }
        });

        local.connect(new Widget.FocusOutEvent() {
            public boolean onFocusOutEvent(Widget source, EventFocus event) {
                long value;

                value = local.getAmount();
                if (value == 0) {
                    local.modifyText(StateType.NORMAL, Color.RED);
                }
                return false;
            }
        });

        grayOut();
    }

    /**
     * Toggle the sensitivity of all the Widgets that represent exchange rate
     * and home value, as listed in the gray List.
     */
    private void grayOut() {
        boolean state;

        if (selector.getCurrency() == home) {
            state = false;
        } else {
            state = true;
        }

        for (Widget w : gray) {
            w.setSensitive(state);
        }
    }

    public void grabFocus() {
        foreign.grabFocus();
    }

    /**
     * The whole point of the Widget is, of course, to generate a
     * ForeignAmount.
     * 
     * @return the ForeignAmount as specified by the user.
     */
    public long getAmount() {
        return foreign.getAmount();
    }

    public long getValue() {
        return local.getAmount();
    }

    public void setAmount(long amount, Currency currency, long value) {
        double rate;
        final String str;

        foreign.setAmount(amount);

        if (amount == 0) {
            foreign.modifyText(StateType.NORMAL, Color.RED);
            rate = 1.0;
        } else {
            rate = ForeignAmount.calculateRate(amount, value);
        }

        str = ForeignAmount.rateToString(rate);

        lastRates.put(currency, str);

        selector.setCurrency(currency);
        exchange.setText(str);
        local.setAmount(value);

        grayOut();
    }

    public interface Updated
    {
        public void onUpdated(long amount, Currency currency, long value);
    }

    public void connect(ForeignAmountEntryBox.Updated handler) {
        if (this.handler != null) {
            throw new AssertionError();
        }
        this.handler = handler;
    }

    public Currency getCurrency() {
        return selector.getCurrency();
    }
}
