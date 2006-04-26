/*
 * PostTransactionCommandTest.java
 *
 * See LICENCE file for usage and redistribution terms
 * Copyright (c) 2005-2006 Operational Dynamics
 */
package accounts.services;

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

public class PostTransactionCommandTest extends BlankDatafileTestCase
{
	static {
		DATAFILE = "tmp/unittests/PostTransactionCommandTest.yap";
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
		t.setIdentifier("TEST01");

		assertTrue(t.isBalanced());

		PostTransactionCommand tc = new PostTransactionCommand(t);

		try {
			tc.execute(rw);
			fail("Should have thrown CommandNotReadyException to indicate unassigned parental relationships");
		} catch (CommandNotReadyException cnre) {
		}

		/*
		 * Fix the situation by assigining parent Ledgers, as should have
		 * happened originally.
		 */
		Ledger cashLedger = new DebitPositiveLedger("Petty Cash");
		Ledger blahExpense = new DebitPositiveLedger("Blah Expense");
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

		assertEquals("TEST01", gt.getIdentifier());
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
		// no reason to commit to database; we've already tested that.
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
	}
}
