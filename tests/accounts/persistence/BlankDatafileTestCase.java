/*
 * BlankDatafileTestCase.java
 * 
 * See LICENCE file for usage and redistribution terms
 * Copyright (c) 2006 Operational Dynamics
 */
package accounts.persistence;

import java.io.File;
import java.io.FileNotFoundException;

import junit.framework.TestCase;

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
 * 
 * @author Andrew Cowie
 */
public class BlankDatafileTestCase extends TestCase
{
	protected static String	DATAFILE	= null;

	private static Class	initialized	= null;
	protected DataClient	rw			= null;

	public void setUp() {
		if (initialized == this.getClass()) {
			try {
				Engine.openDatafile(DATAFILE);
			} catch (FileNotFoundException fnfe) {
				throw new Error(fnfe);
			}
		} else {
			if (DATAFILE == null) {
				throw new Error("You must define DATAFILE in a static {...} block in a BlankDatafileTestCase sublcass");
			}

			new File(DATAFILE).delete();
			Engine.newDatafile(DATAFILE);

			initialized = this.getClass();
		}
		rw = Engine.gainClient();
	}

	public void tearDown() {
		Engine.releaseClient(rw);
		Engine.shutdown();
	}
}
