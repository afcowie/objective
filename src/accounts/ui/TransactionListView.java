/*
 * TransactionListView.java
 * 
 * See LICENCE file for usage and redistribution terms
 * Copyright (c) 2006 Operational Dynamics
 */
package accounts.ui;

import generic.persistence.DataClient;
import generic.ui.Text;
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
import org.gnu.gtk.DataColumnInt;
import org.gnu.gtk.DataColumnObject;
import org.gnu.gtk.DataColumnString;
import org.gnu.gtk.ListStore;
import org.gnu.gtk.SelectionMode;
import org.gnu.gtk.TreeIter;
import org.gnu.gtk.TreeSelection;
import org.gnu.gtk.TreeView;
import org.gnu.gtk.TreeViewColumn;
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

public class TransactionListView extends TreeView
{
	private transient Currency	home	= null;

	DataColumnString			typeMarkup_DataColumn;
	DataColumnString			dateText_DataColumn;
	DataColumnInt				dateSort_DataColumn;
	DataColumnString			descriptionAccountLedgerText_DataColumn;
	DataColumnString			descriptionSort_DataColumn;
	DataColumnString			debitAmountsText_DataColumn;
	DataColumnInt				debitAmountsSort_DataColumn;
	DataColumnString			creditAmountsText_DataColumn;
	DataColumnInt				creditAmountsSort_DataColumn;
	DataColumnObject			transactionObject_DataColumn;
	ListStore					listStore;

	TreeView					view;

	public TransactionListView(DataClient db, List transactions) {
		super();

		Books root = (Books) db.getRoot();
		home = root.getHomeCurrency();

		typeMarkup_DataColumn = new DataColumnString();
		dateText_DataColumn = new DataColumnString();
		dateSort_DataColumn = new DataColumnInt();
		descriptionAccountLedgerText_DataColumn = new DataColumnString();
		descriptionSort_DataColumn = new DataColumnString();
		debitAmountsText_DataColumn = new DataColumnString();
		debitAmountsSort_DataColumn = new DataColumnInt();
		creditAmountsText_DataColumn = new DataColumnString();
		creditAmountsSort_DataColumn = new DataColumnInt();
		transactionObject_DataColumn = new DataColumnObject();

		listStore = new ListStore(new DataColumn[] {
			typeMarkup_DataColumn,
			dateText_DataColumn,
			dateSort_DataColumn,
			descriptionAccountLedgerText_DataColumn,
			descriptionSort_DataColumn,
			debitAmountsText_DataColumn,
			debitAmountsSort_DataColumn,
			creditAmountsText_DataColumn,
			creditAmountsSort_DataColumn,
			transactionObject_DataColumn
		});

		populate(transactions);

		// since this might change away from a direct subclass
		view = this;

		view.setModel(listStore);

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
		type_ViewColumn.setSortColumn(typeMarkup_DataColumn); // FIXME Sort

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

		/*
		 * This does indeed set the foreground text colour, but manual settings
		 * via Pango <span color=""> markup override foreground and so are
		 * unaffected. Damn.
		 */
		// view.setTextColor(StateType.SELECTED, Color.YELLOW);
		date_ViewColumn.click();

		TreeSelection selection = view.getSelection();
		selection.setMode(SelectionMode.SINGLE);

		view.addListener(new TreeViewListener() {
			public void treeViewEvent(TreeViewEvent event) {
				if (event.getType() == TreeViewEvent.Type.MOVE_CURSOR) {
					Debug.print("listeners", "TreeViewEvent: " + event.getType().getName() + " "
						+ event.getMovementStep() + "," + event.getHowMany());
				}

			}
		});

	}

	private static final Pattern	regexAmp	= Pattern.compile("&");

	private static final String		DARKGRAY	= "darkgray";

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
			TreeIter pointer = listStore.appendRow();

			StringBuffer type = new StringBuffer();
			// type.append("<span color='darkgray'>");
			type.append(Text.wrap(t.getClassString(), 5));
			// type.append("</span>");

			listStore.setValue(pointer, typeMarkup_DataColumn, type.toString());

			listStore.setValue(pointer, dateText_DataColumn, "<span font_desc='Mono'>"
				+ t.getDate().toString() + "</span>");

			final long timestamp = t.getDate().getInternalTimestamp();
			final long smaller = timestamp / 1000;
			final int datei = Integer.valueOf(Long.toString(smaller)).intValue();
			listStore.setValue(pointer, dateSort_DataColumn, datei);

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

				titleName.append("<span color='" + account.getColor() + "'>");
				Matcher ma = regexAmp.matcher(account.getTitle());
				titleName.append(ma.replaceAll("&amp;"));
				titleName.append("</span>");
				/*
				 * We use » \u00bb. Other possibilities: ∞ \u221e, and ⑆ \u2446.
				 */
				titleName.append("<span color='" + CHARCOAL + "'>");
				titleName.append(" \u00bb ");
				titleName.append("</span>");

				titleName.append("<span color='" + ledger.getColor() + "'>");
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
				value.append("<span color='" + DARKGRAY + "'>");
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
			 * width to square the Strings, and then copying them to the
			 * appropriate left/right credit/debit buffer.
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
			 * Trim the trailing newline (otherwise we get a blank line in the
			 * row)
			 */
			debitVal.deleteCharAt(debitVal.length() - 1);
			creditVal.deleteCharAt(creditVal.length() - 1);

			/*
			 * And set it monospaced (though not terminal - is there a better
			 * attribute to set?)
			 */
			debitVal.insert(0, "<span font_desc='mono' color='" + Debit.COLOR + "'>");
			debitVal.append("</span>");
			creditVal.insert(0, "<span font_desc='mono' color='" + Credit.COLOR + "'>");
			creditVal.append("</span>");

			/*
			 * Now add the data for the Entries related columns:
			 */
			listStore.setValue(pointer, descriptionAccountLedgerText_DataColumn, titleName.toString());
			listStore.setValue(pointer, descriptionSort_DataColumn, t.getDescription());

			listStore.setValue(pointer, debitAmountsText_DataColumn, debitVal.toString());
			listStore.setValue(pointer, creditAmountsText_DataColumn, creditVal.toString());

			final int dri = Integer.valueOf(Long.toString(largestDebitNumber)).intValue();
			listStore.setValue(pointer, debitAmountsSort_DataColumn, dri);

			final int cri = Integer.valueOf(Long.toString(largestCreditNumber)).intValue();
			listStore.setValue(pointer, creditAmountsSort_DataColumn, cri);

			listStore.setValue(pointer, transactionObject_DataColumn, t);
		}
	}

	/**
	 * Drop the existing Set of Transactions shown by this widget and replace it
	 * with a new Set.
	 */
	public void setTransactions(List transactions) {
		listStore.clear();
		populate(transactions);
	}
}
