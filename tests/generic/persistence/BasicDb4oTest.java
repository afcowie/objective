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
public class BasicDb4oTest extends TestCase
{
	private static String	TMPDBFILE	= "tmp/unittests/BasicDb4oTest.yap";
	File					_target		= null;

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

		assertTrue(container.ext().isClosed());
	}

	public final void testFetchObject() {
		ObjectContainer container = Db4o.openFile(TMPDBFILE);
		assertFalse(container.ext().isClosed());

		ObjectSet result = container.get(new DummyInts(7));
		assertNotNull(result);
		assertEquals(1, result.size());

		DummyInts seven = (DummyInts) result.next();
		assertEquals("7", seven.toString());

		container.close();
	}

	public final void testFetchAllObjects() {
		ObjectContainer container = Db4o.openFile(TMPDBFILE);

		ObjectSet result = container.get(null);

		int i = 0;
		while (result.hasNext()) {
			DummyInts obj = (DummyInts) result.next();
			assertNotNull(obj);
			i++;
			// System.out.println(obj.toString());
		}
		assertEquals(10, i);

		container.close();
	}
}