/*
 * EntityCommandTest.java
 * 
 * See LICENCE file for usage and redistribution terms
 * Copyright (c) 2006 Operational Dynamics
 */
package accounts.services;

import java.util.List;

import accounts.domain.AccountsPayable;
import accounts.domain.AccountsReceivable;
import accounts.domain.Client;
import accounts.domain.ClientLedger;
import accounts.domain.Currency;
import accounts.domain.Ledger;
import accounts.domain.Supplier;
import accounts.domain.SupplierLedger;
import accounts.persistence.BlankDatafileTestCase;
import accounts.persistence.IdentifierAlreadyExistsException;

public class EntityCommandTest extends BlankDatafileTestCase
{
	static {
		DATAFILE = "tmp/unittests/EntityCommandTest.yap";
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

		try {
			Currency home = new Currency("APF", "Antarctic Penguin Fish Token", "%");
			InitBooksCommand ibc = new InitBooksCommand(home);
			ibc.execute(rw);
			AddAccountCommand aac = new AddAccountCommand(ar);
			aac.execute(rw);
		} catch (CommandNotReadyException cnre) {
			fail("Caught CommandNotReadyException: " + cnre.getMessage());
		}
		rw.commit();

		/*
		 * Now run the AddEntityCommand
		 */

		try {
			AddEntityCommand aec = new AddEntityCommand(client);
			aec.execute(rw);
		} catch (IdentifierAlreadyExistsException iaee) {
			fail(iaee.getMessage());
		} catch (CommandNotReadyException cnre) {
			fail(cnre.getMessage());
		}
		rw.commit();

		/*
		 * Now make sure it did what it should have!
		 */

		List found = rw.queryByExample(ClientLedger.class);
		assertEquals("Only should be one ClientLedger in database at this point", 1, found.size());

		SpecificLedgerFinder finder = new SpecificLedgerFinder();
		finder.setAccountTitle("Accounts Receiv");
		finder.setLedgerName("");

		Ledger candidate = null;
		try {
			finder.query(rw);
			candidate = finder.getLedger();
		} catch (NotFoundException nfe) {
			fail(nfe.getMessage());
		}

		assertTrue(candidate instanceof ClientLedger);

		/*
		 * If you try adding it again it should bail.
		 */

		try {
			AddEntityCommand aec = new AddEntityCommand(client);
			aec.execute(rw);
			fail("should have thrown IdentifierAlreadyExistsException");
		} catch (CommandNotReadyException cnre) {
			fail("Unepected CommandNotReadyException " + cnre.getMessage());
		} catch (IdentifierAlreadyExistsException iaee) {
			// good
		}

		/*
		 * artificially create and store a ledger so that the new ledger already
		 * exists, but the client doesn't
		 */
		String DUPLICATE = "SomeOtherFirm Ltd";
		Client another = new Client(DUPLICATE);

		ClientLedger dup = new ClientLedger(another);
		ar.addLedger(dup);
		rw.save(ar);
		rw.commit();

		try {
			AddEntityCommand aec = new AddEntityCommand(another);
			aec.execute(rw);
			fail("should have thrown IdentifierAlreadyExistsException to reflect an already extant ClientLedger with Client's name.");
		} catch (CommandNotReadyException cnre) {
			fail("Unepected CommandNotReadyException " + cnre.getMessage());
		} catch (IdentifierAlreadyExistsException iaee) {
			// good
		}

		client.setName(DUPLICATE + "Something Else");

		try {
			AddEntityCommand aec = new AddEntityCommand(client);
			aec.execute(rw);
		} catch (IdentifierAlreadyExistsException iaee) {
			fail("Shouldn't have thrown an IdentifierAlreadyExistsException at this point");
		} catch (CommandNotReadyException cnre) {
			fail("Shouldn't have thrown an CommandNotReadyException at this point either");
		}
		rw.commit();
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

		try {
			// already a Books object
			AddAccountCommand aac = new AddAccountCommand(ap);
			aac.execute(rw);
		} catch (CommandNotReadyException cnre) {
			fail("Caught CommandNotReadyException: " + cnre.getMessage());
		}
		rw.commit();

		/*
		 * Now run the AddEntityCommand
		 */

		try {
			AddEntityCommand aec = new AddEntityCommand(supplier);
			aec.execute(rw);
		} catch (IdentifierAlreadyExistsException iaee) {
			fail(iaee.getMessage());
		} catch (CommandNotReadyException cnre) {
			fail(cnre.getMessage());
		}
		rw.commit();

		/*
		 * Now make sure it did what it should have!
		 */

		List found = rw.queryByExample(SupplierLedger.class);
		assertEquals("Only should be one SupplierLedger in database at this point", 1, found.size());

		SpecificLedgerFinder finder = new SpecificLedgerFinder();
		finder.setAccountTitle("Accounts Payab");
		finder.setLedgerName("");
		Ledger candidate = null;
		try {
			finder.query(rw);
			candidate = finder.getLedger();
		} catch (NotFoundException nfe) {
			fail(nfe.getMessage());
		}

		assertTrue(candidate instanceof SupplierLedger);

		/*
		 * If you try adding it again it should bail.
		 */

		try {
			AddEntityCommand aec = new AddEntityCommand(supplier);
			aec.execute(rw);
			fail("should have thrown IdentifierAlreadyExistsException");
		} catch (CommandNotReadyException cnre) {
			fail("Unepected CommandNotReadyException "+cnre.getMessage());
		} catch (IdentifierAlreadyExistsException iaee) {
			// good
		}

		/*
		 * artificially create and store a ledger so that the new ledger already
		 * exists, but the client doesn't
		 */
		String DUPLICATE = "A Duplicate Unlimited";
		Supplier yetAnother = new Supplier(DUPLICATE);

		SupplierLedger dup2 = new SupplierLedger(yetAnother);
		ap.addLedger(dup2);
		rw.save(ap);
		rw.commit();

		try {
			AddEntityCommand aec = new AddEntityCommand(yetAnother);
			aec.execute(rw);
			fail("should have thrown IdentifierAlreadyExistsException to reflect an already extant ClientLedger with Client's name.");
		} catch (CommandNotReadyException cnre) {
			fail("Unepected CommandNotReadyException "+cnre.getMessage());
		} catch (IdentifierAlreadyExistsException iaee) {
			// good
		}

		supplier.setName(DUPLICATE + "Suffix Uniqueness");

		try {
			AddEntityCommand aec = new AddEntityCommand(supplier);
			aec.execute(rw);
		} catch (IdentifierAlreadyExistsException iaee) {
			fail("Shouldn't have thrown an IdentifierAlreadyExistsException at this point");
		} catch (CommandNotReadyException cnre) {
			fail("Shouldn't have thrown an CommandNotReadyException at this point either");
		}
		rw.commit();
	}
}
