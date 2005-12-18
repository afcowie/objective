/*
 * PostTransactionCommandTest.java
 *
 * See LICENCE file for usage and redistribution terms
 * Copyright (c) 2005 Operational Dynamics
 */
package accounts.services;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import com.db4o.ObjectContainer;
import com.db4o.ObjectSet;

import accounts.client.ObjectiveAccounts;
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
import accounts.persistence.UnitOfWork;
import junit.framework.TestCase;

public class PostTransactionCommandTest extends TestCase
{
	public static final String	TESTS_DATABASE	= "tmp/unittests/PostTransactionCommandTest.yap";
	private static boolean		initialized		= false;

	private void init() {
		new File(TESTS_DATABASE).delete();
		ObjectiveAccounts.store = DatafileServices.newDatafile(TESTS_DATABASE);
		ObjectiveAccounts.store.close();
		initialized = true;
	}

	public void setUp() {
		if (!initialized) {
			init();
		}
		try {
			ObjectiveAccounts.store = DatafileServices.openDatafile(TESTS_DATABASE);
		} catch (FileNotFoundException fnfe) {
			fail("Where is the test database?");
		}
	}

	public void tearDown() {
		ObjectiveAccounts.store.close();
	}

	/*
	 * Test method for the implicit completeness tests in
	 * PostTransactionCommand.action()'
	 */
	public void testTransactionCommandWithGeneric() {
		HashSet entries = new HashSet();

		Credit price = new Credit(new Amount("1100.00"), null);
		Debit gst = new Debit(new Amount("100.00"), null);
		Debit expense = new Debit(new Amount("1000.00"), null);

		entries.add(price);
		entries.add(gst);
		entries.add(expense);

		GenericTransaction t = new GenericTransaction("Test transaction", entries);
		t.setIdentifier("TEST01");
		Datestamp d = new Datestamp();
		d.setAsToday();
		t.setDate(d);

		assertTrue(t.isBalanced());

		UnitOfWork uow = new UnitOfWork("testTransactionCommandWithGeneric");
		PostTransactionCommand tc = new PostTransactionCommand(t);

		try {
			tc.execute(uow);
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
			tc.execute(uow);
		} catch (CommandNotReadyException cnre) {
			fail("Threw " + cnre);
		}

		uow.commit();
	}

	/*
	 * Verify that the Transaction laid down by the above PostTransactionCommand
	 * actually persisted fully.
	 */
	public void testPersistence() {
		Transaction proto = new Transaction();

		ObjectContainer container = ObjectiveAccounts.store.getContainer();
		ObjectSet results = container.get(proto);
		assertEquals(1, results.size());
		Transaction t = (Transaction) results.next();
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
		UnitOfWork uow = new UnitOfWork("testDatestampSettingByPostTransactionCommand");

		PostTransactionCommand cmd = new PostTransactionCommand(t3);
		try {
			cmd.execute(uow);
			uow.cancel();
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
			cmd.execute(uow);
		} catch (CommandNotReadyException cnre) {
			uow.cancel();
			fail("Should NOT have thrown CommandNotReadyException " + cnre.getMessage());
		}
		// no reason to commit to database; we've already tested that.
		uow.cancel();
	}
}
