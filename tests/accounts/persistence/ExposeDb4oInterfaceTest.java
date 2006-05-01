/*
 * ExposeDb4oInterfaceTest.java
 * 
 * See LICENCE file for usage and redistribution terms
 * Copyright (c) 2005 Operational Dynamics
 */
package accounts.persistence;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.NoSuchElementException;

import junit.framework.TestCase;

/**
 * DataClient exposes ways to do queries on the underlying database. Test them
 * here.
 * 
 * @author Andrew Cowie
 */
public class ExposeDb4oInterfaceTest extends TestCase
{
	public static final String	TESTS_DATABASE	= "tmp/unittests/ExposeDb4oInterfaceTest.yap";
	private static boolean		initialized		= false;

	private void init() {
		try {
			new File(TESTS_DATABASE).delete();
			Engine.newDatafile(TESTS_DATABASE);

			DataClient rw = Engine.gainClient();

			for (int i = 0; i < 20; i++) {
				DummyInts d = new DummyInts(i);
				rw.save(d);
			}

			rw.commit();
			Engine.releaseClient(rw);
			Engine.shutdown();

			initialized = true;
		} catch (Exception e) {
			System.err.println("Unexpected problem in init()!");
			e.printStackTrace();
			System.err.flush();
		}
	}

	public void setUp() {
		if (!initialized) {
			init();
		}
		try {
			Engine.openDatafile(TESTS_DATABASE);
		} catch (FileNotFoundException fnfe) {
			fail("Where is the test database?");
		}
	}

	public void tearDown() {
		try {
			Engine.shutdown();
		} catch (Exception e) {
			System.err.println("What the hell? " + e);
			System.err.flush();
		}
	}

	public final void testDataClientQueryByExample() {
		DataClient store = Engine.gainClient();

		/*
		 * Query something that isn't in the set
		 */
		DummyInts proto1 = new DummyInts(25);
		List results = store.queryByExample(proto1);

		assertNotNull(results);
		assertEquals(0, results.size());

		/*
		 * Now query something that is
		 */
		DummyInts proto2 = new DummyInts(7);

		results = store.queryByExample(proto2);

		assertEquals(1, results.size());

		DummyInts r = (DummyInts) results.get(0);
		assertEquals(7, r.getNum());

		Engine.releaseClient(store);
	}

	/**
	 * Make sure that peek() as implemented in DataClient returns committed
	 * (persisted) Objects, not the current in flight ones. This just ensures
	 * the basic behaviour. It's actually needed for
	 * {@link UpdateTransactionCommand}, so
	 * 
	 * @see FIXME
	 */
	public final void testPeekPersistedReturnsCommittedObject() {
		DataClient store = Engine.gainClient();

		/*
		 * So fetch up #8.
		 */
		DummyInts proto = new DummyInts(8);
		List results = store.queryByExample(proto);

		assertNotNull(results);
		assertEquals(1, results.size());
		DummyInts eight = (DummyInts) results.get(0);
		assertEquals(8, eight.getNum());

		/*
		 * Now use DataClient's peek() wrapper of
		 * Db4o.ExtObjectContainer.peekPersisted() and see what we pull up:
		 */
		DummyInts peeked = (DummyInts) store.peek(eight);

		assertEquals(8, peeked.getNum());
		assertNotSame(eight, peeked);
		assertEquals(eight.getNum(), peeked.getNum());

		/*
		 * So they're not the same object. What happens if we querry it?
		 */
		results = store.queryByExample(peeked);
		assertEquals(1, results.size());
		DummyInts anotherEight = (DummyInts) results.get(0);
		assertEquals(8, anotherEight.getNum());

		/*
		 * Fair enough. Now the kicker: is it the same object?
		 */
		assertSame(eight, anotherEight);

		/*
		 * Yes they are! Terrific. Now. The reasl point of peek[committed] is to
		 * find out what is persisted. What happens if the local object is
		 * modified?
		 */

		eight.setNum(66);
		assertEquals(66, eight.getNum());

		DummyInts committed = (DummyInts) store.peek(eight);
		// certainly won't be the same object:
		assertNotSame(eight, committed);
		/*
		 * But the thing we get back should be the committed value, not the
		 * current object.
		 */
		assertEquals(8, committed.getNum());
		// good show.

		/*
		 * Finally make sure it behaves when asked to peek a not-persisted
		 * object
		 */

		DummyInts notThere = new DummyInts(5678);
		try {
			store.peek(notThere);
			fail("Calling peek() on an object that isn't persisted+committed should result in an Exception");
		} catch (NoSuchElementException nsee) {
			// good
		}

		Engine.releaseClient(store);
	}
}
