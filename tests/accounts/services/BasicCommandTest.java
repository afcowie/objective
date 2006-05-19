/*
 * BasicCommandTest.java
 * 
 * See LICENCE file for usage and redistribution terms
 * Copyright (c) 2005-2006 Operational Dynamics
 */
package accounts.services;

import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

import accounts.domain.Account;
import accounts.domain.Books;
import accounts.domain.CashAccount;
import accounts.domain.Currency;
import accounts.domain.Datestamp;
import accounts.domain.Ledger;
import accounts.persistence.BlankDatafileTestCase;

public class BasicCommandTest extends BlankDatafileTestCase
{
	static {
		DATAFILE = "tmp/unittests/BasicCommandTest.yap";
	}

	public final void testInitBooksCommand() {
		try {
			Books one = (Books) rw.getRoot();
			fail("Should have thrown NoSuchElementException to point out uninitialized database.");
		} catch (NoSuchElementException nsee) {
		}

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
			ibc.execute(rw);
		} catch (CommandNotReadyException cnre) {
			fail("threw CommandNotReadyException");
		}
		rw.commit();

		Books two = (Books) rw.getRoot();
		assertNotNull("Should be a Books object by now", two);

		Set accounts = two.getAccountsSet();
		assertNotNull("Should be an Account Set now", accounts);
	}

	public final void testPersistenceCascade() {
		Books root = (Books) rw.getRoot();
		assertNotNull("Should be a Books object, established by previous test, available for retrieval",
			root);

		Set accounts = root.getAccountsSet();
		assertNotNull("Should be an Account Set stored and retreivable", accounts);
	}

	public final void testAddAccountCommand() {
		Books root = (Books) rw.getRoot();
		assertNotNull("Should *still* be a Books object available, established by static block", root);

		CashAccount pettyCash = new CashAccount("Petty Cash", "Manly Office");
		pettyCash.setCode("1-1201");

		AddAccountCommand aac = new AddAccountCommand(pettyCash);

		try {
			aac.execute(rw);
		} catch (CommandNotReadyException cnre) {
			fail("threw CommandNotReadyException");
		}
		rw.commit();

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
		assertTrue("Furthermore, it should BE the object we stored", account.equals(pettyCash));
	}

	public final void testAccountLedgerUpdateCascade() {
		Books root = (Books) rw.getRoot();
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

	/**
	 * Not much to this one; its just a wrapper around DataClient.save()
	 */
	public final void testStoreObjectCommand() {
		Datestamp virgin = new Datestamp("18 Mar 96");

		try {
			StoreObjectCommand soc = new StoreObjectCommand(virgin);
			soc.execute(rw);
		} catch (Exception e) {
			fail("InitBooksCommand shouldn't have thrown anything, let alone " + e);
		}
		rw.commit();

		List l = rw.queryByExample(virgin);

		assertEquals(1, l.size());

		Datestamp slut = (Datestamp) l.get(0);
		assertSame(slut, virgin);

		last = true;
	}
}
