/*
 * BasicCommandTest.java
 * 
 * See LICENCE file for usage and redistribution terms
 * Copyright (c) 2005 Operational Dynamics
 */
package accounts.services;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;

import junit.framework.TestCase;
import accounts.client.ObjectiveAccounts;
import accounts.domain.Account;
import accounts.domain.Books;
import accounts.domain.CashAccount;
import accounts.domain.Currency;
import accounts.domain.Ledger;
import accounts.persistence.UnitOfWork;

public class BasicCommandTest extends TestCase
{
	public static final String	TESTS_DATABASE	= "tmp/unittests/BasicCommandTest.yap";
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

	public final void testInitBooksCommand() {
		try {
			Books one = ObjectiveAccounts.store.getBooks();
			fail("Should have thrown NoSuchElementException to point out uninitialized DataStore.");
		} catch (NoSuchElementException nsee) {
		}

		UnitOfWork uow = new UnitOfWork("testInitBooksCommand");

		InitBooksCommand ibc = null;
		/*
		 * test readiness now that we've added home Currency selection.
		 */
		try {
			ibc = new InitBooksCommand(null);
			fail("should have thrown IllegalArgumentException as the home Currency hasn't been set");
		} catch (IllegalArgumentException iae) {
		} catch (Exception other) {
			fail("should have thrown IllegalArgumentException, not " + other.toString());
		}

		Currency home = new Currency("AUD", "Aussie Dollar", "$");
		try {
			ibc = new InitBooksCommand(home);
		} catch (Exception e) {
			fail("shouldn't have thrown an Exception from constructor");
		}

		try {
			ibc.execute(uow);
		} catch (CommandNotReadyException cnre) {
			fail("threw CommandNotReadyException");
		}
		uow.commit();

		Books two = ObjectiveAccounts.store.getBooks();
		assertNotNull("Should be a Books object by now", two);

		Set accounts = two.getAccountsSet();
		assertNotNull("Should be an Account Set now", accounts);
	}

	public final void testPersistenceCascade() {
		Books root = ObjectiveAccounts.store.getBooks();
		assertNotNull("Should be a Books object, established by static block, available for retrieval", root);

		// ObjectContainer container = ObjectiveAccounts.store.getContainer();
		// ObjectClass objcls = Db4o.configure().objectClass(root);
		// objcls.
		/*
		 * How can we test this?
		 */

		Set accounts = root.getAccountsSet();
		assertNotNull("Should be an Account Set stored and retreivable", accounts);
	}

	public final void testAddAccountCommand() {
		Books root = ObjectiveAccounts.store.getBooks();
		assertNotNull("Should *still* be a Books object available, established by static block", root);

		CashAccount pettyCash = new CashAccount("Petty Cash", "Manly Office");
		pettyCash.setCode("1-1201");

		UnitOfWork uow = new UnitOfWork("testAddAccountCommand");
		AddAccountCommand aac = new AddAccountCommand(pettyCash);

		try {
			aac.execute(uow);
		} catch (CommandNotReadyException cnre) {
			fail("threw CommandNotReadyException");
		}
		uow.commit();

		Set accounts = root.getAccountsSet();
		assertNotNull("Should still be an Account Set", accounts);

		Iterator iter = accounts.iterator();
		assertTrue("Shold be an Object to iterate over", iter.hasNext());
		Account account = (Account) iter.next();
		/*
		 * Should only be one account, and not only should be the petty cash
		 * account we created above, but it should be that object!
		 */
		assertEquals("Account retreived should match the one we stored", "1-1201", account.getCode());
		assertTrue("Furthermore, it should BE the object we store", account.equals(pettyCash));
	}

	public final void testAccountLedgerUpdateCascade() {
		Books root = ObjectiveAccounts.store.getBooks();
		Set accounts = root.getAccountsSet();
		Iterator iter = accounts.iterator();

		Account account = (Account) iter.next();
		assertNotNull(account);
		assertEquals("Account retreived should match the one we stored", "1-1201", account.getCode());

		Set ledgers = account.getLedgers();
		assertNotNull("The set of Ledgers should have been persisted", ledgers);
		Iterator ledgersIter = ledgers.iterator();
		assertTrue("There should be a Ledger", ledgersIter.hasNext());
		Ledger ledger = (Ledger) ledgersIter.next();
		assertEquals("The Ledger should be the one we expect!", "Manly Office", ledger.getName());
	}
}
