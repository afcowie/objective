/*
 * CalendarToDatestampTest.java
 *
 * Copyright (c) 2007 Operational Dynamics Consulting Pty Ltd
 * 
 * The code in this file, and the library it is a part of, are made available
 * to you by the authors under the terms of the "GNU General Public Licence,
 * version 2" See the LICENCE file for the terms governing usage and
 * redistribution.
 */
package accounts.ui;

import static org.freedesktop.bindings.Time.makeTime;

import java.text.ParseException;

import org.gnome.gtk.Calendar;
import org.gnome.gtk.Gtk;

import accounts.domain.Datestamp;

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
        
        seconds = makeTime(calendar.getDateYear(), calendar.getDateMonth(), calendar.getDateDay(), 0, 0, 0);
        
        date = new Datestamp();
        date.setDate(seconds * 1000);
        
        assertEquals("25 Dec 07", date.toString());
    }
}
