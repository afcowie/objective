/*
 * ObjectiveAccounts, accounting for small professional services firms.
 *
 * Copyright © 2005-2011 Operational Dynamics Consulting, Pty Ltd
 *
 * The code in this file, and the program it is a part of, is made available
 * to you by its authors as open source software: you can redistribute it
 * and/or modify it under the terms of the GNU General Public License version
 * 2 ("GPL") as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GPL for more details.
 *
 * You should have received a copy of the GPL along with this program. If not,
 * see http://www.gnu.org/licenses/. The authors of this program may be
 * contacted via http://research.operationaldynamics.com/projects/objective/.
 */
package accounts.domain;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import objective.domain.Credit;
import objective.domain.Datestamp;
import objective.domain.Debit;
import objective.domain.Entry;
import objective.domain.GenericTransaction;

import junit.framework.TestCase;

/**
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

        Datestamp d = new Datestamp();

        GenericTransaction t = new GenericTransaction(DESCRIPTION, d,
                (Entry[]) bunchOfEntries.toArray(new Entry[3]));
        assertNotNull(t);
        assertTrue("Should be a balanced transaction", t.isBalanced());

        String storedDescription = t.getDescription();
        assertEquals(DESCRIPTION, storedDescription);
    }

    public final void testTransactionArraySetter() {
        HashSet moreEntries = new HashSet();
        moreEntries.add(new Debit(new Amount("1.00"), null));
        moreEntries.add(new Credit(new Amount("1.00"), null));

        Datestamp d = new Datestamp();

        GenericTransaction t = new GenericTransaction("Another transaction", d,
                (Entry[]) moreEntries.toArray(new Entry[2]));
        assertTrue("Should be a balanced transaction", t.isBalanced());

        /*
         * Now call the setter which should cause a replacement of the
         * existing set
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
