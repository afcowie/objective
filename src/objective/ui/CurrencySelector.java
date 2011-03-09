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

import objective.domain.Currency;
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
public class CurrencySelector extends ComboBox
{
    private DataColumnString codeDisplayColumn;

    private DataColumnReference<Currency> currencyObjectColumn;

    private ListStore listStore;

    /**
     * Instantiate a new CurrencySelector, specifying the open database from
     * which the list of Currencies will be pulled.
     */
    public CurrencySelector(DataStore data) {
        super();

        final Currency[] currencies;
        TreeIter row;
        DataColumn[] columns;
        CellRendererText renderer;

        /*
         * We go to the considerable effort of having a TreeModel here so that
         * we can store a reference to the Currency object that is being
         * picked.
         */

        codeDisplayColumn = new DataColumnString();
        currencyObjectColumn = new DataColumnReference<Currency>();

        columns = new DataColumn[] {
            codeDisplayColumn,
            currencyObjectColumn
        };

        listStore = new ListStore(columns);

        /*
         * populate
         */

        currencies = data.listCurrencies();

        for (Currency currency : currencies) {
            row = listStore.appendRow();
            listStore.setValue(row, codeDisplayColumn, currency.getCode());
            listStore.setValue(row, currencyObjectColumn, currency);
        }

        /*
         * Now the UI
         */

        this.setModel(listStore);

        renderer = new CellRendererText(this);
        renderer.setText(codeDisplayColumn);
    }

    /**
     * @return the currently selected Currency object, or null if nothing is
     *         selected. (TODO TEST!)
     */
    public Currency getCurrency() {
        final TreeIter row;

        row = this.getActiveIter();
        if (row == null) {
            return null;
        }
        return listStore.getValue(row, currencyObjectColumn);
    }

    /**
     * Set the specified currency as active.
     * 
     * @param currency
     *            The Currency object to be set as active.
     * @throws IllegalArgumentException
     *             if you are so foolish as to tell it to select a Currency
     *             object which isn't in the system currency table.
     */
    public void setCurrency(Currency currency) {
        final TreeIter row;

        row = listStore.getIterFirst();

        if (row == null) {
            throw new IllegalArgumentException(
                    "How did you manage to ask to activate a Currency object that isn't in the system?");
        }

        do {
            if (listStore.getValue(row, currencyObjectColumn) == currency) {
                this.setActiveIter(row);
                return;
            }
        } while (row.iterNext());
    }
}
