/*
 * UnitOfWorkTest.java
 *
 * See LICENCE file for usage and redistribution terms
 * Copyright (c) 2005-2006 Operational Dynamics
 */
package accounts.persistence;

import generic.util.Debug;

import java.util.ConcurrentModificationException;

import junit.framework.AssertionFailedError;

import com.db4o.ObjectContainer;
import com.db4o.ObjectSet;

/**
 * Excercise the UnitOfWork class.
 * 
 * @author Andrew Cowie
 * @deprecated
 */
public class UnitOfWorkTest extends TestCase
{
	public static final String	TESTS_DATABASE	= "tmp/unittests/UnitOfWorkTest.yap";
	private static boolean		initialized		= false;

	private void init() {
		Debug.init("memory");
		Debug.setProgname("UnitOfWorkTest");
		Debug.register("memory");
		new File(TESTS_DATABASE).delete();
		ObjectiveAccounts.store = DatafileServices.newDatafile(TESTS_DATABASE);
		ObjectiveAccounts.store.close();
		initialized = true;
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
		ObjectiveAccounts.store.close();
	}

	/**
	 * Bare bones test case to make sure the finalize() works with a beginning
	 * UnitOfWork object.
	 */
	public final void testSetupAndFinalize() {
		UnitOfWork uow = new UnitOfWork("testSetupAndFinalize");
		uow.cancel();

		uow.finalize();
		uow = null;
	}

	public final void testActiveChecks() {
		UnitOfWork uow = new UnitOfWork("testActiveChecks");

		uow.commit();

		try {
			uow.registerInterest(null);
			fail("Should have thought UnitOfWork invalid and thrown IllegalStateException");
		} catch (IllegalStateException iae) {
		}
		try {
			uow.registerDirty(null);
			fail("Should have thought UnitOfWork invalid and thrown IllegalStateException");
		} catch (IllegalStateException iae) {
		}
		try {
			uow.commit();
			fail("Should have thought UnitOfWork invalid and thrown IllegalStateException");
		} catch (IllegalStateException iae) {
		}
		try {
			uow.rollback();
			fail("Should have thought UnitOfWork invalid and thrown IllegalStateException");
		} catch (IllegalStateException iae) {
		}
		try {
			uow.cancel();
			fail("Should have thought UnitOfWork invalid and thrown IllegalStateException");
		} catch (IllegalStateException iae) {
		}

	}

	public final void testRegisterDirty() {
		Tofu first = new Tofu(-7);
		Tofu second = first;

		assertEquals(-7, second.getNum());

		UnitOfWork uow = new UnitOfWork("testRegisterDirty-1");
		uow.registerDirty(first);
		uow.commit();

		ObjectContainer container = ObjectiveAccounts.store.getContainer();
		ObjectSet result = container.get(Tofu.class);
		Tofu retreived = (Tofu) result.next();
		assertEquals(-7, retreived.getNum());
		retreived.setNum(3);
		assertEquals(3, retreived.getNum());
		/*
		 * And now we see that the retrieved object is a reference to the same
		 * 'first' object, just as 'second' was.
		 */
		assertSame(first, retreived);
		assertTrue(first == retreived);
		assertEquals(3, first.getNum());

		uow = new UnitOfWork("testRegisterDirty-2");
		uow.registerDirty(first);
		uow.commit();

		result = container.get(Tofu.class);
		retreived = (Tofu) result.next();
		assertEquals(3, retreived.getNum());

	}

	public final void testAvoidConcurrentModificationBug() {
		Tofu first = new Tofu(21);
		Tofu second = new Tofu(22);

		UnitOfWork uow = new UnitOfWork("testAvoidConcurrentModificationBug");
		uow.registerDirty(first);
		uow.registerDirty(second);
		try {
			uow.commit();
		} catch (ConcurrentModificationException cme) {
			fail("ConcurrentModificationException thrown from UnitOfWork - bug [still] present");
		}

		ObjectContainer container = ObjectiveAccounts.store.getContainer();
		ObjectSet result = container.get(Tofu.class);
		assertEquals(3, result.size());
	}

	int	counter;

	public final void testRegisterInterest() {
		counter = 0;
		UnitOfWork observer = new UnitOfWork("observer");

		ObjectContainer container = ObjectiveAccounts.store.getContainer();
		final Tofu proto = new Tofu(3);
		ObjectSet result = container.get(proto);
		assertEquals(1, result.size());
		final Tofu innocent = (Tofu) result.next();
		assertEquals(3, innocent.getNum());

		/*
		 * It's a set, so registering interest again should have no effact, but
		 * we'll verify that by the count at the end.
		 */
		observer.registerInterest(innocent);
		observer.registerInterest(innocent);

		observer.onChange(new UpdateListener() {
			public void changed(Object obj) {
				assertSame(innocent, obj);
				counter++;
			}
		});

		UnitOfWork worker = new UnitOfWork("worker");

		ObjectSet anotherResult = container.get(proto);
		Tofu sinful = (Tofu) anotherResult.next();
		assertEquals(3, sinful.getNum());

		worker.registerDirty(sinful);
		worker.commit();

		/*
		 * This should have caused innnocent's listener to get called...
		 */
		assertEquals(1, counter);
		observer.cancel();
	}

	public final void testGlobalListenersClearingBug() {
		UnitOfWork uow = new UnitOfWork("testGlobalListenersClearingBug");
		Tofu gretzky = new Tofu(99);
		uow.registerDirty(gretzky);
		try {
			uow.commit();
		} catch (AssertionFailedError afe) {
			fail("Listener from testRegisterInterest still present");
		}
	}
}

class Tofu
{
	private int	num;

	Tofu(int i) {
		setNum(i);
	}

	void setNum(int i) {
		num = i;
	}

	int getNum() {
		return num;
	}
}