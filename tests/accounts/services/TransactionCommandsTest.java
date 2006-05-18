/*
 * TransactionCommandsTest.java
 *
 * See LICENCE file for usage and redistribution terms
 * Copyright (c) 2005-2006 Operational Dynamics
 */
package accounts.services;

import generic.persistence.DataClient;
import generic.persistence.Engine;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

import accounts.domain.Amount;
import accounts.domain.Credit;
import accounts.domain.CreditPositiveLedger;
import accounts.domain.Datestamp;
import accounts.domain.Debit;
import accounts.domain.DebitPositiveLedger;
import accounts.domain.Entry;
import accounts.domain.GenericTransaction;
import accounts.domain.Ledger;
import accounts.domain.Transaction;
import accounts.persistence.BlankDatafileTestCase;

public class TransactionCommandsTest extends BlankDatafileTestCase
{
	static {
		DATAFILE = "tmp/unittests/TransactionCommandsTest.yap";

		// Debug.init("debug");
		// Debug.register("debug");
		// Debug.setProgname("tct");
	}

	/*
	 * Test method for the implicit completeness tests in
	 * PostTransactionCommand.action()'
	 */
	public void testTransactionCommandWithGeneric() {
		Credit price = new Credit(new Amount("1100.00"), null);
		Debit gst = new Debit(new Amount("100.00"), null);
		Debit expense = new Debit(new Amount("1000.00"), null);

		Entry[] entries = new Entry[] {
			price,
			gst,
			expense
		};

		Datestamp d = new Datestamp();
		d.setAsToday();

		GenericTransaction t = new GenericTransaction("Test transaction", d, entries);
		t.setReference("TEST01");

		assertTrue(t.isBalanced());

		PostTransactionCommand tc = new PostTransactionCommand(t);

		try {
			tc.execute(rw);
			fail("Should have thrown CommandNotReadyException to indicate unassigned parental relationships");
		} catch (CommandNotReadyException cnre) {
			// good
		}

		/*
		 * Fix the situation by assigining parent Ledgers, as should have
		 * happened originally.
		 */
		Ledger cashLedger = new DebitPositiveLedger("Petty Cash");
		Ledger blahExpense = new DebitPositiveLedger("Telephone Expense");
		Ledger gstPayable = new CreditPositiveLedger("GST Payable");

		price.setParentLedger(cashLedger);
		gst.setParentLedger(gstPayable);
		expense.setParentLedger(blahExpense);

		try {
			tc.execute(rw);
		} catch (CommandNotReadyException cnre) {
			fail("Threw " + cnre);
		}

		rw.commit();
	}

	/*
	 * Verify that the Transaction laid down by the above PostTransactionCommand
	 * actually persisted fully.
	 */
	public void testPersistence() {
		Transaction proto = new Transaction();

		List results = rw.queryByExample(proto);
		assertEquals(1, results.size());
		Transaction t = (Transaction) results.get(0);
		assertTrue(t instanceof GenericTransaction);
		GenericTransaction gt = (GenericTransaction) t;

		assertEquals("TEST01", gt.getReference());
		assertEquals("Test transaction", gt.getDescription());
		assertTrue(gt.isBalanced());

		Set entries = gt.getEntries();

		Iterator iter = entries.iterator();

		boolean price = false;
		boolean expense = false;
		boolean gst = false;
		int i = 0;

		while (iter.hasNext()) {
			i++;
			Entry e = (Entry) iter.next();
			if (e.getAmount().getValue().equals("1100.00")) {
				assertTrue(e instanceof Credit);
				price = true;
			}
			if (e.getAmount().getValue().equals("1000.00")) {
				assertTrue(e instanceof Debit);
				expense = true;
			}

			if (e.getAmount().getValue().equals("100.00")) {
				assertTrue(e instanceof Debit);
				gst = true;
			}
		}
		assertEquals(3, i);
		assertTrue(price && expense && gst);
	}

	public final void testDatestampSettingByPostTransactionCommand() {
		Ledger someLedger = new DebitPositiveLedger("Blah");
		/*
		 * Set up two Entries with differing dates
		 */
		Debit gauche = new Debit(new Amount("9.95"), someLedger);
		gauche.setDate(new Datestamp("1 Jul 95"));

		Credit droit = new Credit(new Amount("9.95"), someLedger);
		droit.setDate(new Datestamp("15 Oct 03"));

		/*
		 * Make a Transaction with them, but don't set a date for the
		 * Transaction, so addEntry can't impose a date.
		 */
		Transaction t3 = new Transaction();
		t3.addEntry(gauche);
		t3.addEntry(droit);

		/*
		 * At this point because the Transaction hasn't had a date set, the
		 * Datestamps won't be the same (this here just to detect behaviour
		 * change).
		 */
		assertFalse(gauche.getDate().toString().equals(droit.getDate().toString()));

		/*
		 * Now use a PostTransactionCommand to attempt to store the Transaction.
		 */
		PostTransactionCommand cmd = new PostTransactionCommand(t3);
		try {
			cmd.execute(rw);
			fail("Should have thrown CommandNotReadyException to signal that the Date hasn't been set");
		} catch (CommandNotReadyException cnre) {
			// correct
		}

		Datestamp someday = new Datestamp("30 Sep 04");
		t3.setDate(someday);

		/*
		 * Now the Entries dates should have all been set to the Transaction's
		 * date.
		 */
		assertTrue(gauche.getDate().toString().equals(droit.getDate().toString()));

		try {
			cmd.execute(rw);
		} catch (CommandNotReadyException cnre) {
			fail("Should NOT have thrown CommandNotReadyException " + cnre.getMessage());
		}

		/*
		 * no reason to commit to database; we've already tested that; more to
		 * the point, keep in clean with one Transaction.
		 */
		rw.rollback();
	}

	public final void testAddEntriesToLedgersOnExecute() {
		Ledger leftLedger = new DebitPositiveLedger("Left side");
		Ledger rightLedger = new CreditPositiveLedger("Right side");

		/*
		 * Set up three Entries with accounts (er, ledgers) specified
		 */
		Debit one = new Debit(new Amount("1.00"), leftLedger);
		Debit two = new Debit(new Amount("2.00"), leftLedger);
		Credit three = new Credit(new Amount("3.00"), rightLedger);

		/*
		 * Make a Transaction with them, but don't set a date for the
		 * Transaction, so addEntry can't impose a date.
		 */
		Transaction t4 = new Transaction();
		t4.addEntry(one);
		t4.addEntry(two);
		t4.addEntry(three);

		Datestamp today = new Datestamp();
		today.setAsToday();
		t4.setDate(today);

		/*
		 * Now use a PostTransactionCommand to attempt to store the Transaction.
		 */
		PostTransactionCommand cmd = new PostTransactionCommand(t4);

		try {
			cmd.execute(rw);
		} catch (CommandNotReadyException cnre) {
			fail("Should NOT have thrown CommandNotReadyException " + cnre.getMessage());
		}

		assertEquals("3.00", rightLedger.getBalance().getValue());
		assertEquals("3.00", leftLedger.getBalance().getValue());

		// no reason to commit to database; we've already tested that.
		rw.rollback();
	}

	public final void testUpdateTransaction() throws Exception {
		List result = rw.queryByExample(Transaction.class);
		assertEquals(1, result.size());

		Transaction t = (Transaction) result.get(0);
		Set entries = t.getEntries();
		assertEquals(3, entries.size());

		Credit price = null;
		Debit gst = null;
		Debit expense = null;

		Iterator eI = entries.iterator();
		while (eI.hasNext()) {
			Entry e = (Entry) eI.next();
			if (e instanceof Credit) {
				price = (Credit) e;
			} else if (e.getAmount().getNumber() == 10000) {
				gst = (Debit) e;
			} else if (e.getAmount().getNumber() == 100000) {
				expense = (Debit) e;
			} else {
				throw new Exception("What Entry did you retrieve?");
			}
		}
		assertNotNull(price);
		assertNotNull(gst);
		assertNotNull(expense);

		/*
		 * Ok, setup complete. Now alter the Entries, changing values and more
		 * importantly removing one:
		 */
		price.getAmount().setValue("2200.00");
		expense.getAmount().setValue("2200.00");
		t.removeEntry(gst);

		UpdateTransactionCommand utc = new UpdateTransactionCommand(t);
		utc.execute(rw);

		rw.commit();

		DataClient another = Engine.gainClient();

		result = another.queryByExample(Transaction.class);
		assertEquals(1, result.size());

		Transaction at = (Transaction) result.get(0);
		entries = at.getEntries();
		assertEquals(2, entries.size());
		Iterator iter = entries.iterator();
		assertTrue(iter.hasNext());
		Entry first = (Entry) iter.next();
		assertEquals(220000, first.getAmount().getNumber());

		assertTrue(iter.hasNext());
		Entry second = (Entry) iter.next();
		assertEquals(220000, second.getAmount().getNumber());
		assertFalse(iter.hasNext());

		/*
		 * Now make sure the GST entry was indeed deleted:
		 */

		result = another.queryByExample(Entry.class);
		assertEquals("If there are 3 Entry objects, then UpdateTransactionCommand didn't do its job", 2,
			result.size());

		Engine.releaseClient(another);

		/*
		 * Next problem: changing the Ledger that an Entry is in
		 */

		Ledger revenueBlah = new CreditPositiveLedger("Revenue Blah");
		expense.setParentLedger(revenueBlah);

		utc = new UpdateTransactionCommand(t);
		utc.execute(rw);

		rw.commit();

		another = Engine.gainClient();
		result = another.queryByExample(Transaction.class);
		assertEquals(1, result.size());

		at = (Transaction) result.get(0);
		entries = at.getEntries();
		assertEquals(2, entries.size());
		iter = entries.iterator();
		assertTrue(iter.hasNext());
		first = (Entry) iter.next();
		assertEquals(220000, first.getAmount().getNumber());
		assertTrue(first instanceof Credit);
		Ledger firstParent = first.getParentLedger();
		assertTrue(firstParent instanceof DebitPositiveLedger);
		assertEquals("Petty Cash", firstParent.getName());

		assertTrue(iter.hasNext());
		second = (Entry) iter.next();
		assertTrue(second instanceof Debit);
		assertEquals(220000, second.getAmount().getNumber());
		Ledger secondParent = second.getParentLedger();

		// another.reload(secondParent);

		/*
		 * Hello. If you're running unit tests, and they fail here, then you've
		 * probably run into an activation problem related to the fact that
		 * "another" didn't actually have it's stale data cleared after all.
		 * Damn and other comments.
		 */
		assertTrue(secondParent instanceof CreditPositiveLedger);
		assertEquals("Revenue Blah", secondParent.getName());

		assertFalse(iter.hasNext());

		Engine.releaseClient(another);
	}
}
