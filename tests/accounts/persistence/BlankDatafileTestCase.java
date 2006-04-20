/*
 * BlankDatafileTestCase.java
 * 
 * See LICENCE file for usage and redistribution terms
 * Copyright (c) 2006 Operational Dynamics
 */
package accounts.persistence;

import java.io.File;

import junit.framework.TestCase;

/**
 * Automatically setup a database file then open it for each test and close it
 * afterwards. To use it, all you have to do is
 * 
 * <pre>
 *   public class MyUnitTest extends BlankDatafileTestCase
 *   {
 *   		static {
 *   			DATAFILE = &quot;tmp/unittests/MyUnitTest.yap&quot;;
 *  		}
 *  
 *  		...
 * </pre>
 * 
 * Then reference <code>rw</code> in your unit tests as an already opened read
 * write DataClient.
 * 
 * TODO should something Engine.shutdown()?
 * 
 * @author Andrew Cowie
 */
public class BlankDatafileTestCase extends TestCase
{
	protected static String		DATAFILE	= null;
	protected static boolean	initialized	= false;

	protected DataClient		rw			= null;

	public void setUp() {
		if (!initialized) {
			if (DATAFILE == null) {
				throw new Error("You must define DATAFILE in a static {...} block in a BlankDatafileTestCase sublcass");
			}

			new File(DATAFILE).delete();
			Engine.newDatafile(DATAFILE);

			initialized = true;
		}
		rw = Engine.gainClient();
	}

	public void tearDown() {
		Engine.releaseClient(rw);
	}
}
