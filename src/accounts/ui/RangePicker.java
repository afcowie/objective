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
package accounts.ui;

import objective.domain.Datestamp;
import objective.ui.DatePicker;

import org.gnome.gtk.HBox;
import org.gnome.gtk.Label;

import generic.ui.Align;
import generic.ui.ChangeListener;
import generic.ui.TwoColumnTable;

import accounts.services.RangeCalculator;

/**
 * At present, only exposes the start and end Datestamp as currently selected.
 * 
 * @author Andrew Cowie
 */
public class RangePicker extends HBox
{
    private transient RangeCalculator calc = null;

    private DatePicker startDate_Picker;

    private DatePicker endDate_Picker;

    private ChangeListener changeListener = null;

    public RangePicker() {
        super(false, 0);

        final TwoColumnTable table = new TwoColumnTable(2);

        final Label startDate_Label = new Label("From...");
        startDate_Label.setAlignment(0.0f, 0.5f);
        table.attach(startDate_Label, Align.LEFT);

        final Label endDate_Label = new Label("Through...");
        endDate_Label.setAlignment(0.0f, 0.5f);
        table.attach(endDate_Label, Align.RIGHT);

        startDate_Picker = new DatePicker();
        table.attach(startDate_Picker, Align.LEFT);

        endDate_Picker = new DatePicker();
        table.attach(endDate_Picker, Align.RIGHT);

        this.packStart(table, true, false, 0);

        /*
         * Now the code to handle changing data. Very simply, we propegate the
         * fact of a change upstream through our own ChangeListener. The
         * Window using this Widget can then either get the RangeCalculator,
         * or directly use the start and end Datestamps (which it may well
         * already have).
         */

        calc = new RangeCalculator();
        calc.setStartDate(startDate_Picker.getDate());
        calc.setEndDate(endDate_Picker.getDate());

        ChangeListener dateChanged = new ChangeListener() {
            public void userChangedData() {
                // System.out.println(calc.calculateDays() + " days, " +
                // calc.calculateWeeks() + " weeks");
                changeListener.userChangedData();
            }
        };

        startDate_Picker.addListener(dateChanged);
        endDate_Picker.addListener(dateChanged);
    }

    /**
     * Get the Datestamp reresenting the beginning of the selected date range
     */
    public Datestamp getStartDate() {
        return startDate_Picker.getDate();
    }

    public void setStartDate(Datestamp startDate) {
        if (startDate == null) {
            throw new IllegalArgumentException();
        }
        startDate_Picker.setDate(startDate);
        calc.setStartDate(startDate);
    }

    /**
     * Get the Datestamp reresenting the end of the selected date range
     */
    public Datestamp getEndDate() {
        return endDate_Picker.getDate();
    }

    public void setEndDate(Datestamp endDate) {
        if (endDate == null) {
            throw new IllegalArgumentException();
        }
        endDate_Picker.setDate(endDate);
        calc.setEndDate(endDate);
    }

    /**
     * Get a RangeCalculator initialized with this picker's start and end
     * Datestamps. You can then use it's utility functions to calculate the
     * length of the range in various useful scales
     */
    public RangeCalculator getRangeCalculator() {
        return calc;
    }

    public void addListener(ChangeListener listener) {
        if (changeListener != null) {
            throw new IllegalStateException(
                    "You can't have more than one ChangeListener on a Display Widget");
        }
        changeListener = listener;
    }
}
