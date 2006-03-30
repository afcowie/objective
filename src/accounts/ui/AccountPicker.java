/*
 * AccountPicker.java
 * 
 * See LICENCE file for usage and redistribution terms
 * Copyright (c) 2005 Operational Dynamics
 */
package accounts.ui;

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
import org.gnu.gtk.GtkStockItem;
import org.gnu.gtk.HBox;
import org.gnu.gtk.IconSize;
import org.gnu.gtk.Image;
import org.gnu.gtk.ListStore;
import org.gnu.gtk.SelectionMode;
import org.gnu.gtk.StatusBar;
import org.gnu.gtk.TreeIter;
import org.gnu.gtk.TreeModel;
import org.gnu.gtk.TreeModelFilter;
import org.gnu.gtk.TreeModelFilterVisibleMethod;
import org.gnu.gtk.TreeModelSort;
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

import accounts.client.ObjectiveAccounts;
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
	private Account				_selectedAccount	= null;
	private Ledger				_selectedLedger		= null;

	private static Account		_lastSelectedAccount;
	private static Ledger		_lastSelectedLedger;

	static {
		_lastSelectedAccount = null;
		_lastSelectedLedger = null;
	}

	// private Label _label = null;
	private SearchEntry			_entry				= null;
	private Button				_pick				= null;
	private AccountPickerPopup	_popup				= null;

	public AccountPicker() {
		super(false, 3);
		_selectedAccount = _lastSelectedAccount;
		_selectedLedger = _lastSelectedLedger;

		_entry = new SearchEntry();
		_entry.setWidth(20);

		_pick = new Button();
		Image icon = new Image(GtkStockItem.JUSTIFY_RIGHT, IconSize.BUTTON);
		// Label label = new Label("Pick", false);
		// HBox box = new HBox(false, 1);
		//
		// box.packStart(icon, false, false, 0);
		// box.packStart(label, false, false, 0);
		// _pick.add(box);
		_pick.add(icon);

		packStart(_entry, true, true, 0);
		packStart(_pick, false, false, 0);

		_popup = new AccountPickerPopup("accountpicker", "share/AccountPickerPopup.glade");

		_pick.addListener(new ButtonListener() {
			public void buttonEvent(ButtonEvent event) {
				if (event.getType() == ButtonEvent.Type.CLICK) {
					_popup.present();
				}
			}
		});

		_entry.setChangeListener(new EntryListener() {
			public void entryEvent(EntryEvent event) {
				Debug.print("listeners", "AccountPicker entry EntryEvent " + event.getType().getName());
				if ((event.getType() == EntryEvent.Type.CHANGED) || (event.getType() == EntryEvent.Type.ACTIVATE)) {
					/*
					 * If we don't have a selected account, then the keystroke
					 * or paste or whatever is the seed for the search filter.
					 */
					if (_selectedAccount == null) {
						_popup.setSearch(_entry.getText());
					}

					_popup.present();

					_entry.clearText();
				}
			}
		});
	}

	/**
	 * A Window (constructed from a glade file) containing the list of Accounts
	 * and an Entry box, and listeners to catch appropriate keystrokes. This is
	 * an inner class of AccountPicker.
	 */
	class AccountPickerPopup extends AbstractWindow
	{
		private ListStore			_listStore;
		private TreeModelFilter		_filteredStore;
		private TreeModelSort		_sortedStore;
		private DataColumnString	_accountDisplay_DataColumn;
		private DataColumnString	_accountTitle_DataColumn;
		private DataColumnObject	_accountObject_DataColumn;
		private DataColumnString	_ledgerDisplay_DataColumn;
		private DataColumnString	_ledgerName_DataColumn;
		private DataColumnObject	_ledgerObject_DataColumn;

		private TreeView			_view;
		private SearchEntry			_search;

		private int					_visibleRows;
		private int					_totalRows;

		private StatusBar			_status;
		private int					_lastID	= 0;

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
			_accountDisplay_DataColumn = new DataColumnString();
			_accountTitle_DataColumn = new DataColumnString();
			_accountObject_DataColumn = new DataColumnObject();

			_ledgerDisplay_DataColumn = new DataColumnString();
			_ledgerName_DataColumn = new DataColumnString();
			_ledgerObject_DataColumn = new DataColumnObject();

			DataColumn[] accountsPicker_DataColumnsArray = new DataColumn[] {
				_accountDisplay_DataColumn,
				_accountTitle_DataColumn,
				_accountObject_DataColumn,
				_ledgerDisplay_DataColumn,
				_ledgerName_DataColumn,
				_ledgerObject_DataColumn
			};

			_listStore = new ListStore(accountsPicker_DataColumnsArray);

			_filteredStore = new TreeModelFilter(_listStore);

			_sortedStore = new TreeModelSort(_filteredStore);

			_view = (TreeView) gladeParser.getWidget("possibles_treeview");
			_view.setModel(_sortedStore);

			/*
			 * As is tradition by now, I observe that "this is the most god
			 * aweful API since EJB"
			 */
			// Account column
			TreeViewColumn account_ViewColumn = new TreeViewColumn();
			account_ViewColumn.setResizable(true);
			account_ViewColumn.setReorderable(false);

			CellRendererText account_CellRenderer = new CellRendererText();
			account_ViewColumn.packStart(account_CellRenderer, true);
			account_ViewColumn.addAttributeMapping(account_CellRenderer, CellRendererText.Attribute.MARKUP,
				_accountDisplay_DataColumn);

			account_ViewColumn.setTitle("Account");
			account_ViewColumn.setClickable(true);
			account_ViewColumn.setSortColumn(_accountTitle_DataColumn);

			_view.appendColumn(account_ViewColumn);

			// Ledger column
			TreeViewColumn ledger_ViewColumn = new TreeViewColumn();
			ledger_ViewColumn.setResizable(true);
			ledger_ViewColumn.setReorderable(false);

			CellRendererText ledger_CellRenderer = new CellRendererText();
			ledger_ViewColumn.packStart(ledger_CellRenderer, true);
			ledger_ViewColumn.addAttributeMapping(ledger_CellRenderer, CellRendererText.Attribute.MARKUP,
				_ledgerDisplay_DataColumn);

			ledger_ViewColumn.setTitle("Ledger");
			ledger_ViewColumn.setClickable(true);
			ledger_ViewColumn.setSortColumn(_ledgerName_DataColumn);

			_view.appendColumn(ledger_ViewColumn);

			/*
			 * overall properties
			 */
			_view.setAlternateRowColor(false);
			_view.setEnableSearch(false);
			_view.setReorderable(false);
			account_ViewColumn.click();

			TreeSelection selection = _view.getSelection();
			selection.setMode(SelectionMode.SINGLE);

			_status = (StatusBar) gladeParser.getWidget("statusbar");

			/*
			 * Populate
			 */
			Books root = ObjectiveAccounts.store.getBooks();
			Set accounts = root.getAccountsSet();
			Iterator acctIter = accounts.iterator();

			final Pattern regexAmp = Pattern.compile("&");
			while (acctIter.hasNext()) {
				Account acct = (Account) acctIter.next();
				Set ledgers = acct.getLedgers();
				Iterator ledgersIter = ledgers.iterator();
				while (ledgersIter.hasNext()) {
					TreeIter pointer = _listStore.appendRow();

					Ledger ledger = (Ledger) ledgersIter.next();

					/*
					 * Eek! Deeply burried presentation code. Alas. Well, it's
					 * important that you have the same size-ness for both
					 * Account and Ledger.
					 */
					final String size = "xx-small";
					Matcher m = regexAmp.matcher(acct.getTitle());
					_listStore.setValue(pointer, _accountDisplay_DataColumn, m.replaceAll("&amp;") + "\n<span size=\""
						+ size + "\" color=\"" + acct.getColor() + "\">" + acct.getClassString() + "</span>");
					_listStore.setValue(pointer, _accountTitle_DataColumn, acct.getTitle());
					_listStore.setValue(pointer, _accountObject_DataColumn, acct);

					_listStore.setValue(pointer, _ledgerDisplay_DataColumn, ledger.getName() + "\n<span size=\"" + size
						+ "\"color=\"" + ledger.getColor() + "\">" + ledger.getClassString() + "</span>");
					_listStore.setValue(pointer, _ledgerName_DataColumn, ledger.getName());
					_listStore.setValue(pointer, _ledgerObject_DataColumn, ledger);
				}
			}

			/*
			 * Setup the search / filter mechanism.
			 */
			Entry e = (Entry) gladeParser.getWidget("search_entry");
			_search = new SearchEntry(e);

			/*
			 * this is here as needs _search to be initialized, otherwise GTK
			 * makes a mess
			 */

			_filteredStore.setVisibleMethod(new TreeModelFilterVisibleMethod() {
				private Pattern	regex	= null;
				private String	cached	= "bleep";

				public boolean filter(TreeModel model, TreeIter pointer) {
					String q = _search.getText();

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

					String accountTitle = model.getValue(pointer, _accountTitle_DataColumn);
					m = regex.matcher(accountTitle);
					if (m.matches()) {
						return true;
					}

					String ledgerName = model.getValue(pointer, _ledgerName_DataColumn);
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
			_search.setChangeListener(new EntryListener() {
				public void entryEvent(EntryEvent event) {
					Debug.print("listeners", "AccountPickerPopup search entryEvent " + event.getType().getName());
					if (event.getType() == EntryEvent.Type.CHANGED) {
						refilter();
					}
					if (event.getType() == EntryEvent.Type.ACTIVATE) {
						_view.grabFocus();
					}
				}
			});

			/*
			 * If in the entry and you press down, jump straight to the rows
			 * (skipping the headers).
			 */
			_search.addListener(new KeyListener() {
				public boolean keyEvent(KeyEvent event) {
					int key = event.getKeyval();
					if ((key == KeyValue.Down) || (key == KeyValue.Up)) {
						_view.grabFocus();
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
			_search.addListener(new FocusListener() {
				public boolean focusEvent(FocusEvent event) {
					if (event.getType() == FocusEvent.Type.FOCUS_IN) {
						_search.setCursorPosition(_search.getText().length());
					}
					return false;
				}
			});

			window.addListener(new KeyListener() {
				public boolean keyEvent(KeyEvent event) {
					int key = event.getKeyval();
					if (key == KeyValue.Escape) {
						if ((_selectedAccount == null) || (_entry.getText().equals(""))) {
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
			_view.addListener(new KeyListener() {
				public boolean keyEvent(KeyEvent event) {
					if (event.getType() == KeyEvent.Type.KEY_PRESSED) {
						int key = event.getKeyval();
						String str = event.getString();

						String orig = _search.getText();
						int len = orig.length();

						if (key == KeyValue.BackSpace) {
							_search.grabFocus();
							if (len > 0) {
								_search.setText(orig.substring(0, len - 1));
								refilter();
							}
							return true;
						} else if (key == KeyValue.Left) {
							_search.grabFocus();
							if (len > 0) {
								_search.setCursorPosition(len - 1);
							}
							return true;
						} else if (key == KeyValue.Right) {
							_search.grabFocus();
							_search.setCursorPosition(len);
							return true;
						} else if (regexAtoZ.matcher(str).matches()) {
							_search.grabFocus();
							_search.setText(_search.getText() + str);
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

			_view.addListener(new TreeViewListener() {
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
			_totalRows = 0;

			pointer = _listStore.getFirstIter();
			while (pointer != null) {
				_totalRows++;
				pointer = pointer.getNextIter();
			}

			_filteredStore.refilter();

			_visibleRows = 0;
			pointer = _filteredStore.getFirstIter();
			while (pointer != null) {
				_visibleRows++;
				pointer = pointer.getNextIter();
			}

			if (_lastID != 0) {
				_status.pop(_lastID);
			}
			_lastID = _status.getContextID("" + _visibleRows);
			_status.push(_lastID, _visibleRows + "/" + _totalRows + " visible");
		}

		/**
		 * When this thing closes the default handler will call deleteHook which
		 * will hide and do other things. That's all fine. But we want to raise
		 * the parent as well.
		 */
		protected void hideHook() {
			/*
			 * So annoying. You'd think you should be able to cast from Widget
			 * to Window here, but it blows ClassCastException.
			 */
			Window top = (Window) _entry.getToplevel();
			top.present();
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
			_selectedAccount = (Account) _sortedStore.getValue(pointer, _accountObject_DataColumn);
			_selectedLedger = (Ledger) _sortedStore.getValue(pointer, _ledgerObject_DataColumn);
			setEntryText();
		}

		private void setEntryText() {
			String str = _selectedAccount.getTitle() + " - " + _selectedLedger.getName();
			_entry.setText(str);
		}

		private void clearEntry() {
			_entry.clearText();
			_selectedAccount = null;
			_selectedLedger = null;
		}

		private void setSearch(String str) {
			_search.setText(str);
			refilter();
		}

		private void clearSearch() {
			_search.clearText();
		}

		/*
		 * Overrides of inherited methods -----------------
		 */

		public void present() {
			org.gnu.gdk.Window gdkWindow = _entry.getWindow();

			int x = gdkWindow.getOrigin().getX();
			int y = gdkWindow.getOrigin().getY(); // + gdkWindow.getHeight();

			window.move(x, y);

			super.present();
			_search.grabFocus();
		}

	}

	/**
	 * An override of Entry which has a single EntryListener which is removed
	 * before programatic changes to the Entry widget.
	 */
	class SearchEntry extends Entry
	{
		private EntryListener	_listener	= null;

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
			this._listener = listener;
			enableChangeListener();
		}

		public void disableChangeListener() {
			if (_listener == null) {
				return;
			}
			super.removeListener(_listener);
		}

		public void enableChangeListener() {
			if (_listener == null) {
				return;
			}
			super.addListener(_listener);
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
	public Account getAccount() {
		return _selectedAccount;
	}

	public void setAccount(Account account) {
		if (account == null) {
			return;
			// throw something?
		}
		_selectedAccount = account;
	}

	/*
	 * Override inherited methods -------------------------
	 */

	public void grabFocus() {
		_entry.grabFocus();
	}
}