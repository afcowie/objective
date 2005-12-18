/*
 * BasicLedgerTest.java
 * 
 * See LICENCE file for usage and redistribution terms
 * Copyright (c) 2005 Operational Dynamics
 */
package accounts.domain;

import java.util.Set;

import junit.framework.TestCase;

/**
 * @author Andrew Cowie
 */
public class BasicLedgerTest extends TestCase
{
	public final void testNoArgConstructor() {
		Ledger ledger = new Ledger();
		/*
		 * Here just to warn of a behaviour change...
		 */
		assertNull("You changed the default null behaviour of Ledger, didn't you?", ledger.getEntries());

		/*
		 * Calls calculateBalance(), so after this, the Ledger's internal Amount
		 * should be not null, though the Entry Set should be.
		 */
		assertNotNull(ledger.getBalance());
		assertEquals("0.00", ledger.getBalance().getValue());
		assertNull("You changed the default null behaviour of Ledger, didn't you?", ledger.getEntries());

	}

	public final void testAddEntry() {
		Ledger ledger = new Ledger();

		/*
		 * Safety check.
		 */
		try {
			ledger.addEntry(null);
			fail("Should have thrown an NullPointerException");
		} catch (NullPointerException npe) {
			//
		}

		/*
		 * Recall real Entries are either Debit or Credit, not both or neither.
		 * So arbitrarily pick one.
		 */
		Debit first = new Debit(new Amount("49.95"), null);

		/*
		 * Another safety check - no operations on raw ledgers!
		 */
		try {
			ledger.addEntry(first);
			fail("Should have thrown an UnsupportedOperationException");
		} catch (UnsupportedOperationException uoe) {
		}

		/*
		 * Ok, now replace it with a real ledger.
		 */
		ledger = new DebitPositiveLedger();

		/*
		 * In this case, addEntry() has been not been called before, so neither
		 * has calculateBalance. THIS should instantiate the internal Set AND
		 * call calculateBalance()
		 */
		ledger.addEntry(first);
		assertNotNull(ledger.getEntries());
		assertEquals("49.95", ledger.getBalance().getValue());

		Debit second = new Debit(new Amount("20.00"), null);
		ledger.addEntry(second);
		assertEquals("69.95", ledger.getBalance().getValue());

		Set entries = ledger.getEntries();

		assertEquals(2, entries.size());
	}

	public final void testDebitsAndCreditEntryRecognition() {
		Ledger ledger = new CreditPositiveLedger();
		Debit oneSide = new Debit(new Amount("15.00"), null);
		Credit otherSide = new Credit(new Amount("15.00"), null);

		ledger.addEntry(oneSide);
		assertEquals("-15.00", ledger.getBalance().getValue());

		ledger.addEntry(otherSide);
		assertEquals("0.00", ledger.getBalance().getValue());
	}
}