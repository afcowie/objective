/*
 * BlankDatafileTestCase.java
 * 
 * See LICENCE file for usage and redistribution terms
 * Copyright (c) 2006 Operational Dynamics
 */
package accounts.persistence;

import generic.persistence.DataClient;
import generic.persistence.Engine;

import java.io.File;

import junit.framework.TestCase;
import accounts.domain.Books;

/**
 * Automatically setup a database file then open it for each test and close it
 * afterwards. To use it, all you have to do is
 * 
 * <code>
 *  public class MyUnitTest extends BlankDatafileTestCase
 *  {
 *  	static {
 *  		DATAFILE = &quot;tmp/unittests/MyUnitTest.yap&quot;;
 *  	}
 *   ...
 * </code>
 * 
 * Then reference <code>rw</code> in your unit tests as an already opened read
 * write DataClient.
 * <p>
 * You must set <code>last</code> to true in the your last text fixture so
 * that {@link #tearDown()} knows to call {@link Engine#shutdown()}.
 * 
 * @author Andrew Cowie
 */
public class BlankDatafileTestCase extends TestCase
{
	protected static String	DATAFILE	= null;

	private static Class	initialized	= null;
	protected DataClient	rw			= null;
	protected boolean		last;

	public void setUp() {
		if (initialized != this.getClass()) {
			if (DATAFILE == null) {
				throw new Error(
					"You must define DATAFILE in a static {...} block in a BlankDatafileTestCase sublcass");
			}
			/*
			 * In case it got missed:
			 */
			try {
				Engine.shutdown();
				System.err.println("\nlast was not set to true in " + initialized.getName()
					+ " and so Engine was still running.\n");
			} catch (IllegalStateException e) {
				// good
			}

			/*
			 * And open a new database:
			 */
			new File(DATAFILE).delete();
			Engine.newDatafile(DATAFILE, Books.class);

			initialized = this.getClass();
			last = false;
		}

		try {
			rw = Engine.gainClient();
		} catch (IllegalStateException ise) {
			throw new IllegalStateException(
				"You forgot to move the `last = true` setting to the final test fixture in "
					+ this.getClass().getName());
		}
	}

	public void tearDown() {
		Engine.releaseClient(rw);

		/*
		 * Since db4o is multithreaded, give, it a chance to catch up. This
		 * shouldn't ever be an issue but unit tests are pretty fast and CPU
		 * intensive, so deliberately take a brief pause.
		 */
		try {
			Thread.sleep(20);
		} catch (InterruptedException e) {
			// ignore
		}

		if (last) {
			Engine.shutdown();
		}
	}
}
