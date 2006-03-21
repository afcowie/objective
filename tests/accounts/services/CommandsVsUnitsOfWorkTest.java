/*
 * CommandsVsUnitsOfWorkTest.java
 * 
 * See LICENCE file for usage and redistribution terms
 * Copyright (c) 2005 Operational Dynamics
 */
package accounts.services;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.NoSuchElementException;

import junit.framework.TestCase;
import accounts.client.ObjectiveAccounts;
import accounts.domain.Account;
import accounts.domain.Books;
import accounts.domain.Currency;
import accounts.domain.Datestamp;
import accounts.persistence.UnitOfWork;

public class CommandsVsUnitsOfWorkTest extends TestCase
{
	public static final String	TESTS_DATABASE	= "tmp/unittests/CommandsVsUnitsOfWorkTest.yap";
	private static boolean		initialized		= false;

	private void init() {
		try {
			new File(TESTS_DATABASE).delete();
			ObjectiveAccounts.store = DatafileServices.newDatafile(TESTS_DATABASE);
			ObjectiveAccounts.store.close();
			initialized = true;
		} catch (Exception e) {
			System.err.println("Unexpected problem in init()!");
			System.err.flush();
		}
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
		try {
			if (!ObjectiveAccounts.store.getContainer().ext().isClosed()) {
				ObjectiveAccounts.store.close();
			} else
				throw new Exception("closed?!?");
		} catch (Exception e) {
			System.err.println("What the hell? " + e);
			System.err.flush();
		}
	}

	public final void testDataStoreGetSetGetBooks() {
		Books root;
		try {
			root = ObjectiveAccounts.store.getBooks();
			fail("Should have thrown NoSuchElementException to point out uninitialized DataStore.");
		} catch (NoSuchElementException nsee) {
		}

		try {
			ObjectiveAccounts.store.setBooks(null);
			fail("Should have thrown IllegalArgumentException due to null argument");
		} catch (IllegalArgumentException iae) {
		}

		root = new Books();
		ObjectiveAccounts.store.setBooks(root);

		try {
			Books foo = new Books();
			ObjectiveAccounts.store.setBooks(foo);
			fail("Should have thrown UnsupportedOperationException due to already set (though cached, not saved) books");
		} catch (UnsupportedOperationException uoe) {
		}
		Books received = ObjectiveAccounts.store.getBooks();
		assertNotNull(received);
		assertSame(root, received);
	}

	public final void testInitBooksAndAddAccountsInOneUnitOfWork() throws Exception {
		try {
			Books one = ObjectiveAccounts.store.getBooks();
			fail("Should have thrown NoSuchElementException to point out uninitialized DataStore.");
		} catch (NoSuchElementException nsee) {
		}

		UnitOfWork uow = new UnitOfWork("testInitBooksAndAddAccountsInOneUnitOfWork");

		InitBooksCommand ibc = null;
		/*
		 * Initialize a Books
		 */
		try {
			Currency home = new Currency("USD", "US Dollar", "$");
			ibc = new InitBooksCommand(home);
			ibc.execute(uow);
		} catch (Exception e) {
			fail("InitBooksCommand shouldn't have thrown anything, let alone " + e);
		}

		/*
		 * Now add an account, but NOT having committed the UnitOfWork. At time
		 * of writing test, this caused a bug. Trap it in this clause and fail.
		 */
		try {
			Account a = new Account("Dummy");
			AddAccountCommand aac = new AddAccountCommand(a);
			aac.execute(uow);
		} catch (NoSuchElementException nsee) {
			uow.cancel();
			fail("aac.execute() threw a NoSuchElementException, as per bug");
		} catch (Exception e) {
			throw e;
		}
		/*
		 * Makes it this far, then InitBooksCommand and DataStore cached the
		 * Books instead of insisting on lookup from database. Go ahead and
		 * commit now.
		 */
		uow.commit();

		try {
			Books foo = new Books();
			ObjectiveAccounts.store.setBooks(foo);
			fail("Should have thrown UnsupportedOperationException due to already set (by InitBooksCommand) books");
		} catch (UnsupportedOperationException uoe) {
		}
	}

	public final void testUseCachedBooks() {
		/*
		 * This time we have a fresh store and so Books should not be cached. Go
		 * fetch!
		 */
		Books received = ObjectiveAccounts.store.getBooks();
		assertNotNull(received);
	}

	/**
	 * Not much to this one; its just a wrapper around DataStore.save()
	 */
	public final void testStoreObjectCommand() {
		UnitOfWork uow = new UnitOfWork("testInitBooksAndAddAccountsInOneUnitOfWork");

		Datestamp virgin = new Datestamp("18 Mar 96");

		try {
			StoreObjectCommand soc = new StoreObjectCommand(virgin);
			soc.execute(uow);
		} catch (Exception e) {
			fail("InitBooksCommand shouldn't have thrown anything, let alone " + e);
		}
		uow.commit();

		List l = ObjectiveAccounts.store.queryByExample(virgin);

		assertEquals(1, l.size());

		Datestamp slut = (Datestamp) l.get(0);
		assertSame(slut, virgin);
	}
}
