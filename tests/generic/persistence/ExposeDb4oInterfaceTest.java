/*
 * ExposeDb4oInterfaceTest.java
 * 
 * See LICENCE file for usage and redistribution terms
 * Copyright (c) 2005-2006 Operational Dynamics
 */
package generic.persistence;

import generic.persistence.DataClient;
import generic.persistence.Engine;

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
			Engine.newDatafile(TESTS_DATABASE, null);

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
			Engine.openDatafile(TESTS_DATABASE, null);
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
	 * {@link accounts.services.UpdateTransactionCommand}.
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

		/*
		 * Final little wrinkle: on release, DataStore refreshes all the objects
		 * that were explicity queried as recorded by DataClient.
		 */

		assertEquals(66, eight.getNum()); // because we dirtied it

		Engine.releaseClient(store);
		store = Engine.gainClient();

		results = store.getUnderlyingContainer().get(proto);
		assertEquals(1, results.size());
		eight = (DummyInts) results.get(0);
		assertEquals(8, eight.getNum());

		Engine.releaseClient(store);
	}

	public final void testDelete() {
		DataClient store = Engine.gainClient();

		DummyInts proto = new DummyInts(8);
		List results = store.queryByExample(proto);
		assertEquals(1, results.size());
		DummyInts eight = (DummyInts) results.get(0);

		store.delete(eight);

		assertEquals(8, eight.getNum());

		results = store.queryByExample(proto);
		assertEquals(0, results.size());

		Engine.releaseClient(store);
	}

	public final void testIdMethodsAgainstZero() {
		DataClient store = Engine.gainClient();

		try {
			store.getID(null);
			fail("How can you ask for the ID of null?");
		} catch (IllegalArgumentException iae) {
			// good
		}

		try {
			DummyInts notInThere = new DummyInts(2061);
			store.getID(notInThere);
			fail("Asking for the ID of an Object that isn't persisted is supposed to throw IllegalStateException");
		} catch (IllegalStateException ise) {
			// good
		}

		try {
			store.fetchByID(0);
			fail("Fetching ID 0 is supposed to throw IllegalArgumentException");
		} catch (IllegalArgumentException iae) {
			// good
		}

		try {
			final long random = 3427323442026l;
			Object o = store.fetchByID(random);
			fail("Fetching some random long "
				+ random
				+ "as ID actually returned an object: "
				+ o
				+ ". Oops. The whole point was to use a long that isn't an actual ID in use. Probably need a differnet long to test this.");
		} catch (IllegalStateException iae) {
			// good
		}

		Engine.releaseClient(store);
	}

	public final void testIdStabilityAcrossClients() {
		DataClient store1 = Engine.gainClient();
		DataClient store2 = Engine.gainClient();

		DummyInts proto = new DummyInts(12);
		List results = store1.queryByExample(proto);
		assertEquals(1, results.size());
		DummyInts twelve1 = (DummyInts) results.get(0);
		assertEquals(12, twelve1.getNum());

		long id1 = store1.getUnderlyingContainer().ext().getID(twelve1);

		results = store2.queryByExample(proto);
		assertEquals(1, results.size());
		DummyInts twelve2 = (DummyInts) results.get(0);
		assertEquals(12, twelve2.getNum());

		assertTrue(twelve1.getNum() == twelve2.getNum());

		long id2 = store2.getUnderlyingContainer().ext().getID(twelve2);

		assertEquals(id1, id2);
		assertNotSame(twelve1, twelve2);

		/*
		 * Now do the same test via DataClient's methods:
		 */

		id1 = store1.getID(twelve1);
		id2 = store2.getID(twelve2);
		assertEquals(id1, id2);

		twelve1 = (DummyInts) store1.fetchByID(id1);
		twelve2 = (DummyInts) store2.fetchByID(id2);

		assertTrue(twelve1.getNum() == twelve2.getNum());
		assertNotSame(twelve1, twelve2);

		Engine.releaseClient(store1);
		Engine.releaseClient(store2);
	}

	public final void testFetchByIdObjectAddedToDirtySet() {
		DataClient store = Engine.gainClient();

		/*
		 * So fetch up #17, but do it the hard way so we have an ID to fetch by.
		 */
		DummyInts proto = new DummyInts(17);
		List results = store.getUnderlyingContainer().get(proto);
		DummyInts seventeen = (DummyInts) results.get(0);
		assertEquals(17, seventeen.getNum());

		/*
		 * Fetch it by ID (that being what we need to test)
		 */

		long id = store.getID(seventeen);
		Object obj = store.fetchByID(id);

		assertNotNull(obj);
		assertTrue(obj instanceof DummyInts);

		seventeen = (DummyInts) obj;
		assertEquals(17, seventeen.getNum());

		/*
		 * Ok! Now dirty it.
		 */
		seventeen.setNum(69);
		assertEquals(69, seventeen.getNum());

		Engine.releaseClient(store);

		/*
		 * This should have caused the 69 to be refreshed back to 17
		 */
		store = Engine.gainClient();

		results = store.getUnderlyingContainer().get(proto);
		assertEquals(1, results.size());
		seventeen = (DummyInts) results.get(0);
		assertEquals(
			"releasing a DataClient containing a dirtied object originally fetched by ID should cause that object to be refreshed.",
			17, seventeen.getNum());

		Engine.releaseClient(store);
	}
}
