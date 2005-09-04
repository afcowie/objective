/*
 * DatafileServices.java
 * 
 * See LICENCE file for usage and redistribution terms
 * Copyright (c) 2005 Operational Dynamics
 */
package accounts.services;

import generic.util.DebugException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import accounts.persistence.DataStore;

/**
 * Facade to expose service layer for manipulating accounting database files.
 * This will be static until there's a reason for it not to be.
 * 
 * @author Andrew Cowie
 */
public class DatafileServices
{
	/**
	 * Create a new datafile for use. Outside of unit tests, this should
	 * probably get called exactly once. :)
	 * 
	 * @param filename
	 * @return a new DataStore open on a new (db4o format) database file. 
	 * @throws IllegalArgumentException
	 *             if the given filename already exists (no blotto!)
	 */
	public static DataStore newDatafile(String filename) {
		File probe = new File(filename);
		if (probe.exists()) {
			throw new IllegalArgumentException("Proposed datafile already exists (or at least, a file by that name does)");
		}

		DataStore store = new DataStore(filename);

		/*
		 * Integrity check
		 */
		if (store == null) {
			throw new DebugException("got a null DataStore back, should have been a brand new one!");
		}
		return store;
	}

	/**
	 * Open an existing datafile.
	 * 
	 * @param filename
	 * @return
	 * @throws FileNotFoundException
	 *             if the specified datafile is not found.
	 */
	public static DataStore openDatafile(String filename) throws FileNotFoundException {
		File probe = new File(filename);
		if (!probe.exists()) {
			throw new FileNotFoundException();
		}

		DataStore store = new DataStore(filename);

		/*
		 * Integrity check
		 */
		if (store == null) {
			throw new DebugException("got a null DataStore back from constructor!");
		}

		return store;
	}

	/**
	 * Initiate a backup of the running DataStore.
	 * 
	 * @param store
	 * @param backupBaseFilename
	 *            The fully qualified pathname used as the prefix for the backup
	 *            file (ie, not just a directory, assumed not to end with a '/'
	 *            character).
	 * @return the created backup's filename.
	 * @throws IOException
	 * @see DataStore#backup(String)
	 */
	public static String backupDatafile(DataStore store, String backupBaseFilename) throws IOException {

		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd-HHmmss");
		String timestamp = sdf.format(new Date());

		String toFilename = backupBaseFilename + "_backup-" + timestamp;

		/*
		 * Do the backup
		 */
		store.backup(toFilename);

		return toFilename;
	}

}