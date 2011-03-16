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
package objective.ui;

import generic.ui.Text;

import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import objective.domain.Account;
import objective.domain.Amount;
import objective.domain.Credit;
import objective.domain.Currency;
import objective.domain.Datestamp;
import objective.domain.Debit;
import objective.domain.Entry;
import objective.domain.Ledger;
import objective.domain.ReimbursableTransaction;
import objective.domain.Transaction;
import objective.persistence.DataStore;
import objective.services.EntryComparator;
import objective.services.TransactionOperations;

import org.gnome.glib.Glib;
import org.gnome.gtk.CellRendererText;
import org.gnome.gtk.DataColumn;
import org.gnome.gtk.DataColumnBoolean;
import org.gnome.gtk.DataColumnLong;
import org.gnome.gtk.DataColumnReference;
import org.gnome.gtk.DataColumnString;
import org.gnome.gtk.ListStore;
import org.gnome.gtk.SelectionMode;
import org.gnome.gtk.TreeIter;
import org.gnome.gtk.TreePath;
import org.gnome.gtk.TreeRowReference;
import org.gnome.gtk.TreeSelection;
import org.gnome.gtk.TreeView;
import org.gnome.gtk.TreeViewColumn;
import org.gnome.pango.EllipsizeMode;

import static org.gnome.gtk.Alignment.LEFT;
import static org.gnome.gtk.Alignment.RIGHT;
import static org.gnome.gtk.Alignment.TOP;

/**
 * Summarize a List of {@link objective.domain.Transaction}s in
 * {@link org.gnome.gtk.TreeView TreeView} form. For each one, display its
 * tye, date, description and then for each {@link objective.domain.Entry}
 * within, display the {@link objective.domain.Account} and
 * {@link objective.domain.Ledger} to which the Entry belongs and the amount
 * of each one, presenting it in foreign currency terms.
 * 
 * <p>
 * Selecting and activating a row will cause the UI to launch an
 * {@linkplain TransactionEditorWindow editor window} suitable to modify the
 * Transaction.
 * 
 * @author Andrew Cowie
 */
public class TransactionListView extends TreeView
{
    private final DataStore data;

    private final DataColumnString typeTextColumn;

    private final DataColumnString typeSortColumn;

    private final DataColumnString dateTextColumn;

    private final DataColumnLong dateSortColumn;

    private final DataColumnString descriptionTextColumn;

    private final DataColumnString descriptionSortColumn;

    private final DataColumnString debitsTextColumn;

    private final DataColumnLong debitsSortColumn;

    private final DataColumnString creditsTextColumn;

    private final DataColumnLong creditsSortColumn;

    /**
     * The Transaction object that this row represents. This must be set
     * before calling {@link #populate(TreeIter)}
     */
    private final DataColumnReference<Transaction> transactionObjectColumn;

    /**
     * Whether the row is to be rendered with bright colours. Set this as true
     * if the row is selected and you want it to show up in "reverse video",
     * otherwise use false for normal colouring.
     */
    private final DataColumnBoolean isActiveColumn;

    private final ListStore model;

    private final TreeView view;

    /**
     * Instantiate a new widget to view a list of Transactions.
     * 
     * @param db
     *            an open DataClient from which the Transactions, Ledgers,
     *            parent Accounts, and Currency information are to be queried.
     * @param transactions
     *            the Transactions that you wish to display.
     */
    public TransactionListView(final DataStore data, final Transaction[] transactions) {
        super();

        TreeViewColumn vertical;
        CellRendererText renderer;

        this.data = data;

        typeTextColumn = new DataColumnString();
        typeSortColumn = new DataColumnString();
        dateTextColumn = new DataColumnString();
        dateSortColumn = new DataColumnLong();
        descriptionTextColumn = new DataColumnString();
        descriptionSortColumn = new DataColumnString();
        debitsTextColumn = new DataColumnString();
        debitsSortColumn = new DataColumnLong();
        creditsTextColumn = new DataColumnString();
        creditsSortColumn = new DataColumnLong();
        transactionObjectColumn = new DataColumnReference<Transaction>();
        isActiveColumn = new DataColumnBoolean();

        model = new ListStore(new DataColumn[] {
            typeTextColumn,
            typeSortColumn,
            dateTextColumn,
            dateSortColumn,
            descriptionTextColumn,
            descriptionSortColumn,
            debitsTextColumn,
            debitsSortColumn,
            creditsTextColumn,
            creditsSortColumn,
            transactionObjectColumn,
            isActiveColumn
        });

        populate(transactions);

        // since this might change away from a direct subclass
        view = this;

        view.setModel(model);

        /*
         * Type
         */

        vertical = view.appendColumn();
        vertical.setResizable(false);
        vertical.setReorderable(false);

        renderer = new CellRendererText(vertical);
        renderer.setAlignment(LEFT, TOP);
        renderer.setMarkup(typeTextColumn);

        vertical.setTitle("Type");
        vertical.setClickable(true);
        vertical.setSortColumn(typeSortColumn);

        /*
         * Date
         */
        vertical = view.appendColumn();
        vertical.setResizable(false);
        vertical.setReorderable(false);

        renderer = new CellRendererText(vertical);
        renderer.setAlignment(LEFT, TOP);
        renderer.setMarkup(dateTextColumn);

        vertical.setTitle("Date");
        vertical.setClickable(true);
        vertical.setSortColumn(dateSortColumn);
        vertical.emitClicked();

        /*
         * Description + Entries' parent Account » Ledger
         */
        vertical = view.appendColumn();
        vertical.setResizable(false);
        vertical.setReorderable(false);
        vertical.setExpand(true);

        renderer = new CellRendererText(vertical);
        renderer.setMarkup(descriptionTextColumn);
        renderer.setEllipsize(EllipsizeMode.NONE);

        vertical.setTitle("Description");
        vertical.setClickable(true);
        vertical.setSortColumn(descriptionSortColumn);

        // TODO sort order AccountComparator, yo

        /*
         * Entries' Debit
         */

        vertical = view.appendColumn();
        vertical.setResizable(false);
        vertical.setReorderable(false);

        renderer = new CellRendererText(vertical);
        renderer.setAlignment(RIGHT, TOP);
        renderer.setAlignment(org.gnome.pango.Alignment.RIGHT);
        renderer.setMarkup(debitsTextColumn);

        vertical.setTitle("    Debits");
        vertical.setAlignment(0.5f);
        vertical.setClickable(true);
        vertical.setSortColumn(debitsSortColumn);

        /*
         * Entries' Credit
         */

        vertical = view.appendColumn();
        vertical.setResizable(false);
        vertical.setReorderable(false);

        renderer = new CellRendererText(vertical);
        renderer.setAlignment(RIGHT, TOP);
        renderer.setAlignment(org.gnome.pango.Alignment.RIGHT);
        renderer.setMarkup(creditsTextColumn);

        vertical.setTitle("    Credits");
        vertical.setAlignment(0.0f);
        vertical.setClickable(true);
        vertical.setSortColumn(creditsSortColumn);

        /*
         * overall properties
         */

        view.setRulesHint(true);
        view.setEnableSearch(false);

        /*
         * repopulate [via showAsActive()] when a row is selected to cause
         * colours to adapt to better values appropriate being against the
         * selected row background colour.
         */

        final TreeSelection selection = view.getSelection();
        selection.setMode(SelectionMode.SINGLE);
        selection.connect(new TreeSelection.Changed() {
            public void onChanged(TreeSelection source) {
                final TreeIter row;

                row = source.getSelected();
                if (row != null) {
                    showAsActive(row);
                }
            }
        });

        view.connect(new TreeView.RowActivated() {
            public void onRowActivated(TreeView source, TreePath path, TreeViewColumn vertical) {
                final TreeIter row;
                final Transaction t;

                row = model.getIter(path);
                t = model.getValue(row, transactionObjectColumn);

                launchEditor(t);
            }
        });
    }

    /*
     * This used to live in a central place; perhaps it should go back one.
     */
    private void launchEditor(Transaction t) {
        final ReimbursableTransaction rt;
        final TransactionEditorWindow window;

        if (t instanceof ReimbursableTransaction) {
            rt = (ReimbursableTransaction) t;
            window = new ReimbursableExpensesEditorWindow(data, rt);
        } else {
            return;
        }

        window.present();

        window.connect(new TransactionEditorWindow.Updated() {
            public void onUpdated(Transaction updated) {
                TreeIter row;
                Transaction t;

                row = model.getIterFirst();
                do {
                    t = model.getValue(row, transactionObjectColumn);
                    if (updated == t) {
                        populate(row);
                        return;
                    }
                } while (row.iterNext());

                throw new AssertionError();
            }
        });
    }

    private static final String DARKGRAY = "darkgray";

    private static final String LIGHTGRAY = "lightgray";

    private static final String CHARCOAL = "#575757";

    /**
     * Load a Set of Transaction objects into the TreeModel underlying this
     * widget. This is called by both the constructor and when a new
     * assortment of Transactions is being updated into an existing widget.
     */
    private void populate(Transaction[] transactions) {
        for (Transaction t : transactions) {
            final TreeIter pointer = model.appendRow();

            /*
             * Populate is geared to be re-used and extracts its Transaction
             * from the DataColumnObject there. So in the case of setting up
             * the TransactionListView for the first time, set that DataColumn
             * before calling populate().
             */
            model.setValue(pointer, transactionObjectColumn, t);
            model.setValue(pointer, isActiveColumn, false);

            populate(pointer);
        }
    }

    /**
     * A stable reference to the previous row selected, if any. This allows
     * the TreeSelectionListener to restore the data in that row to "normal"
     * when the selection changes. Note that this is a TreeRowReference rather
     * than a TreePath so that it is stable across things that invalidate
     * TreeIters (sorting on a different column, for instance).
     */
    private TreeRowReference previous = null;

    /**
     * Set a given row of the ListStore as as "active" (ie, selected) by
     * repopulaing its DataColumns src/accounts/ui/TransactionListView.javato
     * have our _ACTIVE color values rather than _NORMAL ones. The row that
     * was previously active is set back to "normal".
     * 
     * @param path
     *            the TreePath (presumably extracted from a TreeSelection)
     *            that you want to indicate as active.
     */
    private void showAsActive(TreeIter pointer) {
        final TreePath current;

        model.setValue(pointer, isActiveColumn, true);
        populate(pointer);

        current = model.getPath(pointer);

        if (previous != null) {
            if (!(previous.getPath().equals(current))) {
                pointer = model.getIter(previous.getPath());
                model.setValue(pointer, isActiveColumn, false);
                populate(pointer);
            }
        }
        previous = new TreeRowReference(model, current);
    }

    /**
     * Populate a given row with data marked up for presentation and with meta
     * data normalized for sorting purposes.
     * 
     * @param row
     *            a TreeIter indicating which row you want to [re]populate.
     */
    /*
     * This is moderately hideous, but then what presentation code ever is NOT
     * ugly? Note that many of the Sort_DataColums fields are nice clean
     * Strings generally directly being the underlying text data, whereas the
     * Markup ones are where the pango mush goes (which is why we can't sort
     * on them - it'd sort by colour!)
     */
    private void populate(final TreeIter row) {
        final Transaction t;
        final boolean active;
        final StringBuilder debitVal, creditVal;
        final Set<Entry> ordered;
        final Iterator<Entry> eI;

        t = model.getValue(row, transactionObjectColumn);
        active = model.getValue(row, isActiveColumn);

        final StringBuffer type = new StringBuffer();

        if (active) {
            type.append("<span color='" + LIGHTGRAY + "'>");
        } else {
            type.append("<span color='" + CHARCOAL + "'>");
        }
        type.append(Text.wrap(t.getClassString(), 5));
        type.append("</span>");

        model.setValue(row, typeTextColumn, type.toString());
        model.setValue(row, typeSortColumn, t.getClassString());

        model.setValue(row, dateTextColumn, "<span font_desc='Mono'>"
                + Datestamp.dateToString(t.getDate()) + "</span>");
        model.setValue(row, dateSortColumn, t.getDate());

        final StringBuffer titleName = new StringBuffer();
        final String OPEN = "<b>";
        final String CLOSE = "</b>";
        titleName.append(OPEN);
        titleName.append(t.getDescription());
        titleName.append(CLOSE);

        debitVal = new StringBuilder();
        debitVal.append(OPEN);
        debitVal.append(' ');
        debitVal.append(CLOSE);
        debitVal.append('\n');

        creditVal = new StringBuilder();
        creditVal.append(OPEN);
        creditVal.append(' ');
        creditVal.append(CLOSE);
        creditVal.append('\n');

        /* ... in this Transaction */
        long largestDebitNumber = 0;
        long largestCreditNumber = 0;

        ordered = new TreeSet<Entry>(new EntryComparator(t));

        TransactionOperations transactions;
        Entry[] entries;

        transactions = new TransactionOperations(data);
        entries = transactions.findEntries(t);

        for (Entry e : entries) {
            ordered.add(e);
        }

        eI = ordered.iterator();
        while (eI.hasNext()) {
            final Entry entry = eI.next();

            final Ledger ledger = entry.getParentLedger();
            final Account account = ledger.getParentAccount();

            titleName.append("\n");

            titleName.append("<span color='");
            titleName.append(account.getColor(active));
            titleName.append("'>");
            titleName.append(Glib.markupEscapeText(account.getTitle()));
            titleName.append("</span>");
            /*
             * We use » \u00bb. Other possibilities: ∞ \u221e, and ⑆ \u2446.
             */
            if (active) {
                titleName.append("<span color='" + LIGHTGRAY + "'>");
            } else {
                titleName.append("<span color='" + CHARCOAL + "'>");
            }
            titleName.append(" \u00bb ");
            titleName.append("</span>");

            titleName.append("<span color='");
            titleName.append(ledger.getColor(active));
            titleName.append("'>");
            titleName.append(Glib.markupEscapeText(ledger.getName()));
            titleName.append("</span>");

            long amount = entry.getAmount();
            Currency cur = entry.getCurrency();

            final StringBuilder buf = new StringBuilder();

            buf.append(cur.getSymbol());
            // add , separators
            buf.append(Amount.padComma(Amount.numberToString(amount)));
            buf.append(' ');
            if (active) {
                buf.append("<span color='" + LIGHTGRAY + "'>");
            } else {
                buf.append("<span color='" + DARKGRAY + "'>");
            }
            buf.append(cur.getCode());
            buf.append("</span>");

            /*
             * We sort the entires by their face value.
             */

            if (entry instanceof Debit) {
                if (amount > largestDebitNumber) {
                    largestDebitNumber = amount;
                }
            } else if (entry instanceof Credit) {
                if (amount > largestCreditNumber) {
                    largestCreditNumber = amount;
                }
            }

            /*
             * And set it monospaced (though not terminal - is there a better
             * attribute to set?)
             */

            if (entry instanceof Debit) {
                debitVal.insert(0, "<span font_desc='mono' color='"
                        + (active ? Debit.COLOR_ACTIVE : Debit.COLOR_NORMAL) + "'>");
                debitVal.append(buf);
                debitVal.append("</span>");
            } else if (entry instanceof Credit) {
                creditVal.insert(0, "<span font_desc='mono' color='"
                        + (active ? Credit.COLOR_ACTIVE : Credit.COLOR_NORMAL) + "'>");
                creditVal.append(buf);
                creditVal.append("</span>");
            }

            if (eI.hasNext()) {
                debitVal.append('\n');
                creditVal.append('\n');
            }
        }

        /*
         * Now add the data for the Entries related columns:
         */

        model.setValue(row, descriptionTextColumn, titleName.toString());
        model.setValue(row, descriptionSortColumn, t.getDescription());

        model.setValue(row, debitsTextColumn, debitVal.toString());
        model.setValue(row, debitsSortColumn, largestDebitNumber);

        model.setValue(row, creditsTextColumn, creditVal.toString());
        model.setValue(row, creditsSortColumn, largestCreditNumber);
    }

    /**
     * Drop the existing Set of Transactions shown by this widget and replace
     * it with a new Set.
     */
    public void setTransactions(Transaction[] transactions) {
        model.clear();
        populate(transactions);
    }

    /*
     * TODO reimplement
     */
    public void redisplayTransaction(long transactionId) {
        final Transaction t;
        final TreeIter pointer;

        t = data.lookupTransaction(transactionId);

        pointer = model.getIterFirst();
        if (pointer == null) {
            return;
        }
        do {
            if (model.getValue(pointer, transactionObjectColumn) == t) {
                populate(pointer);
                return;
            }
        } while (pointer.iterNext());

        /*
         * Well, not here. No biggie, unless we start registering ID to Window
         * mappings up at UserInterface.
         */
    }
}
