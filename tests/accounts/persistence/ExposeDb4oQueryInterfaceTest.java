/*
 * ExposeDb4oQueryInterfaceTest.java
 * 
 * See LICENCE file for usage and redistribution terms
 * Copyright (c) 2005 Operational Dynamics
 */
package accounts.persistence;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;

import junit.framework.TestCase;

import accounts.client.ObjectiveAccounts;
import accounts.services.DatafileServices;

/**
 * DataStore needs to expose ways to do queries on the underlying database. Test
 * them here.
 * 
 * @author Andrew Cowie
 */
public class ExposeDb4oQueryInterfaceTest extends TestCase
{
	public static final String	TESTS_DATABASE	= "tmp/unittests/ExposeDb4oQueryInterfaceTest.yap";
	private static boolean		initialized		= false;

	private void init() {
		try {
			new File(TESTS_DATABASE).delete();
			ObjectiveAccounts.store = DatafileServices.newDatafile(TESTS_DATABASE);

			for (int i = 0; i < 20; i++) {
				DummyInts d = new DummyInts(i);
				ObjectiveAccounts.store.save(d);
			}

			ObjectiveAccounts.store.close();
			initialized = true;
		} catch (Exception e) {
			System.err.println("Unexpected problem in init()!");
			System.err.flush();
		}
	}

	public void setUp() {
		if (!initialized) {
			init();
		}
		try {
			ObjectiveAccounts.store = DatafileServices.openDatafile(TESTS_DATABASE);
		} catch (FileNotFoundException fnfe) {
			fail("Where is the test database?");
		}
	}

	public void tearDown() {
		try {
			if (!ObjectiveAccounts.store.getContainer().ext().isClosed()) {
				ObjectiveAccounts.store.close();
			} else
				throw new Exception("closed?!?");
		} catch (Exception e) {
			System.err.println("What the hell? " + e);
			System.err.flush();
		}
	}

	/**
	 * Test DataStore.query()
	 */
	public final void testDataStoreQueryByExample() {
		DataStore store = ObjectiveAccounts.store;

		/*
		 * Query something that isn't in the set
		 */
		DummyInts proto1 = new DummyInts(25);
		List results = store.query(proto1);

		assertEquals(0, results.size());

		/*
		 * Now query something that is
		 */
		DummyInts proto2 = new DummyInts(7);

		results = store.query(proto2);

		assertEquals(1, results.size());

		DummyInts r = (DummyInts) results.get(0);
		assertEquals(7, r.getNum());
	}
}
