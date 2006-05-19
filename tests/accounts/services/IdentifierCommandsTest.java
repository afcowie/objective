/*
 * IdentifierCommandsTest.java
 * 
 * See LICENCE file for usage and redistribution terms
 * Copyright (c) 2006 Operational Dynamics
 */
package accounts.services;

import java.util.Iterator;
import java.util.List;

import accounts.domain.Identifier;
import accounts.domain.IdentifierGroup;
import accounts.domain.PayrollTaxIdentifier;
import accounts.persistence.BlankDatafileTestCase;
import country.au.domain.AustralianPayrollTaxIdentifier;

public class IdentifierCommandsTest extends BlankDatafileTestCase
{
	static {
		DATAFILE = "tmp/unittests/IdentifierCommandsTest.yap";

	}

	public final void testStoreIdentifierCommandSimple() {
		Identifier i1 = new Identifier("One");
		Identifier i2 = new Identifier("Two");

		IdentifierGroup grp = new IdentifierGroup("Simple");
		grp.addIdentifier(i1);
		grp.addIdentifier(i2);

		StoreIdentifierGroupCommand sigc = new StoreIdentifierGroupCommand(grp);
		try {
			sigc.execute(rw);
		} catch (Exception e) {
			fail("Shouldn't have thrown " + e);
		}

		rw.commit();

		/*
		 * verify they both got stored
		 */
		List l = rw.queryByExample(Identifier.class);
		assertEquals(2, l.size());

		/*
		 * Now make sure adding a third one actually works
		 */

		PayrollTaxIdentifier i3 = new PayrollTaxIdentifier("Three, PTI", 3);
		grp.addIdentifier(i3);

		sigc = new StoreIdentifierGroupCommand(grp);
		try {
			sigc.execute(rw);
		} catch (Exception e) {
			fail("Shouldn't have thrown " + e);
		}
		rw.commit();

		l = rw.queryByExample(Identifier.class);
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
		List l = rw.queryByExample(Identifier.class);
		assertEquals(3, l.size());

		/*
		 * Now fetch out one of the Identifiers, change it, and store it.
		 */
		Identifier proto = new Identifier("One");
		l = rw.queryByExample(proto);
		assertEquals(1, l.size());
		Identifier found = (Identifier) l.get(0);
		assertEquals("One", found.getName());

		found.setName("The One");

		l = rw.queryByExample(IdentifierGroup.class);
		assertEquals(1, l.size());
		IdentifierGroup grp = (IdentifierGroup) l.get(0);

		StoreIdentifierGroupCommand sigc = new StoreIdentifierGroupCommand(grp);
		try {
			sigc.execute(rw);
		} catch (CommandNotReadyException cnre) {
			fail("Should not have thrown exception");
		}
		rw.save(found);
		rw.commit();

		/*
		 * Should still be three objects; should not be a "One" and should be a
		 * "The One"
		 */
		l = rw.queryByExample(Identifier.class);
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
		AustralianPayrollTaxIdentifier i4 = new AustralianPayrollTaxIdentifier("Four, APTI", 4);
		grp.addIdentifier(i4);
		try {
			sigc = new StoreIdentifierGroupCommand(grp);
			sigc.execute(rw);
		} catch (CommandNotReadyException cnre) {
			fail("Shouldn't have thrown " + cnre);
		}
		rw.commit();
	}

	/**
	 * When you remove an Identifier from the List in an IdentiferGroup, the
	 * Identifier still exists in the database. Deleting an Identifier is a
	 * different case.
	 */
	public final void testStoreIdentifierGroupCommandHavingRemovedOne() {
		List l = rw.queryByExample(IdentifierGroup.class);
		assertEquals(1, l.size());

		IdentifierGroup grp = (IdentifierGroup) l.get(0);

		List ids = grp.getIdentifiers();
		ids.remove(1);

		StoreIdentifierGroupCommand sigc = new StoreIdentifierGroupCommand(grp);
		try {
			sigc.execute(rw);
		} catch (CommandNotReadyException cnre) {
			fail("Should not have thrown " + cnre);
		}
		rw.commit();

		/*
		 * Now find out if its still in the stored IdentifierGroup (it shouldn't
		 * be)
		 */
		l = rw.queryByExample(IdentifierGroup.class);
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
		l = rw.queryByExample(proto);
		assertEquals(1, l.size());
	}
}
