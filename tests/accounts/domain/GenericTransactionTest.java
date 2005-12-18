/*
 * GenericTransactionTest.java
 * 
 * See LICENCE file for usage and redistribution terms
 * Copyright (c) 2005 Operational Dynamics
 */
package accounts.domain;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

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

		bunchOfEntries.add(new Debit(expense, null));
		bunchOfEntries.add(new Debit(gst, null));
		bunchOfEntries.add(new Credit(price, null));

		GenericTransaction t = new GenericTransaction(DESCRIPTION, bunchOfEntries);
		assertNotNull(t);
		assertTrue("Should be a balanced transaction", t.isBalanced());

		String storedDescription = t.getDescription();
		assertEquals(DESCRIPTION, storedDescription);
	}

	public final void testTransactionArraySetter() {
		HashSet moreEntries = new HashSet();
		moreEntries.add(new Debit(new Amount("1.00"), null));
		moreEntries.add(new Credit(new Amount("1.00"), null));

		GenericTransaction t = new GenericTransaction("Another transaction", moreEntries);
		assertTrue("Should be a balanced transaction", t.isBalanced());

		/*
		 * Now call the setter which should cause a replacement of the existing
		 * set
		 */
		t.setEntries(new Entry[] {
				new Debit(new Amount("10.00"), null),
				new Credit(new Amount("4.25"), null),
				new Credit(new Amount("5.75"), null),
		});
		assertTrue("Should still be a balanced transaction", t.isBalanced());
		Set e = t.getEntries();
		assertEquals("Should have been three entries now!", 3, e.size());
		Iterator iter = e.iterator();
		Entry first = (Entry) iter.next();
		assertEquals("10.00", first.getAmount().getValue());
	}
}