/*
 * BasicTransactionTest.java
 * 
 * See LICENCE file for usage and redistribution terms
 * Copyright (c) 2005 Operational Dynamics
 */
package accounts.domain;

import junit.framework.TestCase;

/**
 * @author Andrew Cowie
 */
public class BasicTransactionTest extends TestCase
{
	public final void testIsBalanced() {
		Transaction t = new Transaction();

		/*
		 * Safety check.
		 */
		try {
			t.addEntry((Entry) null);
			fail("Should have thrown an NullPointerException");
		} catch (NullPointerException npe) {
			//
		}

		/*
		 * Also verifies Credit and Debit super calls are being made.
		 */
		Debit one = new Debit(new Amount("10.00"), null);
		Credit two = new Credit(new Amount("10.00"), null);

		t.addEntry(one);
		t.addEntry(two);
		assertTrue(t.isBalanced());

		/*
		 * As we have switched to Sets throughout, trying to add the same object
		 * should result in a false return and no change to the entries Set.
		 */
		boolean result = t.addEntry(two);
		assertFalse("Trying to add the same Entry object again should have failed and returned false", result);
		assertTrue(t.isBalanced());

		/*
		 * Continue verfication, this time lopsided.
		 */
		Credit three = new Credit(new Amount("0.10"), null);
		Debit four = new Debit(new Amount("0.03"), null);
		Debit five = new Debit(new Amount("0.07"), null);

		t.addEntry(three);
		assertFalse(t.isBalanced());

		t.addEntry(four);
		assertFalse(t.isBalanced());

		t.addEntry(five);
		assertTrue(t.isBalanced());

	}

	public final void testDatestampSettingAfterEntriesAdded() {
		Transaction t1 = new Transaction();

		/*
		 * Deliberately construct Entry objects with blank Datestamps...
		 */
		Debit left = new Debit();
		left.setAmount(new Amount("15.00"));
		Credit right = new Credit();
		right.setAmount(new Amount("15.00"));

		assertNull(left.getDate());
		assertNull(right.getDate());

		/*
		 * Add them to the Transaction. This should not set their dates, because
		 * the Transaction hasn't a date yet.
		 */
		t1.addEntry(left);
		t1.addEntry(right);

		assertNull(left.getDate());
		assertNull(right.getDate());

		Datestamp date = new Datestamp();
		date.setAsToday();

		/*
		 * Set the Transaction's date. This should have the ripple effect of
		 * setting the Entry's dates.
		 */
		t1.setDate(date);

		assertNotNull(left.getDate());
		assertNotNull(right.getDate());
		assertTrue(left.getDate() == date);
		assertTrue(right.getDate() == date);
	}

	public final void testDatestampNormalizedOnEntriesAdded() {
		/*
		 * Now repeat the test, opposite cycle. These Entrys, using
		 * {Debit|Credit}'s Amount constructor, will have today's Datestamp.
		 */

		Debit gauche = new Debit(new Amount("9.95"), null);
		Credit droit = new Credit(new Amount("9.95"), null);
		assertNull(gauche.getDate());
		assertNull(droit.getDate());

		Datestamp today = new Datestamp();
		today.setAsToday();
		gauche.setDate(today);

		Datestamp theOtherDay = new Datestamp("14 Oct 03");
		droit.setDate(theOtherDay);

		assertEquals(today.getInternalTimestamp(), gauche.getDate().getInternalTimestamp());
		assertEquals(theOtherDay.toString(), droit.getDate().toString());

		/*
		 * Now, adding them to a transaction should normalize their dates.
		 */
		Datestamp aNewDay = new Datestamp("7 Dec 93");
		assertEquals("07 Dec 93", aNewDay.toString());

		Transaction t2 = new Transaction();
		t2.setDate(aNewDay);

		t2.addEntry(gauche);
		t2.addEntry(droit);

		/*
		 * so check the two Entries we added to have aNewDay as their Datestamp
		 * value.
		 */
		assertEquals(aNewDay.getInternalTimestamp(), gauche.getDate().getInternalTimestamp());
		assertEquals(aNewDay.toString(), droit.getDate().toString());
	}

}