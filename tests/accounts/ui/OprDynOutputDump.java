/*
 * OprDynOutputDump.java
 * 
 * See LICENCE file for usage and redistribution terms
 * Copyright (c) 2005 Operational Dynamics
 */
package accounts.ui;

import java.io.PrintWriter;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

import accounts.client.OprDynBooksSetup;
import accounts.domain.Books;
import accounts.domain.Transaction;
import accounts.persistence.DataStore;
import accounts.services.DatafileServices;
import accounts.services.TransactionComparator;

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

			/*
			 * Hack to show Transactions after Accounts
			 */
			System.out.println();
			List tL = store.query(Transaction.class);

			TreeSet sorted = new TreeSet(new TransactionComparator());
			sorted.addAll(tL);

			Iterator sI = sorted.iterator();
			while (sI.hasNext()) {
				Transaction t = (Transaction) sI.next();
				t.toOutput(out);
			}

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