/*
 * IdentifierCommandsTest.java
 * 
 * See LICENCE file for usage and redistribution terms
 * Copyright (c) 2006 Operational Dynamics
 */
package accounts.services;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Iterator;
import java.util.List;

import junit.framework.TestCase;
import accounts.client.ObjectiveAccounts;
import accounts.domain.Identifier;
import accounts.domain.IdentifierGroup;
import accounts.domain.PayrollTaxIdentifier;
import accounts.persistence.UnitOfWork;
import country.au.domain.AustralianPayrollTaxIdentifier;

public class IdentifierCommandsTest extends TestCase
{
	public static final String	TESTS_DATABASE	= "tmp/unittests/IdentifierCommandsTest.yap";
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

	public final void testStoreIdentifierCommandSimple() {
		Identifier i1 = new Identifier("One");
		Identifier i2 = new Identifier("Two");

		IdentifierGroup grp = new IdentifierGroup("Simple");
		grp.addIdentifier(i1);
		grp.addIdentifier(i2);

		UnitOfWork uow = new UnitOfWork("testStoreIdentifierCommandSimple-1");
		StoreIdentifierGroupCommand sigc = new StoreIdentifierGroupCommand(grp);
		try {
			sigc.execute(uow);
		} catch (Exception e) {
			fail("Shouldn't have thrown " + e);
		}

		uow.commit();

		/*
		 * verify they both got stored
		 */
		List l = ObjectiveAccounts.store.query(Identifier.class);
		assertEquals(2, l.size());

		/*
		 * Now make sure adding a third one actually works
		 */
		uow = new UnitOfWork("testStoreIdentifierCommandSimple-2");

		PayrollTaxIdentifier i3 = new PayrollTaxIdentifier("Three, PTI");
		grp.addIdentifier(i3);

		sigc = new StoreIdentifierGroupCommand(grp);
		try {
			sigc.execute(uow);
		} catch (Exception e) {
			fail("Shouldn't have thrown " + e);
		}
		uow.commit();

		l = ObjectiveAccounts.store.query(Identifier.class);
		assertEquals(3, l.size());
	}

	/**
	 * Same test, but this time we check to see that changing an identifier
	 * works
	 */
	public final void testStoreIdentifierCommandInChange() {
		/*
		 * Verify persistance across individual tests
		 */
		List l = ObjectiveAccounts.store.query(Identifier.class);
		assertEquals(3, l.size());

		/*
		 * Now fetch out one of the Identifiers, change it, and store it.
		 */
		Identifier proto = new Identifier("One");
		l = ObjectiveAccounts.store.query(proto);
		assertEquals(1, l.size());
		Identifier found = (Identifier) l.get(0);
		assertEquals("One", found.getName());

		found.setName("The One");

		l = ObjectiveAccounts.store.query(IdentifierGroup.class);
		assertEquals(1, l.size());
		IdentifierGroup grp = (IdentifierGroup) l.get(0);

		UnitOfWork uow = new UnitOfWork("testStoreIdentifierCommandInChange-1");
		StoreIdentifierGroupCommand sigc = new StoreIdentifierGroupCommand(grp);
		try {
			sigc.execute(uow);
		} catch (CommandNotReadyException cnre) {
			fail("Should not have thrown exception");
		}
		uow.commit();

		/*
		 * Should still be three objects; should not be a "One" and should be a
		 * "The One"
		 */
		l = ObjectiveAccounts.store.query(Identifier.class);
		assertEquals(3, l.size());

		boolean theone = false;
		boolean two = false;
		boolean three = false;

		Iterator iter = l.iterator();
		while (iter.hasNext()) {
			Identifier i = (Identifier) iter.next();
			if (i.getName().equals("One")) {
				fail("One shouldn't still be here");
			}
			if (i.getName().equals("The One")) {
				theone = true;
				continue;
			}
			if (i.getName().equals("Two")) {
				two = true;
				continue;
			}
			if (i.getName().equals("Three, PTI")) {
				three = true;
				continue;
			}
			fail("Unknown Identifier retrieved");
		}
		assertTrue(theone);
		assertTrue(two);
		assertTrue(three);

		/*
		 * Now add one more object, checking that the retrieved IdentifierGroup
		 * is [still] mutable
		 */
		AustralianPayrollTaxIdentifier i4 = new AustralianPayrollTaxIdentifier("Four, APTI");
		grp.addIdentifier(i4);
		try {
			uow = new UnitOfWork("testStoreIdentifierCommandInChange-2");
			sigc = new StoreIdentifierGroupCommand(grp);
			sigc.execute(uow);
		} catch (CommandNotReadyException cnre) {
			fail("Shouldn't have thrown " + cnre);
		}
		uow.commit();
	}

	/**
	 * When you remove an Identifier from the List in an IdentiferGroup, the
	 * Identifier still exists in the database. Deleting an Identifier is a
	 * different case.
	 */
	public final void testStoreIdentifierGroupCommandHavingRemovedOne() {
		List l = ObjectiveAccounts.store.query(IdentifierGroup.class);
		assertEquals(1, l.size());

		IdentifierGroup grp = (IdentifierGroup) l.get(0);

		List ids = grp.getIdentifiers();
		ids.remove(1);

		UnitOfWork uow = new UnitOfWork("testStoreIdentifierGroupCommandHavingRemovedOne");
		StoreIdentifierGroupCommand sigc = new StoreIdentifierGroupCommand(grp);
		try {
			sigc.execute(uow);
		} catch (CommandNotReadyException cnre) {
			fail("Should not have thrown " + cnre);
		}
		uow.commit();

		/*
		 * Now find out if its still in the stored IdentifierGroup (it shouldn't
		 * be)
		 */
		l = ObjectiveAccounts.store.query(IdentifierGroup.class);
		assertEquals(1, l.size());
		grp = (IdentifierGroup) l.get(0);

		List now = grp.getIdentifiers();
		assertEquals(3, now.size());

		boolean theone = false;
		boolean three = false;
		boolean four = false;

		Iterator iter = now.iterator();
		while (iter.hasNext()) {
			Identifier i = (Identifier) iter.next();
			if (i.getName().equals("The One")) {
				theone = true;
				continue;
			}
			if (i.getName().equals("Two ")) {
				fail("One shouldn't still be here");
			}
			if (i.getName().equals("Three, PTI")) {
				three = true;
				continue;
			}
			if (i.getName().equals("Four, APTI")) {
				four = true;
				continue;
			}
			fail("Unknown Identifier retrieved; removal didn't happen?");
		}
		assertTrue(theone);
		assertTrue(three);
		assertTrue(four);

		/*
		 * But it should still be in the database.
		 */
		Identifier proto = new Identifier("Two");
		l = ObjectiveAccounts.store.query(proto);
		assertEquals(1, l.size());
	}
}
