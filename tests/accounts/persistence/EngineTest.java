/*
 * EngineTest.java
 * 
 * See LICENCE file for usage and redistribution terms
 * Copyright (c) 2006 Operational Dynamics
 */
package accounts.persistence;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import junit.framework.TestCase;

import com.db4o.ObjectContainer;

/**
 * Test getting opening a DataServer from Engine, then getting DataClients from
 * the connection pool mechanism inside DataServer.
 * 
 * @author Andrew Cowie
 */
public class EngineTest extends TestCase
{
	private static final String	TESTFILE	= "tmp/unittests/EngineTest.yap";

	public final void testNewDatafile() {
		new File(TESTFILE).delete();

		Engine.newDatafile(TESTFILE);
		assertNotNull(Engine.server);
		Engine.shutdown();
		assertNull(Engine.server);
	}

	public final void testOpenDatafile() throws FileNotFoundException {
		try {
			Engine.openDatafile("/no/such/file.yap");
			fail("Should have thrown FileNotFoundException");
		} catch (FileNotFoundException fnfe) {
		}

		Engine.openDatafile(TESTFILE); // throws, but shouldn't!
		assertNotNull(Engine.server);
		assertTrue("New datafile doesn't exist!", new File(TESTFILE).exists());

		try {
			Engine.openDatafile(TESTFILE);
			fail("Should have failed because datafile already open, locked and so should have raised exception");
		} catch (IllegalStateException ise) {
		}

		ObjectContainer container = Engine.server.getUnderlyingServer().openClient();
		assertFalse(container.ext().isClosed());

		container.close();
		Engine.shutdown();
		assertNull(Engine.server);
	}

	/*
	 * Directly evaluate DataServer's connection pool mechanism
	 */
	public final void testConnectionPool() throws FileNotFoundException {
		DataServer server = server = new DataServer(TESTFILE);
		assertNotNull(server);

		DataClient c1 = server.gainClient();

		assertEquals(1, server.sizeInUse());
		assertEquals(0, server.sizeAvailable());

		DataClient c2 = server.gainClient();

		assertEquals(2, server.sizeInUse());
		assertEquals(0, server.sizeAvailable());

		server.releaseClient(c1);

		assertEquals(1, server.sizeInUse());
		assertEquals(1, server.sizeAvailable());

		DataClient c3 = server.gainClient();

		assertEquals(2, server.sizeInUse());
		assertEquals(0, server.sizeAvailable());

		DataClient c4 = server.gainClient();

		assertEquals(3, server.sizeInUse());
		assertEquals(0, server.sizeAvailable());

		server.releaseClient(c3);

		assertEquals(2, server.sizeInUse());
		assertEquals(1, server.sizeAvailable());

		server.releaseClient(c4);
		server.releaseClient(c2);

		assertEquals(0, server.sizeInUse());
		assertEquals(3, server.sizeAvailable());

		server.close();

		try {
			server.sizeInUse();
			fail("Connection pool Collections should be null after close()");
		} catch (NullPointerException npe) {
		}
		try {
			server.sizeAvailable();
			fail("Connection pool Collections should be null after close()");
		} catch (NullPointerException npe) {
		}
	}

	/*
	 * This duplicates the last test, but this time does it against Engine's
	 * static methods.
	 */
	public final void testEngineWrappingDataServer() throws FileNotFoundException {
		Engine.openDatafile(TESTFILE);

		DataClient c1 = Engine.gainClient();

		assertEquals(1, Engine.server.sizeInUse());
		assertEquals(0, Engine.server.sizeAvailable());

		DataClient c2 = Engine.gainClient();

		assertEquals(2, Engine.server.sizeInUse());
		assertEquals(0, Engine.server.sizeAvailable());

		Engine.releaseClient(c1);

		assertEquals(1, Engine.server.sizeInUse());
		assertEquals(1, Engine.server.sizeAvailable());

		DataClient c3 = Engine.gainClient();

		assertEquals(2, Engine.server.sizeInUse());
		assertEquals(0, Engine.server.sizeAvailable());

		DataClient c4 = Engine.gainClient();

		assertEquals(3, Engine.server.sizeInUse());
		assertEquals(0, Engine.server.sizeAvailable());

		Engine.releaseClient(c3);

		assertEquals(2, Engine.server.sizeInUse());
		assertEquals(1, Engine.server.sizeAvailable());

		Engine.releaseClient(c4);
		Engine.releaseClient(c2);

		assertEquals(0, Engine.server.sizeInUse());
		assertEquals(3, Engine.server.sizeAvailable());

		Engine.shutdown();
		assertNull(Engine.server);
	}

	public final void testEnginePrimaryClient() throws FileNotFoundException {
		Engine.openDatafile(TESTFILE);

		DataClient p = Engine.primaryClient();

		assertEquals(1, Engine.server.sizeInUse());
		assertEquals(0, Engine.server.sizeAvailable());

		Engine.shutdown();
		assertNull(Engine.server);
	}

	public final void testBackupDatafile() {
		try {
			Engine.openDatafile(TESTFILE);
			assertNotNull(Engine.server);

			/*
			 * Use existing temporary file as the prefix.
			 */
			String filename = Engine.backupToFile(TESTFILE);

			File original = new File(TESTFILE);
			File backup = new File(filename);

			assertTrue("Backup file not present!", backup.exists());
			assertEquals("Size mismatch - backup should be identical to original", original.length(), backup.length());
		} catch (FileNotFoundException fnfe) {
			fail("Testing datafile is supposed to exist already");
		} catch (IOException ioe) {
			fail("IO interruption while doing backup");
		}
	}
}
