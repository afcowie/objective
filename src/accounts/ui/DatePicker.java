/*
 * DatePicker.java
 * 
 * See LICENCE file for usage and redistribution terms
 * Copyright (c) 2005-2006 Operational Dynamics
 */
package accounts.ui;

import static org.freedesktop.bindings.Time.makeTime;
import generic.ui.AbstractWindow;
import generic.ui.ChangeListener;

import java.text.ParseException;

import org.freedesktop.bindings.Time;
import org.gnome.gtk.Button;
import org.gnome.gtk.Calendar;
import org.gnome.gtk.Entry;
import org.gnome.gtk.HBox;
import org.gnome.gtk.Image;
import org.gnome.gtk.Label;
import org.gnome.gtk.Stock;
import org.gnome.gtk.Window;

import accounts.domain.Datestamp;

/**
 * A date picker widget, customized for use within the ObjectiveAccounts
 * application. Dates are converted to a Datestamp object which can be
 * queried.
 * 
 * @author Andrew Cowie
 */
public class DatePicker extends HBox
{
    private Datestamp date = null;

    private static Datestamp _lastSelectedDate = null;

    static {
        _lastSelectedDate = new Datestamp();
        _lastSelectedDate.setAsToday();
    }

    private Entry entry = null;

    private Button pick = null;

    private DatePickerPopup popup = null;

    private ChangeListener changeListener;

    /**
     * Instantiate a new DatePicker widget. The date will be (a clone of) the
     * last one selected by [another] DatePicker, or today if none has been
     * previously used.
     */
    public DatePicker() {
        super(false, 3);
        date = (Datestamp) _lastSelectedDate.clone();

        entry = new Entry();
        entry.setWidth(9);
        entry.setText(date.toString());

        // _pick = new Button("Pick", false);
        // _pick = new Button(new GtkStockItem("stock_calendar-view-month"));
        // GtkStockItem stock = new GtkStockItem("stock_calendar-view-month");
        // Image icon = new Image(stock, IconSize.BUTTON);
        // _pick = new Button("stock_calendar-view-month");

        pick = new Button();
        Image icon = new Image(Stock.INDEX, IconSize.BUTTON);
        Label label = new Label("Pick", false);
        HBox box = new HBox(false, 1);

        box.packStart(icon, false, false, 0);
        box.packStart(label, false, false, 0);
        pick.add(box);

        packStart(entry, false, false, 0);
        packStart(pick, false, false, 0);

        pick.connect(new Button.CLICKED() {
            public void onClicked(Button source) {
                if (popup == null) {
                    popup = new DatePickerPopup("datepicker", "share/DatePickerPopup.glade");
                }
                popup.present();
            }
        });

        entry.connect(new Entry.ACTIVATE() {
            public void onActivate(Entry source) {
                entry.setText(date.toString());
                entry.setPosition(9);

                if (changeListener != null) {
                    changeListener.userChangedData();
                }
            };
        });
    }

    /**
     * A Window (constructed from a glade file) containing the Calendar
     * Widget, and listeners to catch appropriate keystrokes.
     */
    class DatePickerPopup extends AbstractWindow
    {
        private Calendar calendar = null;

        private DatePickerPopup(String which, String filename) {
            super(which, filename);

            calendar = (Calendar) gladeParser.getWidget("calendar");
            calendar.connect(new Calendar.DAY_SELECTED_DOUBLE_CLICK() {
                public void onDaySelectedDoubleClick(Calendar source) {
                    applySelection();
                }
            });

            window.addListener(new KeyListener() {
                public boolean keyEvent(KeyEvent event) {
                    int key = event.getKeyval();
                    if (key == KeyValue.Escape) {
                        window.hide();
                        return true;
                    } else if (key == KeyValue.Home || key == KeyValue.t) {
                        date.setAsToday();
                        present();
                        return true;
                    } else if (key == KeyValue.Return) {
                        applySelection();
                        return true;
                    } else {
                        // pass through the keystroke
                        return false;
                    }
                }
            });
        }

        private void applySelection() {
            final long seconds;
            
            window.hide();
            
            seconds = makeTime(calendar.getDateYear(), calendar.getDateMonth(), calendar.getDateDay(), 0, 0, 0);
            try {
                date.setDate(seconds * 1000);
            } catch (ParseException pe) {
                return;
            }
            entry.setText(date.toString());

            if (changeListener != null) {
                changeListener.userChangedData();
            }
        }

        /*
         * Overrides of inherited methods -----------------
         */

        public void present() {
            java.util.Calendar cal = java.util.Calendar.getInstance();
            cal.setTime(date.getDate());

            int day = cal.get(java.util.Calendar.DAY_OF_MONTH);
            int month = cal.get(java.util.Calendar.MONTH);
            int year = cal.get(java.util.Calendar.YEAR);

            /*
             * Set the month first, otherwise we get a glitch if the
             * previously set month doesn't have the requested day.
             */
            calendar.selectMonth(month, year);
            calendar.selectDay(day);

            super.present();
        }

        /**
         * Raise the window that popped the picker.
         */
        protected void hideHook() {
            /*
             * So annoying. You'd think you should be able to cast from Widget
             * to Window here, but it blows ClassCastException.
             */
            Window top = (Window) entry.getToplevel();
            top.present();
        }

        /**
         * Only hide, don't destroy. More to the point, override the default
         * return of false.
         */
        public boolean deleteHook() {
            window.hide();
            return true;
        }
    }

    /**
     * Attach a ChangeListener to this DatePicker.
     * 
     * @see AmountEntry#addListener(ChangeListener) for a full description
     */
    public void addListener(ChangeListener listener) {
        if (changeListener != null) {
            throw new IllegalStateException(
                    "You can't have more than one ChangeListener on a DatePicker");
        }
        changeListener = listener;
    }

    /*
     * Getters and Setters --------------------------------
     */
    public Datestamp getDate() {
        return date;
    }

    public void setDate(Datestamp date) {
        this.date = date;
        entry.setText(date.toString());
        entry.setPosition(9);
    }
}
