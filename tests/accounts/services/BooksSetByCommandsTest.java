/*
 * BooksSetByCommandsTest.java
 * 
 * See LICENCE file for usage and redistribution terms
 * Copyright (c) 2005-2006 Operational Dynamics
 */
package accounts.services;

import java.util.NoSuchElementException;

import junit.framework.TestCase;
import accounts.client.ObjectiveAccounts;
import accounts.domain.Account;
import accounts.domain.Books;
import accounts.domain.Currency;
import accounts.persistence.BlankDatafileTestCase;
import accounts.persistence.DataClient;
import accounts.persistence.Engine;

public class BooksSetByCommandsTest extends BlankDatafileTestCase
{
	static {
		DATAFILE = "tmp/unittests/BooksSetByCommandsTest.yap";
	}

	public final void testDataStoreGetSetGetBooks() {
		Books root;
		try {
			root = rw.getBooks();
			fail("Should have thrown NoSuchElementException to point out uninitialized DataClient.");
		} catch (NoSuchElementException nsee) {
		}

		try {
			rw.setBooks(null);
			fail("Should have thrown IllegalArgumentException due to null argument");
		} catch (IllegalArgumentException iae) {
		}

		root = new Books();
		rw.setBooks(root);

		try {
			Books foo = new Books();
			rw.setBooks(foo);
			fail("Should have thrown UnsupportedOperationException due to already set (though cached, not saved) books");
		} catch (UnsupportedOperationException uoe) {
		}
		Books received = rw.getBooks();
		assertNotNull(received);
		assertSame(root, received);
	}

	public final void testInitBooksAndAddAccountsInOneUnitOfWork() throws Exception {
		/*
		 * Reopen the database to make sure that the DataClients we get are for
		 * sure uninitialized.
		 */
		Engine.releaseClient(rw);
		Engine.shutdown();
		Engine.openDatafile(DATAFILE);
		rw = Engine.gainClient();

		try {
			Books one = rw.getBooks();
			fail("Should have thrown NoSuchElementException to point out uninitialized database.");
		} catch (NoSuchElementException nsee) {
		}

		InitBooksCommand ibc = null;
		/*
		 * Initialize a Books
		 */
		try {
			Currency home = new Currency("USD", "US Dollar", "$");
			ibc = new InitBooksCommand(home);
			ibc.execute(rw);
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
			aac.execute(rw);
		} catch (NoSuchElementException nsee) {
			rw.rollback();
			fail("aac.execute() threw a NoSuchElementException, as per bug");
		} catch (Exception e) {
			throw e;
		}
		/*
		 * Makes it this far, then InitBooksCommand and DataClient cached the
		 * Books instead of insisting on lookup from database. Go ahead and
		 * commit now.
		 */
		rw.commit();

		try {
			Books foo = new Books();
			rw.setBooks(foo);
			fail("Should have thrown UnsupportedOperationException due to already set (by InitBooksCommand) books");
		} catch (UnsupportedOperationException uoe) {
		}
	}

	public final void testUseCachedBooks() {
		/*
		 * We should probably get the same client as above back, so its Books is
		 * cached up...
		 */
		Books received = rw.getBooks();
		assertNotNull(received);
		/*
		 * But a brand new DataClient won't have a Books cached. Go fetch!
		 */
		DataClient fresh = Engine.server.gainClient();
		Books another = rw.getBooks();
		assertNotNull(another);
		Engine.releaseClient(fresh);
	}
}
