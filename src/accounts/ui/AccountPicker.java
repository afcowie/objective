/*
 * AccountPicker.java
 * 
 * See LICENCE file for usage and redistribution terms
 * Copyright (c) 2005-2008 Operational Dynamics
 */
package accounts.ui;

import static org.gnome.gtk.Alignment.CENTER;
import static org.gnome.gtk.Alignment.LEFT;
import generic.persistence.DataClient;

import java.util.Iterator;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.gnome.gdk.EventFocus;
import org.gnome.gdk.EventKey;
import org.gnome.gdk.Keyval;
import org.gnome.gtk.Button;
import org.gnome.gtk.CellRendererText;
import org.gnome.gtk.DataColumn;
import org.gnome.gtk.DataColumnReference;
import org.gnome.gtk.DataColumnString;
import org.gnome.gtk.Editable;
import org.gnome.gtk.Entry;
import org.gnome.gtk.EventBox;
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

import accounts.domain.Account;
import accounts.domain.Books;
import accounts.domain.Ledger;

/**
 * A widget to select an Account / Ledger combination. AccountPicker presents
 * itself as a button containing a Label presenting the Account and Ledger in
 * the appropriate Debit or Credit colour. When activated, a quick Window pops
 * up displaying full the list of Accounts and Ledgers, allowing the user to
 * select the one they are interested in. The user can narrowthe search with
 * any characters entered and the search text will be used to filter both
 * Account titles and Ledger names.
 * 
 * @author Andrew Cowie
 * @see accounts.ui.AccountLedgerDisplay which is used to render the label on
 *      the main button indicating the selected Account/Ledger pair.
 */
public class AccountPicker extends HBox
{
    private Account selectedAccount = null;

    private Ledger selectedLedger = null;

    private AccountLedgerDisplay display = null;

    private Button wide = null;

    private AccountPickerPopup popup = null;

    private EventBox backing = null;

    private DataClient db;

    /**
     * Instantiate a new AccountPicker widget.
     * 
     * @param store
     *            an open DataClient connection that will be used to pull the
     *            list Accounts and Ledgers.
     */
    public AccountPicker(DataClient store) {
        super(false, 3);

        db = store;

        display = new AccountLedgerDisplay();
        display.setSizeRequest(240, -1);

        /*
         * This box is a spacer so that the EventBox is as wide as the widget
         * up to but not including the pick button.
         */

        wide = new Button();
        wide.add(display);
        wide.setRelief(ReliefStyle.NORMAL);

        backing = new EventBox();
        backing.add(wide);

        popup = new AccountPickerPopup();

        wide.connect(new Button.CLICKED() {
            public void onClicked(Button source) {
                popup.present();
            }
        });

        this.packStart(backing, true, true, 0);
    }

    /**
     * A Window containing the list of Accounts and an Entry box, and
     * listeners to catch appropriate keystrokes. This is an inner class of
     * AccountPicker.
     */
    /*
     * It's a bit of a monster, this class.
     */
    class AccountPickerPopup
    {
        private ListStore listStore;

        private TreeModelFilter filteredStore;

        private TreeModelSort sortedStore;

        private DataColumnString accountDisplay_DataColumn;

        private DataColumnString accountTitle_DataColumn;

        private DataColumnReference accountObject_DataColumn;

        private DataColumnString ledgerDisplay_DataColumn;

        private DataColumnString ledgerName_DataColumn;

        private DataColumnReference ledgerObject_DataColumn;

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

            window = new Window();

            TreeViewColumn vertical;
            CellRendererText text;

            /*
             * Setup the underlying TreeModel. Each viewable column has three
             * DataColumn objects underlying it. The "Display" one holds an
             * extensively composite marked up version that is what is
             * actually displayed. "Title" is the straight up String version
             * of the data, and is used for sorting and filtering. And the
             * "Object" is the reference back to our domain object model - the
             * thing we're actually trying to pick in the first place.
             */
            accountDisplay_DataColumn = new DataColumnString();
            accountTitle_DataColumn = new DataColumnString();
            accountObject_DataColumn = new DataColumnReference();

            ledgerDisplay_DataColumn = new DataColumnString();
            ledgerName_DataColumn = new DataColumnString();
            ledgerObject_DataColumn = new DataColumnReference();

            DataColumn[] accountsPicker_DataColumnsArray = new DataColumn[] {
                    accountDisplay_DataColumn,
                    accountTitle_DataColumn,
                    accountObject_DataColumn,
                    ledgerDisplay_DataColumn,
                    ledgerName_DataColumn,
                    ledgerObject_DataColumn
            };

            listStore = new ListStore(accountsPicker_DataColumnsArray);

            filteredStore = new TreeModelFilter(listStore, null);

            sortedStore = new TreeModelSort(filteredStore);

            view = new TreeView(sortedStore);

            // Account column
            vertical = view.appendColumn();
            vertical.setResizable(true);
            vertical.setReorderable(false);

            text = new CellRendererText(vertical);
            text.setMarkup(accountDisplay_DataColumn);

            vertical.setTitle("Account");
            vertical.setClickable(true);
            vertical.setSortColumn(accountTitle_DataColumn);
            vertical.clicked();

            // Ledger column
            vertical = view.appendColumn();
            vertical.setResizable(true);
            vertical.setReorderable(false);

            text = new CellRendererText(vertical);
            text.setMarkup(ledgerDisplay_DataColumn);

            vertical.setTitle("Ledger");
            vertical.setClickable(true);
            vertical.setSortColumn(ledgerName_DataColumn);

            /*
             * overall properties
             */
            view.setRulesHint(false);
            view.setEnableSearch(false);
            view.setReorderable(false);

            TreeSelection selection = view.getSelection();
            selection.setMode(SelectionMode.SINGLE);

            /*
             * Populate
             */
            Books root = (Books) db.getRoot();
            Set<Account> accounts = root.getAccountsSet();
            Iterator<Account> acctIter = accounts.iterator();

            final Pattern regexAmp = Pattern.compile("&");
            while (acctIter.hasNext()) {
                Account acct = acctIter.next();
                Set<Ledger> ledgers = acct.getLedgers();
                Iterator<Ledger> ledgersIter = ledgers.iterator();
                while (ledgersIter.hasNext()) {
                    TreeIter pointer = listStore.appendRow();

                    Ledger ledger = ledgersIter.next();

                    /*
                     * Eek! Deeply burried presentation code. Alas. Well, it's
                     * important that you have the same size-ness for both
                     * Account and Ledger.
                     */
                    final String size = "xx-small";
                    Matcher m = regexAmp.matcher(acct.getTitle());
                    listStore.setValue(pointer, accountDisplay_DataColumn, m.replaceAll("&amp;")
                            + "\n<span size=\"" + size + "\" color=\"" + acct.getColor(false) + "\">"
                            + acct.getClassString() + "</span>");
                    listStore.setValue(pointer, accountTitle_DataColumn, acct.getTitle());
                    listStore.setValue(pointer, accountObject_DataColumn, acct);

                    listStore.setValue(pointer, ledgerDisplay_DataColumn, ledger.getName()
                            + "\n<span size=\"" + size + "\"color=\"" + ledger.getColor(false) + "\">"
                            + ledger.getClassString() + "</span>");
                    listStore.setValue(pointer, ledgerName_DataColumn, ledger.getName());
                    listStore.setValue(pointer, ledgerObject_DataColumn, ledger);
                }
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

            top.packStart(search);

            view.setSizeRequest(-1, 210);
            scroll = new ScrolledWindow();
            scroll.setPolicy(PolicyType.NEVER, PolicyType.ALWAYS);
            scroll.add(view);
            top.packStart(scroll);

            top.packEnd(status);

            window.add(top);
            window.setDecorated(false);

            /*
             * Signal handlers and Callbacks
             */

            filteredStore.setVisibleCallback(new TreeModelFilter.VISIBLE() {
                private Pattern regex = null;

                private String cached = "bleep";

                public boolean onVisible(TreeModelFilter source, TreeModel model, TreeIter pointer) {
                    String q = search.getText();

                    /*
                     * This is ugly, but since we had to do it this way to get
                     * at a case insensitive regex, we get a way to cache the
                     * pattern as compiling them is often quite resource
                     * intensive.
                     */
                    if (!cached.equals(q)) {
                        regex = Pattern.compile(".*" + q + ".*", Pattern.CASE_INSENSITIVE);
                        cached = new String(q);
                    }
                    Matcher m;

                    String accountTitle = model.getValue(pointer, accountTitle_DataColumn);
                    m = regex.matcher(accountTitle);
                    if (m.matches()) {
                        return true;
                    }

                    String ledgerName = model.getValue(pointer, ledgerName_DataColumn);
                    m = regex.matcher(ledgerName);
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
            search.connect(new Entry.CHANGED() {
                public void onChanged(Editable source) {
                    refilter();
                }
            });

            search.connect(new Entry.ACTIVATE() {
                public void onActivate(Entry source) {
                    view.grabFocus();
                }
            });

            /*
             * If in the entry and you press down, jump straight to the rows
             * (skipping the headers).
             */
            search.connect(new Widget.KEY_PRESS_EVENT() {
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
            search.connect(new Widget.FOCUS_IN_EVENT() {
                public boolean onFocusInEvent(Widget source, EventFocus event) {
                    search.setPosition(search.getText().length());
                    return false;
                }
            });

            window.connect(new Widget.KEY_PRESS_EVENT() {
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

            final Pattern regexAtoZ = Pattern.compile("[a-z]");
            view.connect(new Widget.KEY_PRESS_EVENT() {
                public boolean onKeyPressEvent(Widget source, EventKey event) {
                    final Keyval key;

                    key = event.getKeyval();

                    String str = "" + key.toUnicode();

                    String orig = search.getText();
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
                        refilter();
                        return true;
                    } else {
                        return false;
                    }
                }
            });

            view.connect(new TreeView.ROW_ACTIVATED() {
                public void onRowActivated(TreeView source, TreePath path, TreeViewColumn vertical) {
                    TreeIter pointer = source.getModel().getIter(path);
                    applySelection(pointer);
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
            TreeIter pointer;
            totalRows = 0;

            pointer = listStore.getIterFirst();
            do {
                totalRows++;
            } while (pointer.iterNext());

            filteredStore.refilter();

            visibleRows = 0;

            pointer = filteredStore.getIterFirst();
            do {
                visibleRows++;
            } while (pointer.iterNext());

            status.setMessage(visibleRows + "/" + totalRows + " visible");
        }

        /**
         * When this thing closes the default handler will call deleteHook
         * which will hide and do other things. That's all fine. But we want
         * to raise the parent as well.
         */
        protected void hideHook() {
            Window parentTop = (Window) wide.getToplevel();
            parentTop.present();
        }

        /**
         * Override default deleteHook (which destroys; we only want to hide).
         * We will clear the Entry text. We don't even have to check
         * _ignoreChangeEvents because this is only called if the user Alt-F4s
         * the popup or something like that.
         * 
         * @return override the default return of false - we return true
         *         because we've handled it, and *don't* want the delete to
         *         turn into a destroy.
         */
        protected boolean deleteHook() {
            window.hide();
            clearSearch();
            return true;
        }

        private void applySelection(TreeIter pointer) {
            window.hide();
            selectedAccount = (Account) sortedStore.getValue(pointer, accountObject_DataColumn);
            selectedLedger = (Ledger) sortedStore.getValue(pointer, ledgerObject_DataColumn);
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
                Ledger l = (Ledger) sortedStore.getValue(pointer, ledgerObject_DataColumn);

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

        public void present() {
            org.gnome.gdk.Window gdkWindow;
            int x, y;
            TreeIter selected;
            TreePath path;

            gdkWindow = backing.getWindow();

            x = gdkWindow.getOriginX();
            y = gdkWindow.getOriginY(); // +
            // REMOVE gdkWindow.getHeight();

            window.move(x, y);

            window.showAll();
            window.present();

            selected = view.getSelection().getSelected();
            if (selected != null) {
                path = sortedStore.getPath(selected);
                view.scrollToCell(path, null, CENTER, LEFT);
            }

            search.grabFocus();
            refilter();
        }

    }

    /*
     * Getters and Setters --------------------------------
     */

    /**
     * @return the selected Account object which contains the selected
     *         Legdger.
     */
    public Account getAccount() {
        return selectedAccount;
    }

    /**
     * Inform the AccountPicker which Account it is to be currently
     * representing. Using {@link #setLedger(Ledger)} is probably easier.
     */
    public void setAccount(Account account) {
        if (account == null) {
            throw new IllegalArgumentException("null invalid argument for setAccount()");
        }
        selectedAccount = account;
        setDisplayText();
    }

    /**
     * @return the selected Ledger object whose parent is the selected
     *         Account.
     */
    public Ledger getLedger() {
        return selectedLedger;
    }

    /**
     * Inform the AccountPicker which Ledger it is to be currently
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
        setDisplayText();
        popup.setSelection(ledger);
    }

    /**
     * If both an account and ledger have been selected when this is called,
     * then it will format up a string for display in the Entry.
     */
    private void setDisplayText() {
        if ((selectedAccount != null) && (selectedLedger != null)) {
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
