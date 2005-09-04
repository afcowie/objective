/*
 * DatafileTest.java
 * 
 * See LICENCE file for usage and redistribution terms
 * Copyright (c) 2005 Operational Dynamics
 */
package accounts.services;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import accounts.persistence.DataStore;
import junit.framework.TestCase;

/**
 * @author Andrew Cowie
 */
public class DatafileTest extends TestCase
{

	private static String	TMPDBFILE	= "tmp/unittests/DatafileTest.yap";

	public final void testNewDatafile() {
		File probe = new File(TMPDBFILE);
		/*
		 * Start fresh
		 */
		probe.delete();

		/*
		 * Main method under test
		 */
		DataStore store = DatafileServices.newDatafile(TMPDBFILE);
		assertNotNull(store);
		assertTrue("New datafile doesn't exist!", probe.exists());

		try {
			DatafileServices.newDatafile(TMPDBFILE);
		} catch (IllegalArgumentException iae) {
			// it's supposed to throw this.
		} catch (Exception e) {
			fail("Didn't throw IllegalArugmentException it should have, " + e.toString());
		}
		store.close();
		assertTrue(store.getContainer().ext().isClosed());
	}

	public final void testOpenAndUseDatafile() {
		DataStore store = null;
		try {
			store = DatafileServices.openDatafile(TMPDBFILE);
		} catch (FileNotFoundException fnfe) {
			fail("Testing datafile is supposed to exist already");
		}
		assertNotNull(store);

		// TODO MORE HERE

		store.close();
	}

	public final void testBackupDatafile() {
		DataStore store = null;
		try {
			store = DatafileServices.openDatafile(TMPDBFILE);
			assertNotNull(store);

			/*
			 * Use existing temporary file as the prefix.
			 */
			String filename = DatafileServices.backupDatafile(store, TMPDBFILE);

			store.close();

			File original = new File(TMPDBFILE);
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