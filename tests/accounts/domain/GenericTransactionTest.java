/*
 * GenericTransactionTest.java
 * 
 * See LICENCE file for usage and redistribution terms
 * Copyright (c) 2005 Operational Dynamics
 */
package accounts.domain;

import java.util.HashSet;

import junit.framework.TestCase;

/**
 * TODO One sentance class summary. TODO Class description here.
 * 
 * @author Andrew Cowie
 */
public class GenericTransactionTest extends TestCase
{

	/*
	 * Class under test for void GenericTransaction(String, Set)
	 */
	public final void testGenericTransactionStringSetConstuctor() {
		HashSet bunchOfEntries = new HashSet();

		Amount price = new Amount("1499.95");
		Amount gst = price.percent(10);
		Amount expense = (Amount) price.clone();
		expense.decrementBy(gst);
		String DESCRIPTION = "Buying a gas fireplace";

		bunchOfEntries.add(new Debit(expense));
		bunchOfEntries.add(new Debit(gst));
		bunchOfEntries.add(new Credit(price));

		GenericTransaction t = new GenericTransaction(DESCRIPTION, bunchOfEntries);
		assertNotNull(t);
		assertTrue("Should be a balanced transaction", t.isBalanced());

		String storedDescription = t.getDescription();
		assertEquals(DESCRIPTION, storedDescription);
	}
}