/*
 * DomainObjectTest.java
 * 
 * See LICENCE file for usage and redistribution terms
 * Copyright (c) 2006 Operational Dynamics
 */
package generic.domain;

import generic.persistence.DataClient;
import generic.persistence.Engine;
import generic.persistence.Selector;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;

import junit.framework.TestCase;

/**
 * Test the stability of DomainObject ids across DataClients and Native Queries
 * 
 * @author Andrew Cowie
 */
public class DomainObjectTest extends TestCase
{
	private static final String	DATAFILE	= "tmp/unittests/DomainObjectTest.yap";
	private static boolean		initialized	= false;
	protected DataClient		store		= null;

	public void setUp() {
		if (initialized == true) {
			try {
				Engine.openDatafile(DATAFILE, null);
			} catch (FileNotFoundException fnfe) {
				throw new Error(fnfe);
			}
		} else {
			new File(DATAFILE).delete();
			Engine.newDatafile(DATAFILE, null);

			initialized = true;
		}
		store = Engine.gainClient();
	}

	public void tearDown() {
		Engine.releaseClient(store);
		Engine.shutdown();
	}

	public final void testGetID() {
		final int num = 25;
		Factoid f = null;
		for (int i = 0; i < num; i++) {
			f = new Factoid(i);
			store.save(f);
		}
		store.commit();

		/*
		 * Spot check basic setup
		 */

		List results;

		results = store.queryByExample(Factoid.class);
		assertEquals(num, results.size());

		Factoid proto = new Factoid(4);
		results = store.queryByExample(proto);
		assertEquals(1, results.size());

		Factoid ship = (Factoid) results.get(0);
		assertNotNull(ship);
		assertEquals(4, ship.getIndex());
		assertNotNull(ship.getSubbie());
		assertEquals("4", ship.getSubbie().getName());

		results = null;
		/*
		 * On to business.
		 */

		long shipId = ship.getID();
		assertTrue(shipId != 0);

		Factoid theSameShip = (Factoid) store.fetchByID(shipId);
		assertSame(ship, theSameShip);

		/*
		 * Now get another client connection (ie, another transaction) and see
		 * if a query there results in a congruent object.
		 */
		DataClient anotherStore = Engine.gainClient();
		List anotherResults;
		anotherResults = anotherStore.queryByExample(proto);

		assertEquals(1, anotherResults.size());

		Factoid boat = (Factoid) anotherResults.get(0);
		assertNotNull(boat);
		assertEquals(4, boat.getIndex());
		assertNotNull(boat.getSubbie());
		assertEquals("4", boat.getSubbie().getName());

		long boatId = boat.getID();
		assertTrue(boatId != 0);
		/*
		 * And now the big question: do they represent the same object?
		 */
		assertTrue(shipId == boatId);

		/*
		 * Make sure our equals override works:
		 */
		assertTrue(ship.congruent(ship));
		assertTrue(ship.congruent(boat));
		assertTrue(boat.congruent(ship));
		assertFalse(ship.congruent(null));

		/*
		 * One more check to be sure it isn't faking us out
		 */
		proto = new Factoid(14);
		anotherResults = anotherStore.queryByExample(proto);

		assertEquals(1, anotherResults.size());

		Factoid rock = (Factoid) anotherResults.get(0);
		assertNotNull(rock);
		assertEquals(14, rock.getIndex());
		assertNotNull(rock.getSubbie());
		assertEquals("14", rock.getSubbie().getName());

		assertTrue(shipId != rock.getID());
		assertFalse(ship.congruent(rock));
		assertFalse(rock.congruent(ship));

		anotherStore.rollback();
		store.rollback();
		Engine.releaseClient(anotherStore);
	}

	/**
	 * Evaluate the equals() override, in particular in a Native Query
	 */
	public final void testEqualsObject() {
		if (true) {
			return;
		}
		Factoid proto = new Factoid(14);
		List result = store.queryByExample(proto);

		assertEquals(1, result.size());
		Factoid rock = (Factoid) result.get(0);
		assertNotNull(rock);
		assertEquals(14, rock.getIndex());
		assertNotNull(rock.getSubbie());
		assertEquals("14", rock.getSubbie().getName());

		final SubThing wanted = rock.getSubbie();

		class FactoidSelector extends Selector<Factoid>
		{
			private SubThing	target;
			private long		targetId;

			FactoidSelector(SubThing sub) {
				this.target = sub;
				this.targetId = sub.getID();
			}

			public boolean match(Factoid fact) {
				if (fact.getSubbie().congruent(target)) {
					return true;
				} else {
					return false;
				}
				// if (fact.getSubbie().getID() == targetId) {
				// return true;
				// } else {
				// return false;
				// }
			}
		}
		result = store.nativeQuery(new FactoidSelector(wanted));
		assertEquals(1, result.size());

		result = store.nativeQuery(new Selector<Factoid>() {
			public boolean match(Factoid fact) {
				if (fact.getSubbie().congruent(wanted)) {
					return true;
				} else {
					return false;
				}
			}
		});
		assertEquals(1, result.size());
	}
}

class Factoid extends DomainObject
{
	SubThing	subbie;
	int			index;

	Factoid(int i) {
		setIndex(i);
		this.subbie = new SubThing(i);
	}

	void setIndex(int i) {
		this.index = i;
	}

	int getIndex() {
		return index;
	}

	void setSubbie(SubThing sub) {
		this.subbie = sub;
	}

	SubThing getSubbie() {
		return subbie;
	}

}

class SubThing extends DomainObject
{
	String	name;

	SubThing(int i) {
		this.name = Integer.toString(i);
	}

	void setName(String name) {
		this.name = name;
	}

	String getName() {
		return name;
	}
}