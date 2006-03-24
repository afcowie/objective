/*
 * OprDynOutputDump.java
 * 
 * See LICENCE file for usage and redistribution terms
 * Copyright (c) 2005-2006 Operational Dynamics
 */
package demo.ui;

import accounts.domain.Books;
import accounts.persistence.DataStore;
import accounts.services.DatafileServices;
import accounts.ui.AccountTextOutput;
import accounts.ui.TextOutput;
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
		DataStore store = null;
		TextOutput outputter = null;

		try {
			store = DatafileServices.openDatafile(DemoBooksSetup.DEMO_DATABASE);

			Books root = store.getBooks();

			System.out.println();

			/*
			 * First output all the Accounts
			 */

			outputter = new AccountTextOutput(store);
			outputter.toOutput(System.out);

			/*
			 * And now output all the Transactions
			 */
			System.out.println();

			outputter = new TransactionTextOutput(store);
			outputter.toOutput(System.out);

			System.out.flush();

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (store != null) {
				store.close();
			}
		}
	}
}