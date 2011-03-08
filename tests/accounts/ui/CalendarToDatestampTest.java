/*
 * ObjectiveAccounts, accounting for small professional services firms.
 *
 * Copyright © 2007-2011 Operational Dynamics Consulting, Pty Ltd
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
package accounts.ui;

import static org.freedesktop.bindings.Time.makeTime;

import java.text.ParseException;

import objective.domain.Datestamp;

import org.gnome.gtk.Calendar;
import org.gnome.gtk.Gtk;


import junit.framework.TestCase;

public class CalendarToDatestampTest extends TestCase
{
    public final void testCalendarToDatestamp() throws ParseException {
        final Calendar calendar;
        final Datestamp date;
        final long seconds;

        Gtk.init(null);
        calendar = new Calendar();

        calendar.selectMonth(12, 2007);
        calendar.selectDay(25);

        seconds = makeTime(calendar.getDateYear(), calendar.getDateMonth(), calendar.getDateDay(), 0, 0,
                0);

        date = new Datestamp();
        date.setDate(seconds * 1000);

        assertEquals("25 Dec 07", date.toString());
    }
}
