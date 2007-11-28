/*
 * DemoWindowRunner.java
 * 
 * See LICENCE file for usage and redistribution terms
 * Copyright (c) 2005-2006 Operational Dynamics
 */
package demo.ui;

import generic.client.Master;
import generic.persistence.Engine;
import generic.util.Debug;

import java.io.FileNotFoundException;

import org.gnome.gtk.Gtk;

import accounts.domain.Books;
import accounts.ui.ObjectiveUserInterface;
import demo.client.DemoBooksSetup;

public class DemoWindowRunner
{

	public static void main(String[] args) {
		Debug.setProgname("windowrunner");
		Debug.register("main");
		Debug.register("debug"); // temporary debugs only!
		Debug.register("command");
		Debug.register("events");
		Debug.register("listeners");
		Debug.register("threads");
		Debug.register("memory");

		args = Debug.init(args);
		Debug.print("main", "Starting DemoWindowRunner");

		Debug.print("main", "Loading demo books");

		try {
			Engine.openDatafile(DemoBooksSetup.DEMO_DATABASE, Books.class);
		} catch (FileNotFoundException fnfe) {
			System.err.println("You need to run DemoBooksSetup to create the demo dataset.");
			System.exit(1);
		} catch (IllegalStateException ise) {
			System.err.println("The database is locked by another program (doh)");
			System.exit(2);
		}
		Debug.print("main", "Initializing Gtk");
		Gtk.init(args);

		Master.ui = new ObjectiveUserInterface();

		// Debug.print("main", "initializing AccountTypeSelectorDialog");
		// AccountTypeSelectorDialog selector = new
		// AccountTypeSelectorDialog();

		Debug.print("main", "Initializing TransactionListViewHolder");
		TransactionListViewHolder view = new TransactionListViewHolder();

		// Debug.print("main", "Initializing SingleWidgetHolder");
		// SingleWidgetHolder swh = new SingleWidgetHolder();

		// Debug.print("main", "Initializing AustralianPayrollEditorWindow");
		// AustralianPayrollEditorWindow payroll = new
		// AustralianPayrollEditorWindow();
		// payroll.present();

		Debug.print("main", "Starting Gtk main loop");
		Gtk.main();
		Debug.print("main", "Returned from Gtk main loop");
		Engine.shutdown();
		Debug.print("main", "Engine shutdown.");
	}
}
