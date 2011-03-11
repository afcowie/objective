/*
 * ObjectiveAccounts, accounting for small professional services firms.
 *
 * Copyright © 2006-2011 Operational Dynamics Consulting, Pty Ltd
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

import org.gnome.gdk.Color;
import org.gnome.gdk.EventFocus;
import org.gnome.gtk.Editable;
import org.gnome.gtk.Entry;
import org.gnome.gtk.StateType;
import org.gnome.gtk.Widget;

/**
 * A tiny little Entry Widget to properly read in Amount fields. It is
 * delegates to and wraps an Entry.
 * 
 * <p>
 * Features of this Widget:
 * 
 * <ul>
 * <li>Turns the text red if there is an illegal argument.
 * </ul>
 * 
 * @author Andrew Cowie
 * @see objective.ui.AmountDisplay the complementary display widget.
 */
public class AmountEntry extends Entry
{
    private final Entry entry;

    // cache
    private long amount;

    /**
     * No need to have a Set of these; only one GUI Window owns the parent
     * relationship to this Widget, and the whole point is to only have one
     * invokation of the callback to that Window's code.
     */
    private AmountEntry.Updated handler;

    /**
     * Construct a new AmountEntry. Use setAmount() if you want to pass in a
     * previously instantiated Amount object.
     */
    public AmountEntry() {
        /*
         * zero is a nice default ;)
         */
        super("0.00");

        entry = this;
        entry.setWidthChars(10);
        entry.setAlignment(1.0f);

        entry.connect(new Entry.Changed() {
            public void onChanged(Editable source) {
                final String text;

                /*
                 * "changed" signals will come in as a result of either user
                 * action or setText() on the Widget. In either case, after
                 * appropriate guards we parse the result by [trying to] set
                 * our Amount object.
                 */

                if (!entry.getHasFocus()) {
                    /*
                     * Then the change wasn't the result of a user action in
                     * this Widget, but rather as a result of some other logic
                     * element calling setText(). So, ignore the event by
                     * returning immediately.
                     */
                    return;
                }

                text = entry.getText();

                if (text.equals("")) {
                    /*
                     * If we have an empty field, then don't do anything to
                     * the Amount we represent (leaving it at whatever was
                     * set). This also covers the case where changing the
                     * value 1results in a Entry.Changed event where the text
                     * is blank right before a event where the text is the new
                     * value.
                     */
                    return;
                }

                try {
                    amount = Amount.stringToNumber(text);
                    entry.modifyText(StateType.NORMAL, Color.BLACK);
                } catch (NumberFormatException nfe) {
                    /*
                     * if the user input is invalid, then ignore it. The
                     * Amount will stay as previously set.
                     */
                    entry.modifyText(StateType.NORMAL, Color.RED);
                    return;
                }

                if (handler != null) {
                    handler.onUpdated(amount);
                }
            }
        });

        entry.connect(new Entry.Activate() {
            public void onActivate(Entry source) {
                /*
                 * Ensure the Entry shows the properly formatted Amount.
                 */
                final String text = Amount.numberToString(amount);
                entry.setText(text);
                entry.setPosition(text.length());
            }
        });

        entry.connect(new Widget.FocusOutEvent() {
            public boolean onFocusOutEvent(Widget source, EventFocus event) {
                /*
                 * Ensure the Entry shows the properly formatted Amount.
                 */
                final String text = Amount.numberToString(amount);
                entry.setText(text);
                /*
                 * It looks really stupid if a subset (or even all) of the
                 * characters are selected when focus leaves; it grays out and
                 * there is no reason to keep the visual reminder of the
                 * selection.
                 */
                entry.selectRegion(0, 0);
                entry.modifyText(StateType.NORMAL, Color.BLACK);

                return false;
            };
        });

        handler = null;
    }

    /**
     * Add a use case specific listener to the Entry that underlies the
     * AmountEntry Widget. AmountEntry internally connects to Entry.Changed so
     * we can't really ask outside code to also connect to that signal.
     * 
     * <p>
     * Note that you can only call this once; only one GUI Window owns the
     * parent relationship to this Widget, and the whole point is to only have
     * one invokation of the callback to that Window's code.
     */
    public void connect(AmountEntry.Updated handler) {
        if (this.handler != null) {
            throw new IllegalStateException("You can't have more than one AmountEntry.Update");
        }
        this.handler = handler;
    }

    public interface Updated
    {
        public void onUpdated(long amount);
    }

    /**
     * @return the Amount as currently held by this Display Widget. Note this
     *         is a live reference, not a copy!
     */
    public long getAmount() {
        return amount;
    }

    /**
     * Set the Amount object this Display Widget is representing. Does
     * <i>not</i> emit AmountEntry.Updated signal; if you need to manually
     * fire it call {@link #emitUpdated()}.
     */
    public void setAmount(long amount) {
        final String str;

        str = Amount.numberToString(amount);

        entry.setText(str);
        entry.modifyText(StateType.NORMAL, Color.BLACK);

        this.amount = amount;
    }

    public void emitUpdated() {
        handler.onUpdated(amount);
    }
}
