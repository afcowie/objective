/*
 * UpdateTransactionCommand.java
 * 
 * See LICENCE file for usage and redistribution terms
 * Copyright (c) 2006 Operational Dynamics
 */
package accounts.services;

import generic.persistence.DataClient;
import generic.persistence.Selector;
import generic.util.Debug;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

import accounts.domain.Entry;
import accounts.domain.Ledger;
import accounts.domain.Transaction;

/**
 * Update an existing Transaction already stored in the database. This is a
 * tricker case than PostTransactionCommand because of the fact that in addition
 * to having modified Entry objects, the Transaction object being passed to
 * save() may well have new Entry objects (no biggie) but equally ones that have
 * been removed. If we don't delete these they will become orphans - unlike a
 * live VM garbage collecting unreachable objects, unused objects are not
 * arbitrarily removed from the persistence store!
 * <p>
 * The following is assumed: a client or ui layer class making modifications to
 * a Transaction and asking UpdateTransactionCommand to persist these changes
 * <b>must call Transaction.removeEntry() and addEntry() for Entries that are
 * not simply being modified</b>.
 * <p>
 * This Command will then:
 * <ul>
 * <li>remove the Transaction's old Entries reducing the balances in the
 * various Ledgers to which they bridged.
 * <li>Add the new Entries to the parent Ledgers,
 * <li>then update the Transaction by calling DataClient.save().
 * </ul>
 */
public class UpdateTransactionCommand extends TransactionCommand
{
	public UpdateTransactionCommand(Transaction t) {
		super(t);
	}

	protected void action(DataClient store) throws CommandNotReadyException {
		super.validate();

		Transaction original;
		try {
			original = (Transaction) store.peek(transaction);
		} catch (NoSuchElementException nsoe) {
			throw new CommandNotReadyException(
				"If we can't get the persisted version of the Transaction, how on earth are we updating it? "
					+ nsoe);
		}

		/*
		 * We do a native query to pull up the committed list of stored Entries
		 * as presently committed under this Transaction object. We then can
		 * test each Entry see if it's present in the live entries Set and act
		 * accordingly.
		 */

		Set liveEntries = transaction.getEntries();
		List missingEntries = new ArrayList();
		List changedLedgers = new ArrayList();

		class EntryContainsTransaction extends Selector
		{
			private long	targetId;

			EntryContainsTransaction(Transaction t) {
				this.targetId = t.getID();
			}

			public boolean match(Entry entry) {
				Transaction parent = entry.getParentTransaction();

				if (parent.getID() == targetId) {
					return true;
				}
				return false;
			}
		}
		List storedEntries = store.nativeQuery(new EntryContainsTransaction(transaction));

		Iterator iter = storedEntries.iterator();
		while (iter.hasNext()) {
			Entry ref = (Entry) iter.next();

			/*
			 * Remove the Entry from the parent Ledger to reduce it's balance,
			 * and if it's "missing" from the current Set, delete the Entry.
			 */

			class LedgerContainingEntry extends Selector
			{
				private Entry	target;

				LedgerContainingEntry(Entry e) {
					this.target = e;
				}

				public boolean match(Ledger ledger) {
					Set entries = ledger.getEntries();
					Iterator iter = entries.iterator();
					while (iter.hasNext()) {
						Entry entry = (Entry) iter.next();
						if (entry.congruent(target)) {
							return true;
						}
					}
					return false;
				}
			}

			List ledgerContainingEntry = store.nativeQuery(new LedgerContainingEntry(ref));

			Debug.print("debug", "Removing "
				+ ref.toString()
				+ " from Ledger \""
				+ (ledgerContainingEntry.size() == 1
					? ((Ledger) ledgerContainingEntry.get(0)).getName()
					: "[no! (" + ledgerContainingEntry.size() + ")]") + "\"");

			Ledger ledger = (Ledger) ledgerContainingEntry.get(0);

			ledger.removeEntry(ref);

			if (liveEntries.contains(ref)) {
				// persisted below
			} else {
				missingEntries.add(ref);
			}
			store.save(ledger);
		}

		/*
		 * We now update the Transaction by [re-]adding its Entries to the
		 * various parent Ledgers and then simply save()ing it; our persistence
		 * engine will take care of adding new objects and updating existing
		 * ones.
		 */

		Iterator eI = liveEntries.iterator();
		while (eI.hasNext()) {
			Entry e = (Entry) eI.next();
			Debug.print("debug", "Adding " + e.toString() + " to Ledger \""
				+ e.getParentLedger().getName() + "\"");
			e.getParentLedger().addEntry(e);
			// store.save(e);
			// store.save(e.getParentLedger());
		}

		store.save(transaction);

		/*
		 * With that taken care of, we now clean up our mess by deleting the
		 * Entries which are no longer in use.
		 */
		Iterator mI = missingEntries.iterator();
		while (mI.hasNext()) {
			Entry m = (Entry) mI.next();
			store.delete(m);
		}
	}

	protected void reverse(DataClient client) throws CommandNotUndoableException {
		throw new UnsupportedOperationException();
	}

	public String getClassString() {
		return "Update Transaction";
	}
}
