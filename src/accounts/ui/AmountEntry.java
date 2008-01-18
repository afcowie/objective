/*
 * AmountEntry.java
 * 
 * See LICENCE file for usage and redistribution terms
 * Copyright (c) 2006 Operational Dynamics
 */
package accounts.ui;

import org.gnome.gdk.Color;
import org.gnome.gdk.EventFocus;
import org.gnome.gtk.Editable;
import org.gnome.gtk.Entry;
import org.gnome.gtk.HBox;
import org.gnome.gtk.StateType;
import org.gnome.gtk.Widget;

import generic.ui.ChangeListener;
import generic.util.Debug;

import accounts.domain.Amount;

/**
 * A tiny little Entry Widget to properly read in Amount fields. It is
 * delegates to and wraps an Entry.
 * <p>
 * Features of this Widget:
 * <ul>
 * <li>Turns the text red if there is an illegal argument.
 * </ul>
 * 
 * @author Andrew Cowie
 * @see accounts.ui.AmountDisplay the complementary display widget.
 */
public class AmountEntry extends HBox
{

    private transient Amount amount;

    private Entry amount_Entry;

    /**
     * No need to have a Set of these; only one GUI Window owns the parent
     * relationship to this Widget, and the whole point is to only have one
     * invokation of the callback to that Window's code.
     */
    private ChangeListener changeListener = null;

    /**
     * Construct a new AmountEntry. Use setAmount() if you want to pass in a
     * previously instantiated Amount object.
     */
    public AmountEntry() {
        /*
         * This class is a Box subclass only so the Entry doesn't swell
         * unnessarily if set in a Table or similar widget. Otherwise, the box
         * nature is transparent.
         */
        super(false, 0);

        /*
         * zero is a nice default ;)
         */
        amount = new Amount(0);

        amount_Entry = new Entry();
        amount_Entry.setWidthChars(10);
        amount_Entry.setAlignment(1.0f);

        amount_Entry.connect(new Entry.CHANGED() {
            public void onChanged(Editable source) {
                /*
                 * "changed" signals will come in as a result of either user
                 * action or setText() on the Widget. In either case, after
                 * appropriate guards we parse the result by [trying to] set
                 * our Amount object.
                 */

                final String text = amount_Entry.getText();

                Debug.print("listeners", "in AmountEntry, Entry CHANGED, text: " + text);

                if (!amount_Entry.getHasFocus()) {
                    /*
                     * Then the change wasn't the result of a user action in
                     * this Widget, but rather as a result of some other logic
                     * element calling setText(). So, ignore the event by
                     * returning immediately.
                     */
                    Debug.print("listeners", "in AmountEntry, Entry CHANGED, ignoring not focused");
                    return;
                }
                if (text.equals("")) {
                    /*
                     * If we have an empty field, then don't do anything to
                     * the Amount we represent (leaving it at whatever was
                     * set). This also covers the case where changing the
                     * value results in a CHANGED event where the text is
                     * blank right before a CHANGED event where the text is
                     * the new value.
                     */
                    Debug.print("listeners", "in AmountEntry, Entry CHANGED, ignoring blank");
                    return;
                }

                Debug.print("listeners", "in AmountEntry, Entry CHANGED, parsing");

                try {
                    amount.setValue(text);
                    amount_Entry.modifyText(StateType.NORMAL, Color.BLACK);
                } catch (NumberFormatException nfe) {
                    /*
                     * if the user input is invalid, then ignore it. The
                     * Amount will stay as previously set.
                     */
                    amount_Entry.modifyText(StateType.NORMAL, Color.RED);
                    return;
                }

                if (changeListener != null) {
                    Debug.print("listeners", "in AmountEntry, Entry CHANGED, firing ChangeListener");
                    changeListener.userChangedData();
                }
            }
        });

        amount_Entry.connect(new Entry.ACTIVATE() {
            public void onActivate(Entry source) {
                /*
                 * Ensure the Entry shows the properly formatted Amount.
                 */
                final String text = amount.getValue();
                amount_Entry.setText(text);
                amount_Entry.setPosition(text.length());
            }
        });

        amount_Entry.connect(new Widget.FOCUS_OUT_EVENT() {
            public boolean onFocusOutEvent(Widget source, EventFocus event) {
                /*
                 * Ensure the Entry shows the properly formatted Amount.
                 */
                final String text = amount.getValue();
                amount_Entry.setText(text);
                /*
                 * It looks really stupid if a subset (or even all) of the
                 * characters are selected when focus leaves; it grays out and
                 * there is no reason to keep the visual reminder of the
                 * selection.
                 */
                amount_Entry.selectRegion(0, 0);
                amount_Entry.modifyText(StateType.NORMAL, Color.BLACK);

                return false;
            };
        });

        this.packStart(amount_Entry, false, false, 0);
    }

    /**
     * Add a use case specific listener to the Entry that underlies the
     * AmountEntry Widget. AmountEntry is largely built around an internal
     * EntryListener; however, this is not exposed as if you try to hookup
     * extra signals to its <code>CHANGED</code> event you'll have to
     * reimplement all its self-protection and anti-duplication logic.
     * 
     * <pre>
     * AmountEntry salaryDisplay;
     * Amount salary;
     * 
     * salaryDisplay.addListener(new ChangeListener() {
     *     public void userChangedData() {
     *         Amount a = salaryDisplay().getAmount();
     *         doSomething(a);
     *         // or if the object you constructed the Widget with is a instance field, just
     *         doSomething(salary);
     *     }
     * });
     * </pre>
     * 
     * Note that you can only call this once; only one GUI Window owns the
     * parent relationship to this Widget, and the whole point is to only have
     * one invokation of the callback to that Window's code.
     */
    public void addListener(ChangeListener listener) {
        if (changeListener != null) {
            throw new IllegalStateException(
                    "You can't have more than one ChangeListener on a Display Widget");
        }
        changeListener = listener;
    }

    /**
     * @return the Amount as currently held by this Display Widget. Note this
     *         is a live reference, not a copy!
     */
    public Amount getAmount() {
        return amount;
    }

    /**
     * Set the Amount object this Display Widget is representing. Note that
     * this really is the object you want to disaply and edit, not just the
     * value of the Amount. Updates the displayed value (but only if the value
     * string of the passed Amount differs from what is displayed, thus
     * avoiding triple taps on the <code>CHANGED</code> signal).
     */
    public void setAmount(Amount a) {
        if (a == null) {
            throw new IllegalArgumentException();
        }
        this.amount = a;
        final String str = a.getValue();
        if (!amount_Entry.getText().equals(str)) {
            amount_Entry.setText(str);
            amount_Entry.modifyText(StateType.NORMAL, Color.BLACK);
        }
    }

    /**
     * If you have a String that you want to use as the argument to
     * Amount.setValue(), pass it here.
     * 
     * @param value
     *            an Amount value in string form (ie, "120.99"). Will be
     *            validated by Amount.
     */
    public void setValue(String value) {
        amount.setValue(value);

        if (!amount_Entry.getText().equals(value)) {
            amount_Entry.setText(value);
            amount_Entry.modifyText(StateType.NORMAL, Color.BLACK);
        }
    }

    /*
     * Override inherited methods -------------------------
     */

    public void grabFocus() {
        amount_Entry.grabFocus();
    }
}
