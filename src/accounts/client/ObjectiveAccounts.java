/*
 * ObjectiveAccounts.java
 * 
 * See LICENCE file for usage and redistribution terms
 * Copyright (c) 2005 Operational Dynamics
 */
package accounts.client;

import accounts.persistence.DataStore;

/**
 * 
 * 
 * @author Andrew Cowie
 */
public class ObjectiveAccounts
{
	public final static String VERSION = "0.1.9";
	public static DataStore store = null;
	
//	public static ObjectiveAccountsUserInterface	ui	= null;
	
	/**
	 * Go down hard. TODO Do we want to System.exit() here or elsewhere?
	 * 
	 * @param string
	 *            Message to display on abort
	 */
	public static void abort(String message) {
		System.err.println(message);
//		if (ui != null) {
//			ui.shutdown();
//		}
	}

}
