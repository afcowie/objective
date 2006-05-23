/*
 * TransactionListView.java
 * 
 * See LICENCE file for usage and redistribution terms
 * Copyright (c) 2006 Operational Dynamics
 */
package accounts.ui;

import generic.client.Master;
import generic.persistence.DataClient;
import generic.ui.Text;
import generic.ui.UpdateListener;
import generic.util.Debug;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.gnu.gtk.CellRendererText;
import org.gnu.gtk.DataColumn;
import org.gnu.gtk.DataColumnBoolean;
import org.gnu.gtk.DataColumnLong;
import org.gnu.gtk.DataColumnObject;
import org.gnu.gtk.DataColumnString;
import org.gnu.gtk.ListStore;
import org.gnu.gtk.SelectionMode;
import org.gnu.gtk.TreeIter;
import org.gnu.gtk.TreePath;
import org.gnu.gtk.TreeRowReference;
import org.gnu.gtk.TreeSelection;
import org.gnu.gtk.TreeView;
import org.gnu.gtk.TreeViewColumn;
import org.gnu.gtk.event.LifeCycleEvent;
import org.gnu.gtk.event.LifeCycleListener;
import org.gnu.gtk.event.TreeSelectionEvent;
import org.gnu.gtk.event.TreeSelectionListener;
import org.gnu.gtk.event.TreeViewEvent;
import org.gnu.gtk.event.TreeViewListener;

import accounts.domain.Account;
import accounts.domain.Amount;
import accounts.domain.Books;
import accounts.domain.Credit;
import accounts.domain.Currency;
import accounts.domain.Debit;
import accounts.domain.Entry;
import accounts.domain.ForeignAmount;
import accounts.domain.Ledger;
import accounts.domain.Transaction;
import accounts.services.EntryComparator;

public class TransactionListView extends TreeView implements UpdateListener
{
	private transient Currency	home	= null;
	private DataClient			db;

	DataColumnString			typeMarkup_DataColumn;
	DataColumnString			typeSort_DataColumn;
	DataColumnString			dateText_DataColumn;
	DataColumnLong				dateSort_DataColumn;
	DataColumnString			descriptionAccountLedgerText_DataColumn;
	DataColumnString			descriptionSort_DataColumn;
	DataColumnString			debitAmountsText_DataColumn;
	DataColumnLong				debitAmountsSort_DataColumn;
	DataColumnString			creditAmountsText_DataColumn;
	DataColumnLong				creditAmountsSort_DataColumn;
	/**
	 * The Transaction object that this row represents. This must be set before
	 * calling {@link #populate(TreeIter)}
	 */
	DataColumnObject			transactionObject_DataColumn;
	/**
	 * Whether the row is to be rendered with bright colours. Set this as true
	 * if the row is selected and you want it to show up in "reverse video",
	 * otherwise use false for normal colouring.
	 */
	DataColumnBoolean			active_DataColumn;
	ListStore					model;

	TreeView					view;

	public TransactionListView(DataClient db, List transactions) {
		super();
		this.db = db;

		Books root = (Books) db.getRoot();
		home = root.getHomeCurrency();

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
		transactionObject_DataColumn = new DataColumnObject();
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
		TreeViewColumn type_ViewColumn = new TreeViewColumn();
		type_ViewColumn.setResizable(false);
		type_ViewColumn.setReorderable(false);

		CellRendererText type_CellRenderer = new CellRendererText();
		type_CellRenderer.setFloatProperty("yalign", 0.0f);
		type_ViewColumn.packStart(type_CellRenderer, false);
		type_ViewColumn.addAttributeMapping(type_CellRenderer, CellRendererText.Attribute.MARKUP,
			typeMarkup_DataColumn);

		type_ViewColumn.setTitle("Type");
		type_ViewColumn.setClickable(true);
		type_ViewColumn.setSortColumn(typeSort_DataColumn);

		view.appendColumn(type_ViewColumn);

		/*
		 * Date
		 */
		TreeViewColumn date_ViewColumn = new TreeViewColumn();
		date_ViewColumn.setResizable(false);
		date_ViewColumn.setReorderable(false);

		CellRendererText date_CellRenderer = new CellRendererText();
		date_CellRenderer.setFloatProperty("yalign", 0.0f);
		date_ViewColumn.packStart(date_CellRenderer, false);
		date_ViewColumn.addAttributeMapping(date_CellRenderer, CellRendererText.Attribute.MARKUP,
			dateText_DataColumn);

		date_ViewColumn.setTitle("Date");
		date_ViewColumn.setClickable(true);
		date_ViewColumn.setSortColumn(dateSort_DataColumn);

		view.appendColumn(date_ViewColumn);

		/*
		 * Description + Entries' parent Account » Ledger
		 */
		TreeViewColumn descEntryText_ViewColumn = new TreeViewColumn();
		descEntryText_ViewColumn.setResizable(false);
		descEntryText_ViewColumn.setReorderable(false);
		descEntryText_ViewColumn.setExpand(true);

		CellRendererText descEntryText_CellRenderer = new CellRendererText();
		descEntryText_ViewColumn.packStart(descEntryText_CellRenderer, true);
		descEntryText_ViewColumn.addAttributeMapping(descEntryText_CellRenderer,
			CellRendererText.Attribute.MARKUP, descriptionAccountLedgerText_DataColumn);

		descEntryText_ViewColumn.setTitle("Description");
		descEntryText_ViewColumn.setClickable(true);
		descEntryText_ViewColumn.setSortColumn(descriptionSort_DataColumn);

		// TODO sort order AccountComparator, yo

		view.appendColumn(descEntryText_ViewColumn);

		/*
		 * Entries' Debit
		 */
		TreeViewColumn debitAmounts_ViewColumn = new TreeViewColumn();
		debitAmounts_ViewColumn.setResizable(false);
		debitAmounts_ViewColumn.setReorderable(false);

		CellRendererText debitAmounts_CellRenderer = new CellRendererText();
		debitAmounts_CellRenderer.setFloatProperty("xalign", 1.0f);
		debitAmounts_ViewColumn.packStart(debitAmounts_CellRenderer, false);
		debitAmounts_ViewColumn.addAttributeMapping(debitAmounts_CellRenderer,
			CellRendererText.Attribute.MARKUP, debitAmountsText_DataColumn);

		debitAmounts_ViewColumn.setTitle("Debits       ");
		debitAmounts_ViewColumn.setAlignment(1.0);
		debitAmounts_ViewColumn.setClickable(true);
		debitAmounts_ViewColumn.setSortColumn(debitAmountsSort_DataColumn);

		view.appendColumn(debitAmounts_ViewColumn);

		/*
		 * Entries' Debit
		 */
		TreeViewColumn creditAmounts_ViewColumn = new TreeViewColumn();
		creditAmounts_ViewColumn.setResizable(false);
		creditAmounts_ViewColumn.setReorderable(false);

		CellRendererText creditAmounts_CellRenderer = new CellRendererText();
		creditAmounts_CellRenderer.setFloatProperty("xalign", 1.0f);
		// creditAmounts_CellRenderer.setFloatProperty("justify", 1.0f);
		creditAmounts_ViewColumn.packStart(creditAmounts_CellRenderer, false);
		creditAmounts_ViewColumn.addAttributeMapping(creditAmounts_CellRenderer,
			CellRendererText.Attribute.MARKUP, creditAmountsText_DataColumn);

		creditAmounts_ViewColumn.setTitle("Credits    ");
		// Label title = new Label("Credits <span font_desc='Mono'> </span>");
		// title.setUseMarkup(true);
		// creditAmounts_ViewColumn.setWidget(title);

		creditAmounts_ViewColumn.setAlignment(1.0);
		creditAmounts_ViewColumn.setClickable(true);
		creditAmounts_ViewColumn.setSortColumn(creditAmountsSort_DataColumn);

		view.appendColumn(creditAmounts_ViewColumn);

		/*
		 * overall properties
		 */
		view.setAlternateRowColor(true);
		view.setEnableSearch(false);
		view.setReorderable(false);

		date_ViewColumn.click();

		/*
		 * repopulate [via showAsActive()] when a row is selected to cause
		 * colours to adapt to better values appropriate being against the
		 * selected row background colour.
		 */

		TreeSelection selection = view.getSelection();
		selection.setMode(SelectionMode.SINGLE);
		selection.addListener(new TreeSelectionListener() {
			public void selectionChangedEvent(TreeSelectionEvent event) {
				TreeSelection selection = (TreeSelection) event.getSource();
				TreePath[] paths = selection.getSelectedRows();
				if (paths.length > 0) {
					showAsActive(paths[0]);
				}
			}
		});

		view.addListener(new TreeViewListener() {
			public void treeViewEvent(TreeViewEvent event) {
				Debug.print("listeners", "TreeViewEvent: " + event.getType().getName());
				if (event.getType() == TreeViewEvent.Type.ROW_ACTIVATED) {
					TreeIter pointer = event.getTreeIter();
					Transaction t = (Transaction) model.getValue(pointer, transactionObject_DataColumn);

					Master.ui.launchEditor(t);
				}
			}
		});

		/*
		 * Listen for updates to the DomainObjects we represent:
		 */
		final TransactionListView me = this;
		Master.ui.registerListener(me);

		/*
		 * And when the widget gets deleted, stop listening. This probably needs
		 * further testing; does unrealize always get sent?
		 */
		this.addListener(new LifeCycleListener() {
			public void lifeCycleEvent(LifeCycleEvent event) {
				if (event.getType() == LifeCycleEvent.Type.UNREALIZE) {
					Master.ui.deregisterListener(me);
				}
			}

			public boolean lifeCycleQuery(LifeCycleEvent event) {
				return false;
			}
		});
	}

	private static final Pattern	regexAmp	= Pattern.compile("&");

	private static final String		DARKGRAY	= "darkgray";

	private static final String		LIGHTGRAY	= "lightgray";

	private static final String		CHARCOAL	= "#575757";

	/**
	 * Load a Set of Transaction objects into the TreeModel underlying this
	 * widget. This is called by both the constructor and when a new assortment
	 * of Transactions is being updated into an existing widget.
	 */
	private void populate(List transactions) {
		Iterator tI = transactions.iterator();
		while (tI.hasNext()) {
			Transaction t = (Transaction) tI.next();
			TreeIter pointer = model.appendRow();

			/*
			 * Populate is geared to be re-used and extracts its Transaction
			 * from the DataColumnObject there. So in the case of setting up the
			 * TransactionListView for the first time, set that DataColumn
			 * before calling populate().
			 */
			model.setValue(pointer, transactionObject_DataColumn, t);
			model.setValue(pointer, active_DataColumn, false);

			populate(pointer);
		}
	}

	/**
	 * A stable reference to the previous row selected, if any. This allows the
	 * TreeSelectionListener to restore the data in that row to "normal" when
	 * the selection changes. Note that this is a TreeRowReference rather than a
	 * TreePath so that it is stable across things that invalidate TreeIters
	 * (sorting on a different column, for instance).
	 */
	private TreeRowReference	previous	= null;

	/**
	 * Set a given row of the ListStore as as "active" (ie, selected) by
	 * repopulaing its DataColumns to have our _ACTIVE color values rather than
	 * _NORMAL ones. The row that was previously active is set back to "normal".
	 * 
	 * @param path
	 *            the TreePath (presumably extracted from a TreeSelection) that
	 *            you want to indicate as active.
	 */
	private void showAsActive(TreePath path) {
		TreeIter pointer = model.getIter(path);

		model.setValue(pointer, active_DataColumn, true);
		populate(pointer);

		if (previous != null) {
			if (!(previous.getPath().equals(path))) {
				pointer = model.getIter(previous.getPath());
				model.setValue(pointer, active_DataColumn, false);
				populate(pointer);
			}
		}
		previous = new TreeRowReference(model, path);
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
	 * ugly? Note that many of the Sort_DataColums fields are nice clean Strings
	 * generally directly being the underlying text data, whereas the Markup
	 * ones are where the pango mush goes (which is why we can't sort on them -
	 * it'd sort by colour!)
	 */
	private void populate(TreeIter pointer) {
		Transaction t = (Transaction) model.getValue(pointer, transactionObject_DataColumn);
		boolean active = model.getValue(pointer, active_DataColumn);

		StringBuffer type = new StringBuffer();

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

		StringBuffer titleName = new StringBuffer();
		final String OPEN = "<b>";
		final String CLOSE = "</b>";
		titleName.append(OPEN);
		titleName.append(t.getDescription());
		titleName.append(CLOSE);

		StringBuffer debitVal = new StringBuffer();
		debitVal.append(OPEN);
		debitVal.append(' ');
		debitVal.append(CLOSE);
		debitVal.append('\n');

		StringBuffer creditVal = new StringBuffer(debitVal.toString());

		/* ... in this Transaction */
		long largestDebitNumber = 0;
		long largestCreditNumber = 0;
		int widestDebitWidth = 0;
		int widestCreditWidth = 0;

		List amountBuffers = new ArrayList(3);
		List entryObjects = new ArrayList(3);

		Set ordered = new TreeSet(new EntryComparator(t));
		ordered.addAll(t.getEntries());
		Iterator eI = ordered.iterator();
		while (eI.hasNext()) {
			Entry entry = (Entry) eI.next();
			Ledger ledger = entry.getParentLedger();
			Account account = ledger.getParentAccount();

			titleName.append("\n");

			titleName.append("<span color='");
			titleName.append(account.getColor(active));
			titleName.append("'>");
			Matcher ma = regexAmp.matcher(account.getTitle());
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
			Matcher ml = regexAmp.matcher(ledger.getName());
			titleName.append(ml.replaceAll("&amp;"));
			titleName.append("</span>");

			Amount a = entry.getAmount();
			ForeignAmount fa;
			if (entry.getAmount() instanceof ForeignAmount) {
				fa = (ForeignAmount) a;
			} else {
				fa = new ForeignAmount();
				fa.setCurrency(home);
				fa.setRate("1.0");
				fa.setForeignValue(a);
			}
			StringBuffer value = new StringBuffer();

			value.append(fa.getCurrency().getSymbol());
			value.append(fa.toString()); // has , separators
			value.append(' ');
			if (active) {
				value.append("<span color='" + LIGHTGRAY + "'>");
			} else {
				value.append("<span color='" + DARKGRAY + "'>");
			}
			value.append(fa.getCurrency().getCode());
			value.append("</span>");

			amountBuffers.add(value);
			entryObjects.add(entry);

			/*
			 * We sort the entires by their face value; number from Amount
			 * represents underlying home quantity.
			 */
			long num = Math.round(Double.parseDouble(fa.getForeignValue()) * 100);
			if (entry instanceof Debit) {
				if (num > largestDebitNumber) {
					largestDebitNumber = num;
				}
				if (value.length() > widestDebitWidth) {
					widestDebitWidth = value.length();
				}
			} else if (entry instanceof Credit) {
				if (num > largestCreditNumber) {
					largestCreditNumber = num;
				}
				if (value.length() > widestCreditWidth) {
					widestCreditWidth = value.length();
				}
			}
		}

		/*
		 * Now we iterate over the Entries again, this time applying the max
		 * width to square the Strings, and then copying them to the appropriate
		 * left/right credit/debit buffer.
		 */

		final int num = entryObjects.size();
		for (int i = 0; i < num; i++) {
			Entry entry = (Entry) entryObjects.get(i);
			StringBuffer buf = (StringBuffer) amountBuffers.get(i);

			if (entry instanceof Debit) {
				int diff = widestDebitWidth - buf.length();
				for (int j = 0; j < diff; j++) {
					buf.insert(0, ' ');
				}
				debitVal.append(buf);
				debitVal.append("\n");
				creditVal.append("\n");

			} else if (entry instanceof Credit) {
				int diff = widestCreditWidth - buf.length();
				for (int j = 0; j < diff; j++) {
					buf.insert(0, ' ');
				}
				debitVal.append("\n");
				creditVal.append(buf);
				creditVal.append("\n");
			}
		}

		/*
		 * Trim the trailing newline (otherwise we get a blank line in the row)
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
	 * Drop the existing Set of Transactions shown by this widget and replace it
	 * with a new Set.
	 */
	public void setTransactions(List transactions) {
		model.clear();
		populate(transactions);
	}

	public void redisplayObject(long id) {
		Transaction t = (Transaction) db.fetchByID(id);
		db.reload(t);

		TreeIter pointer = model.getFirstIter();
		while (pointer != null) {
			if (model.getValue(pointer, transactionObject_DataColumn) == t) {
				Debug.print("listeners", "redisplayObject(" + id + ") called; repopulating TreeIter "
					+ pointer);
				populate(pointer);
				return;
			}
			pointer = pointer.getNextIter();
		}

		/*
		 * Well, not here. No biggie, unless we start registering ID to Window
		 * mappings up at UserInterface.
		 */
	}
}
