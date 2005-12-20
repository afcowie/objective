/*
 * OprDynOutputDump.java
 * 
 * See LICENCE file for usage and redistribution terms
 * Copyright (c) 2005 Operational Dynamics
 */
package accounts.ui;

import java.io.PrintWriter;

import accounts.client.OprDynBooksSetup;
import accounts.domain.Books;
import accounts.persistence.DataStore;
import accounts.services.DatafileServices;

/**
 * Use the toOuput() routine to dump the demo database.
 * 
 * @author Andrew Cowie
 */
public class OprDynOutputDump
{
	public static void main(String[] args) {
		DataStore store = null;
		try {
			store = DatafileServices.openDatafile(OprDynBooksSetup.DEMO_DATABASE);

			Books root = store.getBooks();

			System.out.println();
			PrintWriter out = new PrintWriter(System.out, true);
			root.toOutput(out);
			out.flush();
			System.out.println();
			out.close();

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (store != null) {
				store.close();
			}
		}
	}
}