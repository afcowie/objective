/*
 * ObjectiveAccounts, accounting for small professional services firms.
 *
 * Copyright © 2011 Operational Dynamics Consulting, Pty Ltd
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

import objective.domain.Tax;
import objective.persistence.DataStore;

import org.gnome.gtk.CellRendererText;
import org.gnome.gtk.ComboBox;
import org.gnome.gtk.DataColumn;
import org.gnome.gtk.DataColumnReference;
import org.gnome.gtk.DataColumnString;
import org.gnome.gtk.ListStore;
import org.gnome.gtk.TreeIter;

/**
 * A ComboBox which allows you to pick a Currency from the internal list of
 * initialized currencies. This is a ComboBox subclass; nicely, that means the
 * ComboBox connect() methods are naturally exposed.
 * 
 * @author Andrew Cowie
 */
// from CurrencySelector
public class TaxSelector extends ComboBox
{
    private DataColumnString codeDisplayColumn;

    private DataColumnReference<Tax> taxObjectColumn;

    private ListStore listStore;

    /**
     * Instantiate a new CurrencySelector, specifying the open database from
     * which the list of Currencies will be pulled.
     */
    public TaxSelector(DataStore data) {
        super();

        final Tax[] codes;
        TreeIter row;
        DataColumn[] columns;
        CellRendererText renderer;

        codeDisplayColumn = new DataColumnString();
        taxObjectColumn = new DataColumnReference<Tax>();

        columns = new DataColumn[] {
            codeDisplayColumn,
            taxObjectColumn,
        };

        listStore = new ListStore(columns);

        /*
         * populate
         */

        // FIXME move to database!
        codes = new Tax[] {
            new Tax("GST", "Goods and Services Tax", 0.1),
            new Tax("N/A", "Not Applicable", 0.0),
            new Tax("Cap", "Capital Expenditure", 0.1),
            new Tax("Exp", "Export", 0.0),
            new Tax("Fre", "Tax Free", 0.0),
        };

        for (Tax tax : codes) {
            row = listStore.appendRow();
            listStore.setValue(row, codeDisplayColumn, tax.getCode());
            listStore.setValue(row, taxObjectColumn, tax);
        }

        /*
         * Now the UI
         */

        this.setModel(listStore);

        renderer = new CellRendererText(this);
        renderer.setText(codeDisplayColumn);

        this.setActive(1);
    }

    /**
     * Get the currently selected tax code.
     */
    public Tax getCode() {
        final TreeIter row;

        row = this.getActiveIter();
        if (row == null) {
            return null;
        }
        return listStore.getValue(row, taxObjectColumn);
    }

    public void setCode(String code) {
        final TreeIter row;
        Tax tag;

        row = listStore.getIterFirst();

        if (row == null) {
            throw new AssertionError();
        }

        do {
            tag = listStore.getValue(row, taxObjectColumn);
            if (tag.getCode().equals(code)) {
                this.setActiveIter(row);
                return;
            }
        } while (row.iterNext());

        throw new AssertionError();
    }
}
