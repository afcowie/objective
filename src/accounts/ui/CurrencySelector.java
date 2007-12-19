/*
 * CurrencySelector.java
 * 
 * See LICENCE file for usage and redistribution terms
 * Copyright (c) 2005-2006 Operational Dynamics
 */
package accounts.ui;

import generic.persistence.DataClient;

import java.util.Iterator;
import java.util.Set;

import org.gnome.gtk.CellRendererText;
import org.gnome.gtk.ComboBox;
import org.gnome.gtk.DataColumn;
import org.gnome.gtk.DataColumnReference;
import org.gnome.gtk.DataColumnString;
import org.gnome.gtk.ListStore;
import org.gnome.gtk.TreeIter;

import accounts.domain.Books;
import accounts.domain.Currency;

/**
 * A ComboBox which allows you to pick a Currency from the internal list of
 * initialized currencies. This is a ComboBox subclass; nicely, that means the
 * ComboBox addListener() methods are naturally exposed.
 * 
 * @author Andrew Cowie
 */
public class CurrencySelector extends ComboBox
{
    private DataColumnString codeDisplay_DataColumn;

    private DataColumnReference currencyObject_DataColumn;

    private ListStore listStore;

    /**
     * Instantiate a new CurrencySelector, specifying an open
     * {@link DataClient} from which the list of Currencies will be pulled.
     */
    public CurrencySelector(DataClient store) {
        super();

        /*
         * We go to the considerable effort of having a TreeModel here so that
         * we can store a reference to the Currency object that is being
         * picked.
         */
        codeDisplay_DataColumn = new DataColumnString();
        currencyObject_DataColumn = new DataColumnReference();

        DataColumn[] currencySelector_DataColumnArray = {
                codeDisplay_DataColumn, currencyObject_DataColumn
        };

        listStore = new ListStore(currencySelector_DataColumnArray);

        /*
         * populate
         */
        Books root = (Books) store.getRoot();

        Set currencies = root.getCurrencySet();
        Iterator iter = currencies.iterator();
        while (iter.hasNext()) {
            Currency cur = (Currency) iter.next();
            TreeIter pointer = listStore.appendRow();
            listStore.setValue(pointer, codeDisplay_DataColumn, cur.getCode());
            listStore.setValue(pointer, currencyObject_DataColumn, cur);
        }

        /*
         * Now the UI
         */
        this.setModel(listStore);

        CellRendererText code_CellRenderer = new CellRendererText(this);
        code_CellRenderer.setText(codeDisplay_DataColumn);
    }

    /**
     * @return the currently selected Currency object, or null if nothing is
     *         selected. (TODO TEST!)
     */
    public Currency getCurrency() {
        TreeIter pointer = this.getActiveIter();
        if (pointer == null) {
            return null;
        }
        return (Currency) listStore.getValue(pointer, currencyObject_DataColumn);
    }

    /**
     * Set the specified currency as active.
     * 
     * @param cur
     *            The Currency object to be set as active.
     * @throws IllegalArgumentException
     *             if you are so foolish as to tell it to select a Currency
     *             object which isn't in the system currency table.
     */
    public void setCurrency(Currency cur) {
        TreeIter pointer = listStore.getIterFirst();

        if (pointer == null) {
            throw new IllegalArgumentException(
                    "How did you manage to ask to activate a Currency object that isn't in the system?");
        }
        
        do {
            if (listStore.getValue(pointer, currencyObject_DataColumn) == cur) {
                this.setActiveIter(pointer);
                return;
            }
        } while (pointer.iterNext());
    }
}
