/*
 * DatePicker.java
 * 
 * Copyright (c) 2005-2007 Operational Dynamics Consulting Pty Ltd
 * 
 * The code in this file, and the library it is a part of, are made available
 * to you by the authors under the terms of the "GNU General Public Licence,
 * version 2" See the LICENCE file for the terms governing usage and
 * redistribution.
 */
package accounts.ui;

import static org.freedesktop.bindings.Time.makeTime;
import generic.ui.ChangeListener;

import java.text.ParseException;

import org.gnome.gdk.Event;
import org.gnome.gdk.EventKey;
import org.gnome.gdk.Keyval;
import org.gnome.gtk.Button;
import org.gnome.gtk.Calendar;
import org.gnome.gtk.Editable;
import org.gnome.gtk.Entry;
import org.gnome.gtk.HBox;
import org.gnome.gtk.IconSize;
import org.gnome.gtk.Image;
import org.gnome.gtk.Label;
import org.gnome.gtk.Stock;
import org.gnome.gtk.Widget;
import org.gnome.gtk.Window;
import org.gnome.gtk.WindowPosition;

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

    private static Datestamp lastSelectedDate = null;

    static {
        lastSelectedDate = new Datestamp();
        lastSelectedDate.setAsToday();
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
        date = (Datestamp) lastSelectedDate.clone();

        entry = new Entry();
        entry.setWidthChars(9);
        entry.setText(date.toString());

        pick = new Button();
        Image icon = new Image(Stock.INDEX, IconSize.BUTTON);
        Label label = new Label("Pick");
        HBox box = new HBox(false, 1);

        box.packStart(icon, false, false, 0);
        box.packStart(label, false, false, 0);
        pick.add(box);

        packStart(entry, false, false, 0);
        packStart(pick, false, false, 0);

        pick.connect(new Button.CLICKED() {
            public void onClicked(Button source) {
                if (popup == null) {
                    popup = new DatePickerPopup();
                }
                popup.present();
            }
        });

        entry.connect(new Entry.CHANGED() {
            public void onChanged(Editable source) {
                try {
                    date.setDate(entry.getText());
                } catch (ParseException pe) {
                    return;
                }
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
     * A Window containing the Calendar Widget, and listeners to catch
     * appropriate keystrokes.
     */
    class DatePickerPopup
    {
        private Calendar calendar = null;

        private Window window;

        private DatePickerPopup() {
            window = new Window();
            window.setDecorated(false);

            /*
             * Not entirely necessary? Probably could use some better
             * positioning, perhaps so the north west corner is at the middle
             * of the Button or something. Center on mouse isn't bad, though.
             */
            window.setTransientFor((Window) entry.getToplevel());
            window.setPosition(WindowPosition.MOUSE);

            calendar = new Calendar();
            window.add(calendar);

            calendar.connect(new Calendar.DAY_SELECTED_DOUBLE_CLICK() {
                public void onDaySelectedDoubleClick(Calendar source) {
                    applySelection();
                }
            });

            window.connect(new Widget.KEY_RELEASE_EVENT() {
                public boolean onKeyReleaseEvent(Widget source, EventKey event) {
                    Keyval key = event.getKeyval();

                    if (key == Keyval.Escape) {
                        window.hide();
                        return true;
                    } else if (key == Keyval.Home || key == Keyval.t) {
                        date.setAsToday();
                        present();
                        return true;
                    } else if (key == Keyval.Return) {
                        applySelection();
                        return true;
                    } else {
                        // pass through the keystroke
                        return false;
                    }
                }
            });

            window.connect(new Widget.HIDE() {
                /*
                 * Raise the window that popped the picker. Given having added
                 * transient, is this necessary?
                 */
                public void onHide(Widget source) {
                    Window top = (Window) entry.getToplevel();
                    top.present();
                }
            });

            window.connect(new Window.DELETE_EVENT() {
                /*
                 * Only hide, don't destroy. More to the point, override the
                 * default return of false.
                 */
                public boolean onDeleteEvent(Widget source, Event event) {
                    window.hide();
                    return true;
                }
            });

            window.showAll();
        }

        private void applySelection() {
            final long seconds;

            window.hide();

            seconds = makeTime(calendar.getDateYear(), calendar.getDateMonth(), calendar.getDateDay(),
                    0, 0, 0);
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

        private void present() {
            /*
             * TODO there a better way to do this?
             */
            java.util.Calendar cal = java.util.Calendar.getInstance();
            cal.setTime(date.getDate());

            int day = cal.get(java.util.Calendar.DAY_OF_MONTH);
            int month = cal.get(java.util.Calendar.MONTH) + 1;
            int year = cal.get(java.util.Calendar.YEAR);

            /*
             * Set the month first, otherwise we get a glitch if the
             * previously set month doesn't have the requested day.
             */
            calendar.selectMonth(month, year);
            calendar.selectDay(day);

            window.present();
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
