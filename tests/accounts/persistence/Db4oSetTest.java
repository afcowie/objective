/*
 * Db4oSetTest.java
 * 
 * See LICENCE file for usage and redistribution terms
 * Copyright (c) 2005 Operational Dynamics
 */
package accounts.persistence;

import java.io.File;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import com.db4o.Db4o;
import com.db4o.ObjectContainer;
import com.db4o.ObjectSet;
import com.db4o.config.Configuration;

import junit.framework.TestCase;

/**
 * Unit tests of our custom Db4oSet implementation.
 * 
 * @author Andrew Cowie
 */
public class Db4oSetTest extends TestCase
{
	private static String	TMPDBFILE	= "tmp/unittests/Db4oSetTest.yap";
	File					_target		= null;

	static {
		Configuration config = Db4o.configure();
		config.exceptionsOnNotStorable(true);
	}

	public final void testDb4oSet() {
		_target = new File(TMPDBFILE);
		if (_target.exists()) {
			_target.delete();
		}
		Configuration config = Db4o.configure();
		config.messageLevel(0);

		ObjectContainer container = Db4o.openFile(TMPDBFILE);

		/*
		 * Test constructor
		 */
		Db4oSet set = new Db4oSet(container);
		assertNotNull(set);
		assertEquals(0, set.size());

		container.close();
	}

	/*
	 * Class under test for boolean add(Object)
	 */
	public final void testAddObject() {
		ObjectContainer container = Db4o.openFile(TMPDBFILE);

		Db4oSet blah = new Db4oSet(container);
		assertNotNull(blah);

		DummyInts one = new DummyInts(1);
		container.set(one);
		container.commit();

		assertTrue(blah.add(one));
		assertEquals(1, blah.size());

		/*
		 * 
		 */
		assertFalse("Why didn't you get false back when adding the same element?", blah.add(one));
		assertEquals(1, blah.size());

		/*
		 * Add a second object
		 */
		DummyInts two = new DummyInts(2);
		container.set(two);
		container.commit();

		assertTrue(blah.add(two));
		assertEquals(2, blah.size());

		/*
		 * Now, store the Db4oSet
		 */
		container.set(blah);
		container.commit();

		container.close();
	}

	public final void testRemoveObject() {
		ObjectContainer container = Db4o.openFile(TMPDBFILE);

		Db4oSet blah = new Db4oSet(container);

		DummyInts three = new DummyInts(3);
		DummyInts four = new DummyInts(4);

		blah.add(three);
		blah.add(four);

		blah.remove(three);
		assertEquals(1, blah.size());
		Iterator iter = blah.iterator();
		DummyInts check = (DummyInts) iter.next();
		assertTrue(check == four);

		blah.remove(four);
		assertEquals(0, blah.size());
		container.commit();
		/*
		 * Whoa, cool. three and four above are still, actually in the database,
		 * since cascade delete isn't on...
		 */
		ObjectSet results = container.get(DummyInts.class);
		assertEquals(4, results.size());

		container.delete(three);
		container.delete(four);

		ObjectSet results2 = container.get(DummyInts.class);
		assertEquals(2, results2.size());

		container.close();
	}

	/*
	 * Yes, I know tests are supposed to be independent. Whatever.
	 */
	public final void testVerifyStoredObjects() {
		ObjectContainer container = Db4o.openFile(TMPDBFILE);
		ObjectSet results = container.get(DummyInts.class);

		/*
		 * Whoa, cool. one and two above are still in there, since cascade
		 * delete wasn't on...
		 */
		assertEquals(2, results.size());

		boolean one = false;
		boolean two = false;
		while (results.hasNext()) {
			DummyInts i = (DummyInts) results.next();
			if (i.getNum() == 1) {
				one = true;
			}
			if (i.getNum() == 2) {
				two = true;
			}
		}
		assertTrue((one && two));

		container.close();
	}

	/*
	 * Now test a Db4oSet in a class
	 */
	public final void testSetInClass() {
		ObjectContainer container = Db4o.openFile(TMPDBFILE);

		DummyObject obj = new DummyObject();

		obj._set = new Db4oSet(container); // NOW FIXME (1)!!!
		obj._set = new LinkedHashSet();

		File f1 = new File("hello.java");
		File f2 = new File("hello.class");

		// NOW FIXME (2)!!!
		// container.set(f1);
		// container.set(f2);

		obj._set.add(f1);
		obj._set.add(f2);

		container.set(obj);
		container.commit();

		ObjectSet results = container.get(DummyObject.class);
		assertNotNull(results);
		assertFalse("Shouldn't be zero results", (results.size() == 0));
		assertEquals("Only one DummyObject should have been retreived", 1, results.size());

		DummyObject output = (DummyObject) results.next();
		assertEquals("The retrieved DummyObject should have had 2 elements in its Set", 2, output._set.size());

		container.close();
	}

	/*
	 * Test to make sure our Db4oSet actually persisted across database close
	 * and reopen.
	 */
	public final void testSetInClassAcrossClosure() {
		// reopen it
		ObjectContainer container = Db4o.openFile(TMPDBFILE);

		ObjectSet results = container.get(DummyObject.class);
		assertEquals("Only one DummyObject should have been retreived", 1, results.size());

		DummyObject output = (DummyObject) results.next();
		assertEquals("The retrieved DummyObject should have had 2 elements in its Set", 2, output._set.size());

		boolean javaPresent = false;
		boolean classPresent = false;
		Iterator iter = output._set.iterator();
		while (iter.hasNext()) {
			File f = (File) iter.next();
			if (f.getName().equals("hello.java")) {
				javaPresent = true;
			}
			if (f.getName().equals("hello.class")) {
				classPresent = true;
			}
		}
		assertTrue("Two Files, one called hello.java and one caled hello.class, should have been retrieved",
				(javaPresent && classPresent));

		container.close();
	}
}

class DummyObject
{
	Set	_set;

	DummyObject() {
		//
	}
}