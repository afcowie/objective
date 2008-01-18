/*
 * BasicDb4oTest.java
 * 
 * See LICENCE file for usage and redistribution terms
 * Copyright (c) 2005 Operational Dynamics
 */
package generic.persistence;

import java.io.File;

import com.db4o.Db4o;
import com.db4o.ObjectContainer;
import com.db4o.ObjectSet;
import com.db4o.config.Configuration;

import junit.framework.TestCase;

/**
 * Make sure the db4o jars are located successfully and working in basic ways.
 * 
 * @author Andrew Cowie
 */
public class RollbackDb4oTest extends TestCase
{
    private static String TMPDBFILE = "tmp/unittests/RollbackDb4oTest.yap";

    File _target = null;

    /*
     * ripped from BasicDb4oTest
     */
    public final void testDatabaseCreation() {
        _target = new File(TMPDBFILE);
        if (_target.exists()) {
            _target.delete();
        }
        Configuration config = Db4o.configure();
        config.messageLevel(0);

        ObjectContainer container = Db4o.openFile(TMPDBFILE);

        for (int i = 0; i < 10; i++) {
            container.set(new DummyInts(i));
        }
        container.close();

        File probe = new File(TMPDBFILE);
        assertTrue(probe.exists());
    }

    public final void testDeactivateReactivate() {
        ObjectContainer container = Db4o.openFile(TMPDBFILE);
        assertFalse(container.ext().isClosed());

        ObjectSet result = container.get(new DummyInts(7));
        assertNotNull(result);
        assertEquals(1, result.size());

        DummyInts seven = (DummyInts) result.next();
        assertEquals("7", seven.toString());

        synchronized (seven) {
            container.deactivate(seven, 2);
            assertEquals("0", seven.toString());

            container.activate(seven, 2);
            assertEquals("7", seven.toString());
        }
        assertEquals("7", seven.toString());
        container.close();
    }

    public final void testRollback() {
        ObjectContainer container = Db4o.openFile(TMPDBFILE);
        assertFalse(container.ext().isClosed());

        ObjectSet result = container.get(new DummyInts(7));
        assertNotNull(result);
        assertEquals(1, result.size());

        DummyInts seven = (DummyInts) result.next();
        assertEquals("7", seven.toString());

        seven.setNum(8);
        assertEquals("8", seven.toString());

        container.set(seven);
        assertEquals("8", seven.toString());

        synchronized (seven) {
            container.rollback();
            assertEquals("8", seven.toString());

            container.deactivate(seven, 2);
            assertEquals("0", seven.toString());

            container.activate(seven, 2);
            assertEquals("7", seven.toString());
        }
        assertEquals("7", seven.toString());
        container.close();
    }

}

/*
 * Uses class DummyInts from BasicDb4oTest
 */
