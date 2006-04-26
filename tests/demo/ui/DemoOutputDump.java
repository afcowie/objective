/*
 * OprDynOutputDump.java
 * 
 * See LICENCE file for usage and redistribution terms
 * Copyright (c) 2005-2006 Operational Dynamics
 */
package demo.ui;

import generic.ui.TextOutput;

import java.io.FileNotFoundException;

import accounts.domain.Books;
import accounts.persistence.DataClient;
import accounts.persistence.Engine;
import accounts.ui.AccountTextOutput;
import accounts.ui.TransactionTextOutput;
import demo.client.DemoBooksSetup;

/**
 * Use the toOuput() routine to dump the demo database.
 * 
 * @author Andrew Cowie
 */
public class DemoOutputDump
{
	public static void main(String[] args) {
		TextOutput outputter = null;

		try {
			Engine.openDatafile(DemoBooksSetup.DEMO_DATABASE);
		} catch (FileNotFoundException fnfe) {
			System.err.println("\nDemo database not found! Did you run DemoBooksSetup?\n");
			System.exit(1);
		}

		DataClient ro = Engine.primaryClient();
		try {
			Books root = ro.getBooks();

			System.out.println();

			/*
			 * First output all the Accounts
			 */

			outputter = new AccountTextOutput(ro);
			outputter.toOutput(System.out);

			/*
			 * And now output all the Transactions
			 */
			System.out.println();

			outputter = new TransactionTextOutput(ro);
			outputter.toOutput(System.out);

			System.out.flush();

		} catch (Exception e) {
			e.printStackTrace();
		}

		Engine.shutdown();
	}
}