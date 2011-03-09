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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import objective.domain.Account;
import objective.domain.Ledger;
import objective.persistence.DataStore;

import org.gnome.gdk.Event;
import org.gnome.gdk.EventFocus;
import org.gnome.gdk.EventKey;
import org.gnome.gdk.Keyval;
import org.gnome.glib.Glib;
import org.gnome.gtk.Alignment;
import org.gnome.gtk.Allocation;
import org.gnome.gtk.Button;
import org.gnome.gtk.CellRendererText;
import org.gnome.gtk.DataColumn;
import org.gnome.gtk.DataColumnReference;
import org.gnome.gtk.DataColumnString;
import org.gnome.gtk.Editable;
import org.gnome.gtk.Entry;
import org.gnome.gtk.HBox;
import org.gnome.gtk.ListStore;
import org.gnome.gtk.PolicyType;
import org.gnome.gtk.ReliefStyle;
import org.gnome.gtk.ScrolledWindow;
import org.gnome.gtk.SelectionMode;
import org.gnome.gtk.Statusbar;
import org.gnome.gtk.TreeIter;
import org.gnome.gtk.TreeModel;
import org.gnome.gtk.TreeModelFilter;
import org.gnome.gtk.TreeModelSort;
import org.gnome.gtk.TreePath;
import org.gnome.gtk.TreeSelection;
import org.gnome.gtk.TreeView;
import org.gnome.gtk.TreeViewColumn;
import org.gnome.gtk.VBox;
import org.gnome.gtk.Widget;
import org.gnome.gtk.Window;

/**
 * A widget to select an Account / Ledger combination. AccountLedgerPicker
 * presents itself as a button containing a Label presenting the Account and
 * Ledger in the appropriate Debit or Credit colour. When activated, a quick
 * Window pops up displaying full the list of Accounts and Ledgers, allowing
 * the user to select the one they are interested in. The user can narrow the
 * search with any characters entered and the search text will be used to
 * filter both Account titles and Ledger names.
 * 
 * @author Andrew Cowie
 * @see objective.ui.AccountLedgerDisplay which is used to render the label on
 *      the main button indicating the selected Account/Ledger pair.
 */
public class AccountLedgerPicker extends HBox
{
    private Account selectedAccount = null;

    private Ledger selectedLedger = null;

    private AccountLedgerDisplay display = null;

    private Button wide = null;

    private AccountPickerPopup popup = null;

    private final DataStore data;

    private static final Pattern regexAtoZ;

    static {
        regexAtoZ = Pattern.compile("[a-z0-9]");
    }

    /**
     * Instantiate a new AccountLedgerPicker widget.
     * 
     * @param store
     *            an open DataClient connection that will be used to pull the
     *            list Accounts and Ledgers.
     */
    public AccountLedgerPicker(DataStore data) {
        super(false, 3);

        this.data = data;

        display = new AccountLedgerDisplay();
        display.setSizeRequest(240, -1);

        /*
         * This box is a spacer so that the EventBox is as wide as the widget
         * up to but not including the pick button.
         */

        wide = new Button();
        wide.add(display);
        wide.setRelief(ReliefStyle.NORMAL);

        popup = new AccountPickerPopup();

        wide.connect(new Button.Clicked() {
            public void onClicked(Button source) {
                popup.present();
            }
        });

        this.packStart(wide, true, true, 0);
    }

    /**
     * A Window containing the list of Accounts and an Entry box, and
     * listeners to catch appropriate keystrokes. This is an inner class of
     * AccountLedgerPicker.
     */
    /*
     * It's a bit of a monster, this class.
     */
    class AccountPickerPopup
    {
        private ListStore listStore;

        private TreeModelFilter filteredStore;

        private TreeModelSort sortedStore;

        private DataColumnString accountDisplayColumn;

        private DataColumnString accountTextColumn;

        private DataColumnReference<Account> accountObjectColumn;

        private DataColumnString ledgerDisplayColumn;

        private DataColumnString ledgerTextColumn;

        private DataColumnReference<Ledger> ledgerObjectColumn;

        private final Window window;

        private final VBox top;

        private final ScrolledWindow scroll;

        private final TreeView view;

        private final Entry search;

        private int visibleRows;

        private int totalRows;

        private final Statusbar status;

        AccountPickerPopup() {
            super();
            final DataColumn[] columns;
            TreeViewColumn vertical;
            CellRendererText text;

            window = new Window();

            /*
             * Setup the underlying TreeModel. Each viewable column has three
             * DataColumn objects underlying it. The "Display" one holds an
             * extensively composite marked up version that is what is
             * actually displayed. "Text" is the straight up String version of
             * the data, and is used for sorting and filtering. And the
             * "Object" is the reference back to our domain object model - the
             * thing we're actually trying to pick in the first place.
             */

            accountDisplayColumn = new DataColumnString();
            accountTextColumn = new DataColumnString();
            accountObjectColumn = new DataColumnReference<Account>();

            ledgerDisplayColumn = new DataColumnString();
            ledgerTextColumn = new DataColumnString();
            ledgerObjectColumn = new DataColumnReference<Ledger>();

            columns = new DataColumn[] {
                accountDisplayColumn,
                accountTextColumn,
                accountObjectColumn,
                ledgerDisplayColumn,
                ledgerTextColumn,
                ledgerObjectColumn
            };

            listStore = new ListStore(columns);

            filteredStore = new TreeModelFilter(listStore, null);

            sortedStore = new TreeModelSort(filteredStore);

            view = new TreeView(sortedStore);

            // Account column
            vertical = view.appendColumn();
            vertical.setResizable(true);
            vertical.setReorderable(false);

            text = new CellRendererText(vertical);
            text.setMarkup(accountDisplayColumn);

            vertical.setTitle("Account");
            vertical.setClickable(true);
            vertical.setSortColumn(accountTextColumn);

            // Ledger column
            vertical = view.appendColumn();
            vertical.setResizable(true);
            vertical.setReorderable(false);

            text = new CellRendererText(vertical);
            text.setMarkup(ledgerDisplayColumn);

            vertical.setTitle("Ledger");
            vertical.setClickable(true);
            vertical.setSortColumn(ledgerTextColumn);

            /*
             * overall properties
             */

            view.setRulesHint(false);
            view.setEnableSearch(false);
            view.setReorderable(false);

            final TreeSelection selection = view.getSelection();
            selection.setMode(SelectionMode.SINGLE);

            view.connect(new Widget.FocusInEvent() {
                public boolean onFocusInEvent(Widget source, EventFocus event) {
                    TreeIter row;

                    row = selection.getSelected();
                    if (row == null) {
                        row = sortedStore.getIterFirst();

                        if (row != null) {
                            selection.selectRow(row);
                        }
                    }
                    return false;
                }
            });

            /*
             * Populate
             */

            final Ledger[] ledgers;
            String str;
            Account parent;
            TreeIter row;

            ledgers = data.listLedgers();

            for (Ledger ledger : ledgers) {
                row = listStore.appendRow();

                /*
                 * Eek! Deeply burried presentation code. Alas. Well, it's
                 * important that you have the same size-ness for both Account
                 * and Ledger.
                 */
                final String size = "xx-small";

                parent = ledger.getParentAccount();
                str = Glib.markupEscapeText(parent.getTitle());

                listStore.setValue(row, accountDisplayColumn, str + "\n<span size=\"" + size
                        + "\" color=\"" + parent.getColor(false) + "\">" + parent.getClassString()
                        + "</span>");
                listStore.setValue(row, accountTextColumn, parent.getTitle());
                listStore.setValue(row, accountObjectColumn, parent);

                str = Glib.markupEscapeText(ledger.getName());

                listStore.setValue(row, ledgerDisplayColumn, str + "\n<span size=\"" + size
                        + "\"color=\"" + ledger.getColor(false) + "\">" + ledger.getClassString()
                        + "</span>");
                listStore.setValue(row, ledgerTextColumn, ledger.getName());
                listStore.setValue(row, ledgerObjectColumn, ledger);
            }

            /*
             * Setup the search / filter mechanism.
             */

            search = new Entry();

            /*
             * And, for visual effect, a statusbar showing what completions
             * are available.
             */

            status = new Statusbar();

            /*
             * With the main Widgets constructed, pack the elements together
             * to form the popup window.
             */

            top = new VBox(false, 0);

            top.packStart(search, false, false, 0);

            view.setSizeRequest(-1, 210);
            scroll = new ScrolledWindow();
            scroll.setPolicy(PolicyType.NEVER, PolicyType.ALWAYS);
            scroll.add(view);
            top.packStart(scroll, true, true, 0);

            top.packEnd(status, false, false, 0);

            window.add(top);
            window.setDecorated(false);

            /*
             * Signal handlers and Callbacks
             */

            filteredStore.setVisibleCallback(new TreeModelFilter.Visible() {
                private Pattern regex = null;

                private String cached = "bleep";

                public boolean onVisible(TreeModelFilter source, TreeModel model, TreeIter pointer) {
                    Matcher m;
                    String q, title, name;

                    q = search.getText();

                    /*
                     * This is ugly, but since we had to do it this way to get
                     * at a case insensitive regex, we get a way to cache the
                     * pattern as compiling them is often expensive.
                     */

                    if (!cached.equals(q)) {
                        regex = Pattern.compile(".*" + q + ".*", Pattern.CASE_INSENSITIVE);
                        cached = q;
                    }

                    title = model.getValue(pointer, accountTextColumn);
                    m = regex.matcher(title);
                    if (m.matches()) {
                        return true;
                    }

                    name = model.getValue(pointer, ledgerTextColumn);
                    m = regex.matcher(name);
                    if (m.matches()) {
                        return true;
                    }

                    // otherwise, don't show the row.
                    return false;
                }
            });

            /*
             * and now we can add the mechanism to refilter on keystrokes.
             */
            search.connect(new Editable.Changed() {
                public void onChanged(Editable source) {
                    refilter();
                }
            });

            search.connect(new Entry.Activate() {
                public void onActivate(Entry source) {
                    view.grabFocus();
                }
            });

            /*
             * If in the entry and you press down, jump straight to the rows
             * (skipping the headers).
             */
            search.connect(new Widget.KeyPressEvent() {
                public boolean onKeyPressEvent(Widget source, EventKey event) {
                    final Keyval key;

                    key = event.getKeyval();
                    if ((key == Keyval.Down) || (key == Keyval.Up)) {
                        view.grabFocus();
                        return true;
                    } else {
                        return false;
                    }
                }
            });

            /*
             * As much as possible, make the cursor go to the end (and
             * deselect all the text in the process - otherwise you get an
             * overwrite)
             */
            search.connect(new Widget.FocusInEvent() {
                public boolean onFocusInEvent(Widget source, EventFocus event) {
                    search.setPosition(search.getText().length());
                    return false;
                }
            });

            window.connect(new Widget.KeyPressEvent() {
                public boolean onKeyPressEvent(Widget source, EventKey event) {
                    if (event.getKeyval() == Keyval.Escape) {
                        if (selectedAccount == null) {
                            clearEntry();
                        }
                        window.hide();
                        clearSearch();
                        return true;
                    } else {
                        return false;
                    }
                }
            });

            view.connect(new Widget.KeyPressEvent() {
                public boolean onKeyPressEvent(Widget source, EventKey event) {
                    final Keyval key;
                    final char ch;
                    final String str, orig;

                    key = event.getKeyval();
                    ch = key.toUnicode();
                    str = Character.toString(ch);

                    orig = search.getText();
                    int len = orig.length();

                    if (key == Keyval.BackSpace) {
                        search.grabFocus();
                        if (len > 0) {
                            search.setText(orig.substring(0, len - 1));
                            refilter();
                        }
                        return true;
                    } else if (key == Keyval.Left) {
                        search.grabFocus();
                        if (len > 0) {
                            search.setPosition(len - 1);
                        }
                        return true;
                    } else if (key == Keyval.Right) {
                        search.grabFocus();
                        search.setPosition(len);
                        return true;
                    } else if (regexAtoZ.matcher(str).matches()) {
                        search.grabFocus();
                        search.setText(search.getText() + str);
                        search.setPosition(-1);
                        refilter();
                        return true;
                    } else {
                        return false;
                    }
                }
            });

            view.connect(new TreeView.RowActivated() {
                public void onRowActivated(TreeView source, TreePath path, TreeViewColumn vertical) {
                    TreeIter pointer = source.getModel().getIter(path);
                    applySelection(pointer);
                }
            });

            window.connect(new Window.DeleteEvent() {
                public boolean onDeleteEvent(Widget source, Event event) {
                    window.hide();
                    clearSearch();
                    Window parentTop = (Window) wide.getToplevel();
                    parentTop.present();
                    return true;
                }
            });
        }

        /**
         * This gets called from a few places; basically its nothing more than
         * a bit of prettiness around {@link TreeModelFilter#refilter()}. The
         * reason this UI element is here at all is that sometimes, especially
         * after a first keystroke, the list isn't appreciably narrowed and so
         * without some feedback that "something is happening" the user can be
         * a bit disoriented.
         * <P>
         * It was tempting to cache some of this, total row count at least,
         * but there is the case that maybe an Account or Ledger has been
         * added. I'd rather have this code here now that try to figure out
         * where the hell an annoying "not keeping up" bug is coming from in
         * 18 months.
         */
        private void refilter() {
            TreeIter row;
            totalRows = 0;

            row = listStore.getIterFirst();
            if (row != null) {
                do {
                    totalRows++;
                } while (row.iterNext());
            }

            filteredStore.refilter();

            visibleRows = 0;

            row = filteredStore.getIterFirst();
            if (row != null) {
                do {
                    visibleRows++;
                } while (row.iterNext());
            }

            status.setMessage(visibleRows + "/" + totalRows + " visible");
        }

        private void applySelection(TreeIter row) {
            window.hide();
            selectedAccount = sortedStore.getValue(row, accountObjectColumn);
            selectedLedger = sortedStore.getValue(row, ledgerObjectColumn);
            setDisplayText();
        }

        /**
         * Select the line containing the indicated Ledger (and hence parent
         * Account) in the TreeView
         */
        private void setSelection(Ledger ledger) {
            TreeIter pointer;

            pointer = sortedStore.getIterFirst();
            do {
                Ledger l = sortedStore.getValue(pointer, ledgerObjectColumn);

                if (ledger == l) {
                    view.getSelection().selectRow(pointer);
                    return;
                }
            } while (pointer.iterNext());
        }

        private void clearEntry() {
            selectedAccount = null;
            selectedLedger = null;
        }

        private void setSearch(String str) {
            search.setText(str);
            refilter();
        }

        private void clearSearch() {
            search.setText("");
        }

        /*
         * Overrides of inherited methods -----------------
         */

        void present() {
            final org.gnome.gdk.Window underlying;
            final Allocation alloc;
            final int x, y;
            final TreeIter selected;
            final TreePath path;

            underlying = wide.getWindow();
            alloc = wide.getAllocation();

            x = underlying.getPositionX() + alloc.getX();
            y = underlying.getPositionY() + alloc.getY();

            window.move(x, y);

            window.showAll();
            window.present();

            selected = view.getSelection().getSelected();
            if (selected != null) {
                path = sortedStore.getPath(selected);
                view.scrollToCell(path, null, Alignment.CENTER, Alignment.LEFT);
            }

            search.grabFocus();
            refilter();
        }
    }

    /*
     * Getters and Setters --------------------------------
     */

    /**
     * @return the selected Ledger object whose parent is the selected
     *         Account.
     */
    public Ledger getLedger() {
        return selectedLedger;
    }

    /**
     * Inform the AccountLedgerPicker which Ledger it is to be currently
     * representing. If the Ledger's parentAccount is set, then that will be
     * used as the account which is currently selected.
     */
    public void setLedger(Ledger ledger) {
        if (ledger == null) {
            throw new IllegalArgumentException("null invalid argument for setLedger()");
        }
        selectedLedger = ledger;
        Account a = ledger.getParentAccount();
        if (a != null) {
            selectedAccount = a;
        }
        this.setDisplayText();
        popup.setSelection(ledger);
    }

    /**
     * If both an account and ledger have been selected when this is called,
     * then it will format up a string for display in the Entry.
     */
    private void setDisplayText() {
        if (selectedLedger != null) {
            display.setLedger(selectedLedger);
        }
    }

    /*
     * Override inherited methods -------------------------
     */

    public void grabFocus() {
        wide.grabFocus();
    }
}
