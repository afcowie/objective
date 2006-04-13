/*
 * EntryTextOutput.java
 * 
 * See LICENCE file for usage and redistribution terms
 * Copyright (c) 2005 Operational Dynamics
 */
package accounts.ui;

import generic.ui.TextOutput;

import java.io.PrintWriter;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import accounts.domain.Credit;
import accounts.domain.Debit;
import accounts.domain.Entry;
import accounts.domain.Ledger;
import accounts.domain.Transaction;
import accounts.persistence.DataStore;
import accounts.services.EntryComparator;

/**
 * Output the contents of a Transaction in a human readable text form.
 * 
 * @author Andrew Cowie
 */
public class EntryTextOutput extends TextOutput
{
	// "DD MMM YY "
	// 12345678901
	protected static final int	dateWidth	= 11;

	protected static final int	FLEX_MIN	= 25;
	protected static final int	idWidth;
	protected static final int	descWidth;
	// XXXXXXX.YY CR
	// 7777777123456
	protected static final int	amountWidth	= 13;	// of which there are two

	static {
		// less one to keep off the edge
		int remaining = COLUMNS - dateWidth - (2 * amountWidth) - 1;
		if (remaining < FLEX_MIN) {
			throw new IllegalStateException("Terminal too narrow for EntryTextOutput. Need width of at least "
				+ (dateWidth + FLEX_MIN + (2 * amountWidth)) + " characters.");
		}
		int piece = remaining / 4;
		idWidth = piece;
		descWidth = remaining - piece;
	}

	private Set					entries;
	private Object				context;

	/**
	 * @param store
	 *            a DataStore from which to fetch all instances of Transaction.
	 * @param context
	 *            an Object of the class (Ledger or Transaction) that you are
	 *            displaying Entries within.
	 */
	public EntryTextOutput(DataStore store, Object context) {
		List tL = store.queryByExample(Transaction.class);

		entries = new TreeSet(new EntryComparator(context));
		entries.addAll(tL);

		this.context = context;
	}

	/**
	 * @param entries
	 *            the Set of transactions to be output. It will be sorted by a
	 *            new TreeSet during instantiation.
	 */
	public EntryTextOutput(Transaction t) {
		this.entries = new TreeSet(new EntryComparator(t));
		entries.addAll(t.getEntries());
		this.context = t;
	}

	/**
	 * ... in Ledger context
	 * 
	 * @param ledger
	 *            the Ledger whose Entries you are outputting.
	 */
	public EntryTextOutput(Ledger l) {
		this.entries = new TreeSet(new EntryComparator(l));
		this.context = l;

		Set contains = l.getEntries();
		if (contains == null) {
			return;
		} else {
			this.entries.addAll(contains);
		}
	}

	/**
	 * @param t
	 *            a single Transaction to run the outputter over. Use for spot
	 *            debugging only.
	 * @param context
	 *            an Object of the class (Ledger or Transaction) that you are
	 *            displaying Entries within.
	 */
	public EntryTextOutput(Entry e, Object context) {
		/*
		 * Only one element, so Set type doesn't matter.
		 */
		entries = Collections.singleton(e);
		this.context = context;
	}

	/*
	 * Reminder: super has toOutput(PrintStream).
	 */

	/**
	 * Print a formatted text version of this Entry on a single line. As entries
	 * have a two-way relation between Ledgers and Transactions, it either
	 * displays the parent transaction, or the parent account, depending on the
	 * context set for this EntryTextOutput instance.
	 * 
	 * @param out
	 *            OutputWriter you want to print to (presumably passed in a
	 *            cascade)
	 */
	public void toOutput(PrintWriter out) {
		if (entries.size() == 0) {
			return;
		}

		Iterator eI = entries.iterator();
		while (eI.hasNext()) {
			Entry e = (Entry) eI.next();

			out.print(pad(e.getDate().toString(), dateWidth, LEFT));

			if (context instanceof Ledger) {
				String id = e.getParentTransaction().getIdentifier();
				if (id == null) {
					id = "";
				}
				out.print(pad(id + "  ", idWidth, RIGHT));

				String desc = e.getParentTransaction().getDescription();
				if (desc == null) {
					desc = "";
				}
				out.print(pad(chomp(desc, descWidth), descWidth, LEFT));
			} else if (context instanceof Transaction) {
				// skip Account identifier
				out.print(pad(e.getParentLedger().getParentAccount().getTitle() + "|" + e.getParentLedger().getName(),
					idWidth + descWidth, LEFT));
			} else {
				throw new IllegalStateException("context is not set to either Ledger or Transaction");
			}

			if (e instanceof Debit) {
				out.print(pad(e.toString(), amountWidth, RIGHT));
				out.print(pad("", amountWidth, RIGHT));
			} else if (e instanceof Credit) {
				out.print(pad("", amountWidth, RIGHT));
				out.print(pad(e.toString(), amountWidth, RIGHT));
			}
			out.println();
		}
	}
}
