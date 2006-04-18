/*
 * EntityCommandTest.java
 * 
 * See LICENCE file for usage and redistribution terms
 * Copyright (c) 2006 Operational Dynamics
 */
package accounts.services;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;

import junit.framework.TestCase;
import accounts.client.ObjectiveAccounts;
import accounts.domain.AccountsPayable;
import accounts.domain.AccountsReceivable;
import accounts.domain.Client;
import accounts.domain.ClientLedger;
import accounts.domain.Currency;
import accounts.domain.Ledger;
import accounts.domain.Supplier;
import accounts.domain.SupplierLedger;
import accounts.persistence.IdentifierAlreadyExistsException;
import accounts.persistence.UnitOfWork;

public class EntityCommandTest extends TestCase
{
	public static final String	TESTS_DATABASE	= "tmp/unittests/EntityCommandTest.yap";
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

	public final void testAddEntityCommandWithClient() throws NotFoundException {
		Client client = new Client();
		ClientLedger cl = null;
		try {
			cl = new ClientLedger(client);
			fail("Client object's name not filled in, should have thrown IllegalArgumentException");
		} catch (IllegalArgumentException iae) {
			// good
		}

		client.setName("Bloggins, Inc");
		try {
			cl = new ClientLedger(client);
		} catch (IllegalArgumentException iae) {
			fail("Should NOT have thrown IllegalArgumentException");
		}
		assertNotNull(cl);

		/*
		 * needs an AccountsReceivable stored for the command to run properly.
		 */

		AccountsReceivable ar = new AccountsReceivable("Accounts Receivable");

		UnitOfWork uow = new UnitOfWork("testAddEntityCommandWithClient-1");
		try {
			Currency home = new Currency("APF", "Antarctic Penguin Fish Token", "%");
			InitBooksCommand ibc = new InitBooksCommand(home);
			ibc.execute(uow);
			AddAccountCommand aac = new AddAccountCommand(ar);
			aac.execute(uow);
		} catch (CommandNotReadyException cnre) {
			fail("Caught CommandNotReadyException: " + cnre.getMessage());
		}
		uow.commit();

		/*
		 * Now run the AddEntityCommand
		 */

		uow = new UnitOfWork("testAddEntityCommandWithClient-2");
		try {
			AddEntityCommand aec = new AddEntityCommand(client);
			aec.execute(uow);
		} catch (IdentifierAlreadyExistsException iaee) {
			fail(iaee.getMessage());
		} catch (CommandNotReadyException cnre) {
			fail(cnre.getMessage());
		}
		uow.commit();

		/*
		 * Now make sure it did what it should have!
		 */

		List found = ObjectiveAccounts.store.queryByExample(ClientLedger.class);
		assertEquals("Only should be one ClientLedger in database at this point", 1, found.size());

		SpecificLedgerFinder finder = new SpecificLedgerFinder();
		finder.setAccountTitle("Accounts Receiv");
		finder.setLedgerName("");
		Ledger candidate = finder.getLedger();

		assertTrue(candidate instanceof ClientLedger);

		/*
		 * If you try adding it again it should bail.
		 */

		try {
			AddEntityCommand aec = new AddEntityCommand(client);
			fail("should have thrown IdentifierAlreadyExistsException");
		} catch (IdentifierAlreadyExistsException iaee) {
			//
		}

		/*
		 * artificially create and store a ledger so that the new ledger already
		 * exists, but the client doesn't
		 */
		String DUPLICATE = "SomeOtherFirm Ltd";
		Client another = new Client(DUPLICATE);

		uow = new UnitOfWork("testAddEntityCommandWithClient-3");
		ClientLedger dup = new ClientLedger(another);
		ar.addLedger(dup);
		uow.registerDirty(ar);
		uow.commit();

		try {
			AddEntityCommand aec = new AddEntityCommand(another);
			fail("should have thrown IdentifierAlreadyExistsException to reflect an already extant ClientLedger with Client's name.");
		} catch (IdentifierAlreadyExistsException iaee) {
			//
		}

		uow = new UnitOfWork("testAddEntityCommandWithClient-4");
		client.setName(DUPLICATE + "Something Else");

		try {
			AddEntityCommand aec = new AddEntityCommand(client);
			aec.execute(uow);
		} catch (IdentifierAlreadyExistsException iaee) {
			fail("Shouldn't have thrown an IdentifierAlreadyExistsException at this point");
		} catch (CommandNotReadyException cnre) {
			fail("Shouldn't have thrown an CommandNotReadyException at this point either");
		}
		uow.commit();
	}

	/*
	 * Complete cut and paste of previous test, this time working Supplier and
	 * SupplierLedger. No InitBooksCommand this time, though.
	 */
	public final void testAddEntityCommandWithSupplier() throws NotFoundException {
		Supplier supplier = new Supplier();
		SupplierLedger sl = null;
		try {
			sl = new SupplierLedger(supplier);
			fail("Supplier object's name not filled in, should have thrown IllegalArgumentException");
		} catch (IllegalArgumentException iae) {
			// good
		}

		supplier.setName("Rabbit Food Pte Ltd");
		try {
			sl = new SupplierLedger(supplier);
		} catch (IllegalArgumentException iae) {
			fail("Should NOT have thrown IllegalArgumentException");
		}
		assertNotNull(sl);

		/*
		 * needs an AccountsPayable stored for the command to run properly.
		 */

		AccountsPayable ap = new AccountsPayable("Accounts Payable");

		UnitOfWork uow = new UnitOfWork("testAddEntityCommandWithSupplier-1");
		try {
			// already a Books object
			AddAccountCommand aac = new AddAccountCommand(ap);
			aac.execute(uow);
		} catch (CommandNotReadyException cnre) {
			fail("Caught CommandNotReadyException: " + cnre.getMessage());
		}
		uow.commit();

		/*
		 * Now run the AddEntityCommand
		 */

		uow = new UnitOfWork("testAddEntityCommandWithSupplier-2");
		try {
			AddEntityCommand aec = new AddEntityCommand(supplier);
			aec.execute(uow);
		} catch (IdentifierAlreadyExistsException iaee) {
			fail(iaee.getMessage());
		} catch (CommandNotReadyException cnre) {
			fail(cnre.getMessage());
		}
		uow.commit();

		/*
		 * Now make sure it did what it should have!
		 */

		List found = ObjectiveAccounts.store.queryByExample(SupplierLedger.class);
		assertEquals("Only should be one SupplierLedger in database at this point", 1, found.size());

		SpecificLedgerFinder finder = new SpecificLedgerFinder();
		finder.setAccountTitle("Accounts Payab");
		finder.setLedgerName("");
		Ledger candidate = finder.getLedger();

		assertTrue(candidate instanceof SupplierLedger);

		/*
		 * If you try adding it again it should bail.
		 */

		try {
			AddEntityCommand aec = new AddEntityCommand(supplier);
			fail("should have thrown IdentifierAlreadyExistsException");
		} catch (IdentifierAlreadyExistsException iaee) {
			//
		}

		/*
		 * artificially create and store a ledger so that the new ledger already
		 * exists, but the client doesn't
		 */
		String DUPLICATE = "A Duplicate Unlimited";
		Supplier yetAnother = new Supplier(DUPLICATE);

		uow = new UnitOfWork("testAddEntityCommandWithSupplier-3");
		SupplierLedger dup2 = new SupplierLedger(yetAnother);
		ap.addLedger(dup2);
		uow.registerDirty(ap);
		uow.commit();

		try {
			AddEntityCommand aec = new AddEntityCommand(yetAnother);
			fail("should have thrown IdentifierAlreadyExistsException to reflect an already extant ClientLedger with Client's name.");
		} catch (IdentifierAlreadyExistsException iaee) {
			//
		}

		uow = new UnitOfWork("testAddEntityCommandWithClient-4");
		supplier.setName(DUPLICATE + "Suffix Uniqueness");

		try {
			AddEntityCommand aec = new AddEntityCommand(supplier);
			aec.execute(uow);
		} catch (IdentifierAlreadyExistsException iaee) {
			fail("Shouldn't have thrown an IdentifierAlreadyExistsException at this point");
		} catch (CommandNotReadyException cnre) {
			fail("Shouldn't have thrown an CommandNotReadyException at this point either");
		}
		uow.commit();
	}
}
