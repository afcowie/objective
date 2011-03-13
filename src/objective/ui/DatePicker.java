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

import java.text.ParseException;

import objective.domain.Datestamp;

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

import static org.freedesktop.bindings.Time.makeTime;

/**
 * A date picker widget, customized for use within the ObjectiveAccounts
 * application. Dates are converted to a Datestamp object which can be
 * queried.
 * 
 * @author Andrew Cowie
 */
public class DatePicker extends HBox
{
    private long datestamp;

    private static long lastSelectedDatestamp;

    static {
        lastSelectedDatestamp = Datestamp.getToday();
    }

    private Entry entry = null;

    private Button pick = null;

    private DatePickerPopup popup = null;

    private DatePicker.Updated handler;

    /**
     * Instantiate a new DatePicker widget. The date will be (a clone of) the
     * last one selected by [another] DatePicker, or today if none has been
     * previously used.
     */
    public DatePicker() {
        super(false, 3);

        datestamp = lastSelectedDatestamp;

        entry = new Entry();
        entry.setWidthChars(9);
        entry.setText(Datestamp.dateToString(datestamp));

        pick = new Button();
        Image icon = new Image(Stock.INDEX, IconSize.BUTTON);
        Label label = new Label("Pick");
        HBox box = new HBox(false, 1);

        box.packStart(icon, false, false, 0);
        box.packStart(label, false, false, 0);
        pick.add(box);

        packStart(entry, false, false, 0);
        packStart(pick, false, false, 0);

        pick.connect(new Button.Clicked() {
            public void onClicked(Button source) {
                if (popup == null) {
                    popup = new DatePickerPopup();
                }
                popup.present();
            }
        });

        entry.connect(new Editable.Changed() {
            public void onChanged(Editable source) {
                final String str;
                try {
                    str = entry.getText();
                    datestamp = Datestamp.stringToDate(str, lastSelectedDatestamp);
                } catch (ParseException pe) {
                    return;
                }
            }
        });

        entry.connect(new Entry.Activate() {
            public void onActivate(Entry source) {
                final String str;

                str = Datestamp.dateToString(datestamp);
                entry.setText(str);
                entry.setPosition(9);

                lastSelectedDatestamp = datestamp;

                if (handler != null) {
                    handler.onUpdated(datestamp);
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

            calendar.connect(new Calendar.DaySelectedDoubleClick() {
                public void onDaySelectedDoubleClick(Calendar source) {
                    applySelection();
                }
            });

            window.connect(new Widget.KeyReleaseEvent() {
                public boolean onKeyReleaseEvent(Widget source, EventKey event) {
                    Keyval key = event.getKeyval();

                    if (key == Keyval.Escape) {
                        window.hide();
                        return true;
                    } else if ((key == Keyval.Home) || (key == Keyval.t)) {
                        datestamp = Datestamp.getToday();
                        setDate(datestamp);
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

            window.connect(new Widget.Hide() {
                /*
                 * Raise the window that popped the picker. Given having added
                 * transient, is this necessary?
                 */
                public void onHide(Widget source) {
                    Window top = (Window) entry.getToplevel();
                    top.present();
                }
            });

            window.connect(new Window.DeleteEvent() {
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
            final String str;

            window.hide();

            seconds = makeTime(calendar.getDateYear(), calendar.getDateMonth(), calendar.getDateDay(),
                    0, 0, 0);

            /*
             * Do a round trip to ensure it's rounded to Zulu day
             */

            str = Datestamp.dateToString(seconds);
            try {
                datestamp = Datestamp.stringToDate(str);
            } catch (ParseException e) {
                throw new AssertionError();
            }

            entry.setText(str);

            if (handler != null) {
                handler.onUpdated(datestamp);
            }
        }

        private void present() {
            /*
             * TODO there a better way to do this?
             */
            java.util.Calendar cal = java.util.Calendar.getInstance();
            cal.setTimeInMillis(datestamp * 1000);

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
     * Connect an DatePicker.Updated handler.
     */
    public void connect(DatePicker.Updated handler) {
        if (this.handler != null) {
            throw new IllegalStateException("You can't have more than one Updated handler");
        }
        this.handler = handler;
    }

    public long getDate() {
        return datestamp;
    }

    public void setDate(long datestamp) {
        this.datestamp = datestamp;
        entry.setText(Datestamp.dateToString(datestamp));
        entry.setPosition(9);
    }

    public interface Updated
    {
        public void onUpdated(long datestamp);
    }
}
