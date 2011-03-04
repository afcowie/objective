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

import generic.client.Master;
import generic.ui.Text;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import objective.domain.Account;
import objective.domain.Amount;
import objective.domain.Credit;
import objective.domain.Currency;
import objective.domain.Debit;
import objective.domain.Entry;
import objective.domain.Ledger;
import objective.domain.Transaction;
import objective.persistence.DataStore;
import objective.services.TransactionOperations;

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

import accounts.services.EntryComparator;
import accounts.ui.TransactionEditorWindow;

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
    private transient Currency home = null;

    private final DataStore data;

    private final DataColumnString typeMarkup_DataColumn;

    private final DataColumnString typeSort_DataColumn;

    private final DataColumnString dateText_DataColumn;

    private final DataColumnLong dateSort_DataColumn;

    private final DataColumnString descriptionAccountLedgerText_DataColumn;

    private final DataColumnString descriptionSort_DataColumn;

    private final DataColumnString debitAmountsText_DataColumn;

    private final DataColumnLong debitAmountsSort_DataColumn;

    private final DataColumnString creditAmountsText_DataColumn;

    private final DataColumnLong creditAmountsSort_DataColumn;

    /**
     * The Transaction object that this row represents. This must be set
     * before calling {@link #populate(TreeIter)}
     */
    private final DataColumnReference<Transaction> transactionObject_DataColumn;

    /**
     * Whether the row is to be rendered with bright colours. Set this as true
     * if the row is selected and you want it to show up in "reverse video",
     * otherwise use false for normal colouring.
     */
    private final DataColumnBoolean active_DataColumn;

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

        home = null; // FIXME

        typeMarkup_DataColumn = new DataColumnString();
        typeSort_DataColumn = new DataColumnString();
        dateText_DataColumn = new DataColumnString();
        dateSort_DataColumn = new DataColumnLong();
        descriptionAccountLedgerText_DataColumn = new DataColumnString();
        descriptionSort_DataColumn = new DataColumnString();
        debitAmountsText_DataColumn = new DataColumnString();
        debitAmountsSort_DataColumn = new DataColumnLong();
        creditAmountsText_DataColumn = new DataColumnString();
        creditAmountsSort_DataColumn = new DataColumnLong();
        transactionObject_DataColumn = new DataColumnReference<Transaction>();
        active_DataColumn = new DataColumnBoolean();

        model = new ListStore(new DataColumn[] {
            typeMarkup_DataColumn,
            typeSort_DataColumn,
            dateText_DataColumn,
            dateSort_DataColumn,
            descriptionAccountLedgerText_DataColumn,
            descriptionSort_DataColumn,
            debitAmountsText_DataColumn,
            debitAmountsSort_DataColumn,
            creditAmountsText_DataColumn,
            creditAmountsSort_DataColumn,
            transactionObject_DataColumn,
            active_DataColumn
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
        renderer.setMarkup(typeMarkup_DataColumn);

        vertical.setTitle("Type");
        vertical.setClickable(true);
        vertical.setSortColumn(typeSort_DataColumn);

        /*
         * Date
         */
        vertical = view.appendColumn();
        vertical.setResizable(false);
        vertical.setReorderable(false);

        renderer = new CellRendererText(vertical);
        renderer.setAlignment(LEFT, TOP);
        renderer.setMarkup(dateText_DataColumn);

        vertical.setTitle("Date");
        vertical.setClickable(true);
        vertical.setSortColumn(dateSort_DataColumn);
        vertical.emitClicked();

        /*
         * Description + Entries' parent Account » Ledger
         */
        vertical = view.appendColumn();
        vertical.setResizable(false);
        vertical.setReorderable(false);
        vertical.setExpand(true);

        renderer = new CellRendererText(vertical);
        renderer.setMarkup(descriptionAccountLedgerText_DataColumn);

        vertical.setTitle("Description");
        vertical.setClickable(true);
        vertical.setSortColumn(descriptionSort_DataColumn);

        // TODO sort order AccountComparator, yo

        /*
         * Entries' Debit
         */
        vertical = view.appendColumn();
        vertical.setResizable(false);
        vertical.setReorderable(false);

        renderer = new CellRendererText(vertical);
        renderer.setAlignment(RIGHT, TOP);
        renderer.setMarkup(debitAmountsText_DataColumn);

        vertical.setTitle("Debits       ");
        vertical.setAlignment(1.0f);
        vertical.setClickable(true);
        vertical.setSortColumn(debitAmountsSort_DataColumn);

        /*
         * Entries' Credit
         */
        vertical = view.appendColumn();
        vertical.setResizable(false);
        vertical.setReorderable(false);

        renderer = new CellRendererText(vertical);
        renderer.setAlignment(RIGHT, TOP);
        renderer.setMarkup(creditAmountsText_DataColumn);

        vertical.setTitle("Credits    ");
        // Label title = new Label("Credits <span font_desc='Mono'> </span>");
        // title.setUseMarkup(true);
        // creditAmounts_ViewColumn.setWidget(title);

        vertical.setAlignment(1.0f);
        vertical.setClickable(true);
        vertical.setSortColumn(creditAmountsSort_DataColumn);

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
                final TreeIter pointer = model.getIter(path); // TODO CHECK
                final Transaction t = model.getValue(pointer, transactionObject_DataColumn);

                Master.ui.launchEditor(t);
            }
        });
    }

    private static final Pattern regexAmp = Pattern.compile("&");

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
            model.setValue(pointer, transactionObject_DataColumn, t);
            model.setValue(pointer, active_DataColumn, false);

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

        model.setValue(pointer, active_DataColumn, true);
        populate(pointer);

        current = model.getPath(pointer);

        if (previous != null) {
            if (!(previous.getPath().equals(current))) {
                pointer = model.getIter(previous.getPath());
                model.setValue(pointer, active_DataColumn, false);
                populate(pointer);
            }
        }
        previous = new TreeRowReference(model, current);
    }

    /**
     * Populate a given row with data marked up for presentation and with meta
     * data normalized for sorting purposes.
     * 
     * @param pointer
     *            a TreeIter indicating which row you want to [re]populate.
     */
    /*
     * This is moderately hideous, but then what presentation code ever is NOT
     * ugly? Note that many of the Sort_DataColums fields are nice clean
     * Strings generally directly being the underlying text data, whereas the
     * Markup ones are where the pango mush goes (which is why we can't sort
     * on them - it'd sort by colour!)
     */
    private void populate(final TreeIter pointer) {
        final Transaction t;
        final boolean active;
        final StringBuffer creditVal;
        final List<StringBuilder> amountBuffers;
        final List<Entry> entryObjects;

        t = model.getValue(pointer, transactionObject_DataColumn);
        active = model.getValue(pointer, active_DataColumn);

        final StringBuffer type = new StringBuffer();

        if (active) {
            type.append("<span color='" + LIGHTGRAY + "'>");
        } else {
            type.append("<span color='" + CHARCOAL + "'>");
        }
        type.append(Text.wrap(t.getClassString(), 5));
        type.append("</span>");

        model.setValue(pointer, typeMarkup_DataColumn, type.toString());
        model.setValue(pointer, typeSort_DataColumn, t.getClassString());

        model.setValue(pointer, dateText_DataColumn, "<span font_desc='Mono'>" + t.getDate().toString()
                + "</span>");
        model.setValue(pointer, dateSort_DataColumn, t.getDate().getInternalTimestamp());

        final StringBuffer titleName = new StringBuffer();
        final String OPEN = "<b>";
        final String CLOSE = "</b>";
        titleName.append(OPEN);
        titleName.append(t.getDescription());
        titleName.append(CLOSE);

        final StringBuffer debitVal = new StringBuffer();
        debitVal.append(OPEN);
        debitVal.append(' ');
        debitVal.append(CLOSE);
        debitVal.append('\n');

        creditVal = new StringBuffer(debitVal);

        /* ... in this Transaction */
        long largestDebitNumber = 0;
        long largestCreditNumber = 0;
        int widestDebitWidth = 0;
        int widestCreditWidth = 0;

        amountBuffers = new ArrayList<StringBuilder>(3);
        entryObjects = new ArrayList<Entry>(3);

        final Set<Entry> ordered = new TreeSet<Entry>(new EntryComparator(t));

        TransactionOperations transactions;
        Entry[] entries;

        transactions = new TransactionOperations(data);
        entries = transactions.findEntries(t);

        for (Entry e : entries) {
            ordered.add(e);
        }

        final Iterator eI = ordered.iterator();
        while (eI.hasNext()) {
            final Entry entry = (Entry) eI.next();
            final Ledger ledger = entry.getParentLedger();
            final Account account = ledger.getParentAccount();

            titleName.append("\n");

            titleName.append("<span color='");
            titleName.append(account.getColor(active));
            titleName.append("'>");
            final Matcher ma = regexAmp.matcher(account.getTitle());
            titleName.append(ma.replaceAll("&amp;"));
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
            final Matcher ml = regexAmp.matcher(ledger.getName());
            titleName.append(ml.replaceAll("&amp;"));
            titleName.append("</span>");

            long amount = entry.getAmount();
            Currency cur = entry.getCurrency();
            long value = entry.getValue();

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

            amountBuffers.add(buf);
            entryObjects.add(entry);

            /*
             * We sort the entires by their face value.
             */
            if (entry instanceof Debit) {
                if (amount > largestDebitNumber) {
                    largestDebitNumber = amount;
                }
                if (buf.length() > widestDebitWidth) {
                    widestDebitWidth = buf.length();
                }
            } else if (entry instanceof Credit) {
                if (amount > largestCreditNumber) {
                    largestCreditNumber = amount;
                }
                if (buf.length() > widestCreditWidth) {
                    widestCreditWidth = buf.length();
                }
            }
        }

        /*
         * Now we iterate over the Entries again, this time applying the max
         * width to square the Strings, and then copying them to the
         * appropriate left/right credit/debit buffer.
         */

        final int num = entryObjects.size();
        for (int i = 0; i < num; i++) {
            final Entry entry = entryObjects.get(i);
            final StringBuilder buf = amountBuffers.get(i);

            if (entry instanceof Debit) {
                final int diff = widestDebitWidth - buf.length();
                for (int j = 0; j < diff; j++) {
                    buf.insert(0, ' ');
                }
                debitVal.append(buf);
                debitVal.append('\n');
                creditVal.append('\n');

            } else if (entry instanceof Credit) {
                final int diff = widestCreditWidth - buf.length();
                for (int j = 0; j < diff; j++) {
                    buf.insert(0, ' ');
                }
                debitVal.append('\n');
                creditVal.append(buf);
                creditVal.append('\n');
            }
        }

        /*
         * Trim the trailing newline (otherwise we get a blank line in the
         * row)
         */
        debitVal.deleteCharAt(debitVal.length() - 1);
        creditVal.deleteCharAt(creditVal.length() - 1);

        /*
         * And set it monospaced (though not terminal - is there a better
         * attribute to set?)
         */
        debitVal.insert(0, "<span font_desc='mono' color='"
                + (active ? Debit.COLOR_ACTIVE : Debit.COLOR_NORMAL) + "'>");
        debitVal.append("</span>");
        creditVal.insert(0, "<span font_desc='mono' color='"
                + (active ? Credit.COLOR_ACTIVE : Credit.COLOR_NORMAL) + "'>");
        creditVal.append("</span>");

        /*
         * Now add the data for the Entries related columns:
         */
        model.setValue(pointer, descriptionAccountLedgerText_DataColumn, titleName.toString());
        model.setValue(pointer, descriptionSort_DataColumn, t.getDescription());

        model.setValue(pointer, debitAmountsText_DataColumn, debitVal.toString());
        model.setValue(pointer, debitAmountsSort_DataColumn, largestDebitNumber);

        model.setValue(pointer, creditAmountsText_DataColumn, creditVal.toString());
        model.setValue(pointer, creditAmountsSort_DataColumn, largestCreditNumber);
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
            if (model.getValue(pointer, transactionObject_DataColumn) == t) {
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
