/*
 * ObjectiveWindowRunner.java
 * 
 * See LICENCE file for usage and redistribution terms
 * Copyright (c) 2005 Operational Dynamics
 */
package accounts.ui;

import generic.util.Debug;

import java.io.FileNotFoundException;

import org.gnu.gtk.Gtk;

import accounts.client.ObjectiveAccounts;
import accounts.client.OprDynBooksSetup;
import accounts.services.DatafileServices;

public class ObjectiveWindowRunner
{

	public static void main(String[] args) {
		Debug.setProgname("windowrunner");
		Debug.register("main");
		Debug.register("debug");	// temporary debugs only!
		Debug.register("events");
		Debug.register("listeners");
		Debug.register("threads");

		args = Debug.init(args);
		Debug.print("main", "Starting ObjectiveWindowRunner");

		Debug.print("main", "Loading demo books");

		try {
			ObjectiveAccounts.store = DatafileServices.openDatafile(OprDynBooksSetup.DEMO_DATABASE);
		} catch (FileNotFoundException fnfe) {
			System.err.println("You need to run OprDynBooksSetup to create the demo dataset.");
			System.exit(1);
		}

		Debug.print("main", "Initializing Gtk");
		Gtk.init(args);

		// Debug.print("main", "initializing AccountTypeSelectorDialog");
		// AccountTypeSelectorDialog selector = new AccountTypeSelectorDialog();
		Debug.print("main", "Initializing ReimbursableExpensesEditorWindow");
		ReimbursableExpensesEditorWindow reimburse = new ReimbursableExpensesEditorWindow();

		Debug.print("main", "Starting Gtk main loop");
		Gtk.main();
		Debug.print("main", "Returned from Gtk main loop");
		ObjectiveAccounts.store.close();
	}
}
