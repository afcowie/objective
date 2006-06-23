/*
 * AccountPicker.java
 * 
 * See LICENCE file for usage and redistribution terms
 * Copyright (c) 2005-2006 Operational Dynamics
 */
package accounts.ui;

import generic.persistence.DataClient;
import generic.ui.AbstractWindow;
import generic.util.Debug;

import java.util.Iterator;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.gnu.gdk.KeyValue;
import org.gnu.gtk.Button;
import org.gnu.gtk.CellRendererText;
import org.gnu.gtk.DataColumn;
import org.gnu.gtk.DataColumnObject;
import org.gnu.gtk.DataColumnString;
import org.gnu.gtk.Entry;
import org.gnu.gtk.EventBox;
import org.gnu.gtk.HBox;
import org.gnu.gtk.ListStore;
import org.gnu.gtk.ReliefStyle;
import org.gnu.gtk.SelectionMode;
import org.gnu.gtk.StatusBar;
import org.gnu.gtk.TreeIter;
import org.gnu.gtk.TreeModel;
import org.gnu.gtk.TreeModelFilter;
import org.gnu.gtk.TreeModelFilterVisibleMethod;
import org.gnu.gtk.TreeModelSort;
import org.gnu.gtk.TreePath;
import org.gnu.gtk.TreeSelection;
import org.gnu.gtk.TreeView;
import org.gnu.gtk.TreeViewColumn;
import org.gnu.gtk.Window;
import org.gnu.gtk.event.ButtonEvent;
import org.gnu.gtk.event.ButtonListener;
import org.gnu.gtk.event.EntryEvent;
import org.gnu.gtk.event.EntryListener;
import org.gnu.gtk.event.FocusEvent;
import org.gnu.gtk.event.FocusListener;
import org.gnu.gtk.event.KeyEvent;
import org.gnu.gtk.event.KeyListener;
import org.gnu.gtk.event.TreeViewEvent;
import org.gnu.gtk.event.TreeViewListener;

import accounts.domain.Account;
import accounts.domain.Books;
import accounts.domain.Ledger;

/**
 * A widget to select an account.
 * 
 * @author Andrew Cowie
 */
public class AccountPicker extends HBox
{
	private Account					selectedAccount	= null;
	private Ledger					selectedLedger	= null;

	private AccountLedgerDisplay	display			= null;
	private Button					wide			= null;
	private AccountPickerPopup		popup			= null;
	private EventBox				backing			= null;

	private DataClient				db;

	public AccountPicker(DataClient store) {
		super(false, 3);

		db = store;

		display = new AccountLedgerDisplay();
		display.setMinimumSize(240, -1);

		/*
		 * This box is a spacer so that the EventBox is as wide as the widget up
		 * to but not including the pick button.
		 */

		wide = new Button();
		wide.add(display);
		wide.setRelief(ReliefStyle.NORMAL);

		backing = new EventBox();
		backing.add(wide);

		popup = new AccountPickerPopup("accountpicker", "share/AccountPickerPopup.glade");

		wide.addListener(new ButtonListener() {
			public void buttonEvent(ButtonEvent event) {
				if (event.getType() == ButtonEvent.Type.CLICK) {
					popup.present();
				}
			}
		});

		this.packStart(backing, true, true, 0);
	}

	/**
	 * A Window (constructed from a glade file) containing the list of Accounts
	 * and an Entry box, and listeners to catch appropriate keystrokes. This is
	 * an inner class of AccountPicker.
	 */
	class AccountPickerPopup extends AbstractWindow
	{
		private ListStore			listStore;
		private TreeModelFilter		filteredStore;
		private TreeModelSort		sortedStore;
		private DataColumnString	accountDisplay_DataColumn;
		private DataColumnString	accountTitle_DataColumn;
		private DataColumnObject	accountObject_DataColumn;
		private DataColumnString	ledgerDisplay_DataColumn;
		private DataColumnString	ledgerName_DataColumn;
		private DataColumnObject	ledgerObject_DataColumn;

		private TreeView			view;
		TreeViewColumn				account_ViewColumn;

		private SearchEntry			search;

		private int					visibleRows;
		private int					totalRows;

		private StatusBar			status;
		private int					lastID	= 0;

		public AccountPickerPopup(String which, String filename) {
			super(which, filename);

			/*
			 * Setup the underlying TreeModel. Each viewable column has three
			 * DataColumn objects underlying it. The "Display" one holds an
			 * extensively composite marked up version that is what is actually
			 * displayed. "Title" is the straight up String version of the data,
			 * and is used for sorting and filtering. And the "Object" is the
			 * reference back to our domain object model - the thing we're
			 * actually trying to pick in the first place.
			 */
			accountDisplay_DataColumn = new DataColumnString();
			accountTitle_DataColumn = new DataColumnString();
			accountObject_DataColumn = new DataColumnObject();

			ledgerDisplay_DataColumn = new DataColumnString();
			ledgerName_DataColumn = new DataColumnString();
			ledgerObject_DataColumn = new DataColumnObject();

			DataColumn[] accountsPicker_DataColumnsArray = new DataColumn[] {
				accountDisplay_DataColumn,
				accountTitle_DataColumn,
				accountObject_DataColumn,
				ledgerDisplay_DataColumn,
				ledgerName_DataColumn,
				ledgerObject_DataColumn
			};

			listStore = new ListStore(accountsPicker_DataColumnsArray);

			filteredStore = new TreeModelFilter(listStore);

			sortedStore = new TreeModelSort(filteredStore);

			view = (TreeView) gladeParser.getWidget("possibles_treeview");
			view.setModel(sortedStore);

			/*
			 * As is tradition by now, I observe that "this is the most god
			 * aweful API since EJB"
			 */
			// Account column
			account_ViewColumn = new TreeViewColumn();
			account_ViewColumn.setResizable(true);
			account_ViewColumn.setReorderable(false);

			CellRendererText account_CellRenderer = new CellRendererText();
			account_ViewColumn.packStart(account_CellRenderer, true);
			account_ViewColumn.addAttributeMapping(account_CellRenderer,
				CellRendererText.Attribute.MARKUP, accountDisplay_DataColumn);

			account_ViewColumn.setTitle("Account");
			account_ViewColumn.setClickable(true);
			account_ViewColumn.setSortColumn(accountTitle_DataColumn);

			view.appendColumn(account_ViewColumn);

			// Ledger column
			TreeViewColumn ledger_ViewColumn = new TreeViewColumn();
			ledger_ViewColumn.setResizable(true);
			ledger_ViewColumn.setReorderable(false);

			CellRendererText ledger_CellRenderer = new CellRendererText();
			ledger_ViewColumn.packStart(ledger_CellRenderer, true);
			ledger_ViewColumn.addAttributeMapping(ledger_CellRenderer,
				CellRendererText.Attribute.MARKUP, ledgerDisplay_DataColumn);

			ledger_ViewColumn.setTitle("Ledger");
			ledger_ViewColumn.setClickable(true);
			ledger_ViewColumn.setSortColumn(ledgerName_DataColumn);

			view.appendColumn(ledger_ViewColumn);

			/*
			 * overall properties
			 */
			view.setAlternateRowColor(false);
			view.setEnableSearch(false);
			view.setReorderable(false);
			account_ViewColumn.click();

			TreeSelection selection = view.getSelection();
			selection.setMode(SelectionMode.SINGLE);

			status = (StatusBar) gladeParser.getWidget("statusbar");

			/*
			 * Populate
			 */
			Books root = (Books) db.getRoot();
			Set accounts = root.getAccountsSet();
			Iterator acctIter = accounts.iterator();

			final Pattern regexAmp = Pattern.compile("&");
			while (acctIter.hasNext()) {
				Account acct = (Account) acctIter.next();
				Set ledgers = acct.getLedgers();
				Iterator ledgersIter = ledgers.iterator();
				while (ledgersIter.hasNext()) {
					TreeIter pointer = listStore.appendRow();

					Ledger ledger = (Ledger) ledgersIter.next();

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
			Entry e = (Entry) gladeParser.getWidget("search_entry");
			search = new SearchEntry(e);

			/*
			 * this is here as needs _search to be initialized, otherwise GTK
			 * makes a mess
			 */

			filteredStore.setVisibleMethod(new TreeModelFilterVisibleMethod() {
				private Pattern	regex	= null;
				private String	cached	= "bleep";

				public boolean filter(TreeModel model, TreeIter pointer) {
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
			search.setChangeListener(new EntryListener() {
				public void entryEvent(EntryEvent event) {
					Debug.print("listeners", "AccountPickerPopup search entryEvent "
						+ event.getType().getName());
					if (event.getType() == EntryEvent.Type.CHANGED) {
						refilter();
					}
					if (event.getType() == EntryEvent.Type.ACTIVATE) {
						view.grabFocus();
					}
				}
			});

			/*
			 * If in the entry and you press down, jump straight to the rows
			 * (skipping the headers).
			 */
			search.addListener(new KeyListener() {
				public boolean keyEvent(KeyEvent event) {
					int key = event.getKeyval();
					if ((key == KeyValue.Down) || (key == KeyValue.Up)) {
						view.grabFocus();
						return true;
					} else {
						return false;
					}
				}
			});

			/*
			 * As much as possible, make the cursor go to the end (and deselect
			 * all the text in the process - otherwise you get an overwrite)
			 */
			search.addListener(new FocusListener() {
				public boolean focusEvent(FocusEvent event) {
					if (event.getType() == FocusEvent.Type.FOCUS_IN) {
						search.setCursorPosition(search.getText().length());
					}
					return false;
				}
			});

			window.addListener(new KeyListener() {
				public boolean keyEvent(KeyEvent event) {
					int key = event.getKeyval();
					if (key == KeyValue.Escape) {
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
			view.addListener(new KeyListener() {
				public boolean keyEvent(KeyEvent event) {
					if (event.getType() == KeyEvent.Type.KEY_PRESSED) {
						int key = event.getKeyval();
						String str = event.getString();

						String orig = search.getText();
						int len = orig.length();

						if (key == KeyValue.BackSpace) {
							search.grabFocus();
							if (len > 0) {
								search.setText(orig.substring(0, len - 1));
								refilter();
							}
							return true;
						} else if (key == KeyValue.Left) {
							search.grabFocus();
							if (len > 0) {
								search.setCursorPosition(len - 1);
							}
							return true;
						} else if (key == KeyValue.Right) {
							search.grabFocus();
							search.setCursorPosition(len);
							return true;
						} else if (regexAtoZ.matcher(str).matches()) {
							search.grabFocus();
							search.setText(search.getText() + str);
							refilter();
							return true;
						} else {
							return false;
						}
					} else {
						return false;
					}

				}
			});

			view.addListener(new TreeViewListener() {
				public void treeViewEvent(TreeViewEvent event) {
					if (event.getType() == TreeViewEvent.Type.ROW_ACTIVATED) {
						TreeIter pointer = event.getTreeIter();
						applySelection(pointer);
					}
				}
			});

		}

		/**
		 * This gets called from a few places; basically its nothing more than a
		 * bit of prettiness around {@link TreeModelFilter#refilter()}. The
		 * reason this UI element is here at all is that sometimes, especially
		 * after a first keystroke, the list isn't appreciably narrowed and so
		 * without some feedback that "something is happening" the user can be a
		 * bit disoriented.
		 * <P>
		 * It was tempting to cache some of this, total row count at least, but
		 * there is the case that maybe an Account or Ledger has been added. I'd
		 * rather have this code here now that try to figure out where the hell
		 * an annoying "not keeping up" bug is coming from in 18 months.
		 */
		private void refilter() {
			TreeIter pointer;
			totalRows = 0;

			pointer = listStore.getFirstIter();
			while (pointer != null) {
				totalRows++;
				pointer = pointer.getNextIter();
			}

			filteredStore.refilter();

			visibleRows = 0;
			pointer = filteredStore.getFirstIter();
			while (pointer != null) {
				visibleRows++;
				pointer = pointer.getNextIter();
			}

			if (lastID != 0) {
				status.pop(lastID);
			}
			lastID = status.getContextID("" + visibleRows);
			status.push(lastID, visibleRows + "/" + totalRows + " visible");
		}

		/**
		 * When this thing closes the default handler will call deleteHook which
		 * will hide and do other things. That's all fine. But we want to raise
		 * the parent as well.
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
		 * @return override the default return of false - we return true because
		 *         we've handled it, and *don't* want the delete to turn into a
		 *         destroy.
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
			TreeIter pointer = sortedStore.getFirstIter();
			while (pointer != null) {
				Ledger l = (Ledger) sortedStore.getValue(pointer, ledgerObject_DataColumn);

				if (ledger == l) {
					view.getSelection().select(pointer);
					return;
				}

				pointer = pointer.getNextIter();
			}
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
			search.clearText();
		}

		/*
		 * Overrides of inherited methods -----------------
		 */

		public void present() {
			org.gnu.gdk.Window gdkWindow = backing.getWindow();

			int x = gdkWindow.getOrigin().getX();
			int y = gdkWindow.getOrigin().getY(); // + gdkWindow.getHeight();

			window.move(x, y);

			super.present();

			TreePath[] selected = view.getSelection().getSelectedRows();
			if (selected.length == 1) {
				view.scrollToCell(selected[0], account_ViewColumn, 0.5, 0.0);
			}

			search.grabFocus();
			refilter();
		}

	}

	/**
	 * An override of Entry which has a single EntryListener which is removed
	 * before programatic changes to the Entry widget.
	 */
	class SearchEntry extends Entry
	{
		private EntryListener	listener	= null;

		SearchEntry() {
			super();
		}

		/**
		 * Create a new SearchEntry from an existing Entry Widget (presumably
		 * retrieved from glade).
		 */
		SearchEntry(Entry existingWidget) {
			super(existingWidget.getHandle());
		}

		/**
		 * 
		 * @param listener
		 *            the EntryListener (presumably listening for CHANGED
		 *            events) that you will be toggled off when programatically
		 *            setting the Entry is required.
		 */
		public void setChangeListener(EntryListener listener) {
			if (listener == null) {
				throw new IllegalArgumentException();
			}
			this.listener = listener;
			enableChangeListener();
		}

		public void disableChangeListener() {
			if (listener == null) {
				return;
			}
			super.removeListener(listener);
		}

		public void enableChangeListener() {
			if (listener == null) {
				return;
			}
			super.addListener(listener);
		}

		public void clearText() {
			disableChangeListener();
			super.setText("");
			enableChangeListener();
		}

		public void setText(String text) {
			disableChangeListener();
			super.setText(text);
			super.setCursorPosition(text.length());
			enableChangeListener();
		}

	}

	/*
	 * Getters and Setters --------------------------------
	 */

	/**
	 * @return the selected Account object which contains the selected Legdger.
	 */
	public Account getAccount() {
		return selectedAccount;
	}

	public void setAccount(Account account) {
		if (account == null) {
			throw new IllegalArgumentException("null invalid argument for setAccount()");
		}
		selectedAccount = account;
		setDisplayText();
	}

	/**
	 * @return the selected Ledger object whose parent is the selected Account.
	 */
	public Ledger getLedger() {
		return selectedLedger;
	}

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