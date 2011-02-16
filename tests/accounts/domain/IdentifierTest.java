/*
 * ObjectiveAccounts, accounting for small professional services firms.
 *
 * Copyright © 2006-2011 Operational Dynamics Consulting, Pty Ltd
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

import java.util.List;

import accounts.persistence.IdentifierAlreadyExistsException;
import junit.framework.TestCase;

/**
 * Tests Identifier (no big deal) and IdentifierGroup.
 * 
 * @author Andrew Cowie
 */
public class IdentifierTest extends TestCase
{

    public final void testBasicIdentifier() {
        Identifier i = null;

        try {
            i = new Identifier("OK");
        } catch (Exception e) {
            fail("This constructor should have worked");
        }

        try {
            i = new Identifier("");
            fail("Shouldn't be able to instantiate with a empty string");
        } catch (IllegalArgumentException iae) {
        }

        try {
            i = new Identifier(null);
            fail("Shouldn't be able to instantiate with a null string");
        } catch (IllegalArgumentException iae) {
        }

        i.setName("Test");

        assertEquals("Test", i.getName());

        Identifier j = new Identifier("ZZZZ");

        assertEquals(0, i.compareTo(i));
        assertTrue(i.compareTo(j) < 0);
        assertTrue(j.compareTo(i) > 0);
    }

    public final void testIdentifierGroup() {
        IdentifierGroup ig = null;

        try {
            ig = new IdentifierGroup(null);
            fail("Should have thrown");
        } catch (IllegalArgumentException iae) {
        }

        try {
            ig = new IdentifierGroup("");
            fail("Should have thrown");
        } catch (IllegalArgumentException iae) {
        }
        ig = new IdentifierGroup("Test group");
        assertEquals("Test group", ig.getLabel());

        /*
         * Create a series of Identifiers. The naughty one has the same
         * description as i0.
         */
        Identifier i0 = new Identifier("First");
        Identifier i0naughty = new Identifier("First");

        Identifier i1 = new Identifier("Second");
        Identifier i2 = new Identifier("Third");

        try {
            ig.addIdentifier(null);
            fail("Shouldn't accept null");
        } catch (IllegalArgumentException iae) {
        }

        try {
            ig.addIdentifier(i0);
            ig.addIdentifier(i0);
            fail("Should have thrown on trying to add the same identifier again");
        } catch (IdentifierAlreadyExistsException iaee) {
        }

        assertNotSame(i0, i0naughty);

        try {
            ig.addIdentifier(i0naughty);
            fail("Should have thrown on trying to add an identifier which matches the one input.");
        } catch (IdentifierAlreadyExistsException iaee) {
        }
        assertEquals(1, ig.getIdentifiers().size());

        ig.addIdentifier(i1);
        ig.addIdentifier(i2);

        List ids = ig.getIdentifiers();

        assertEquals(0, ids.indexOf(i0));
        assertEquals(1, ids.indexOf(i1));
        assertEquals(2, ids.indexOf(i2));

        try {
            ig.swap(i0, i0naughty);
            fail("Should have thrown IllegalArgumentException since second arg not in group");
        } catch (IllegalArgumentException iae) {
        }

        ig.swap(i0, i2);

        assertEquals(2, ids.indexOf(i0));
        assertEquals(1, ids.indexOf(i1));
        assertEquals(0, ids.indexOf(i2));

        ig.swap(i1, i2);

        assertEquals(2, ids.indexOf(i0));
        assertEquals(0, ids.indexOf(i1));
        assertEquals(1, ids.indexOf(i2));

        ig.removeIdentifier(i2);

        assertEquals(2, ids.size());
        assertEquals(-1, ids.indexOf(i2));
        assertEquals(0, ids.indexOf(i1));
        assertEquals(1, ids.indexOf(i0));
    }
}
