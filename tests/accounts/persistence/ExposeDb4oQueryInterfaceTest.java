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

/**
 * DataClient exposes ways to do queries on the underlying database. Test them
 * here.
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
			Engine.newDatafile(TESTS_DATABASE);

			DataClient ro = Engine.gainClient();

			for (int i = 0; i < 20; i++) {
				DummyInts d = new DummyInts(i);
				ro.save(d);
			}

			ro.commit();
			Engine.releaseClient(ro);
			Engine.shutdown();

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
}
