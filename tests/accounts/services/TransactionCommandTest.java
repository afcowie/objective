/*
 * TransactionCommandTest.java
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
import accounts.domain.Debit;
import accounts.domain.Entry;
import accounts.domain.GenericTransaction;
import accounts.domain.Transaction;
import junit.framework.TestCase;

public class TransactionCommandTest extends TestCase
{
	public static final String	TESTS_DATABASE	= "tmp/unittests/TransactionCommandTest.yap";
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
	 * Test method for 'accounts.services.TransactionCommand.isComplete()'
	 */
	public void testTransactionCommandWithGeneric() {
		HashSet entries = new HashSet();

		Amount price = new Amount("1100.00");
		Amount gst = new Amount("100.00");
		Amount expense = new Amount("1000.00");

		entries.add(new Credit(price));
		entries.add(new Debit(expense));
		entries.add(new Debit(gst));

		GenericTransaction t = new GenericTransaction("Test transaction", entries);
		t.setIdentifier("TEST01");

		assertTrue(t.isBalanced());

		TransactionCommand tc = new TransactionCommand();
		tc.setTransaction(t);

		assertTrue(tc.isComplete());
		try {
			tc.execute();
		} catch (CommandNotReadyException cnre) {
			fail("Threw " + cnre);
		}
	}

	/*
	 * Test method for 'accounts.services.Command.commit()'
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
}
