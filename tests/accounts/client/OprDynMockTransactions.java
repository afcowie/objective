/*
 * OprDynBooksSetup.java
 * 
 * See LICENCE file for usage and redistribution terms
 * Copyright (c) 2005 Operational Dynamics
 */
package accounts.client;

import generic.util.Debug;

import java.io.FileNotFoundException;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import accounts.domain.Account;
import accounts.domain.Amount;
import accounts.domain.Credit;
import accounts.domain.Datestamp;
import accounts.domain.Debit;
import accounts.domain.Entry;
import accounts.domain.GenericTransaction;
import accounts.domain.Ledger;
import accounts.domain.Transaction;
import accounts.persistence.UnitOfWork;
import accounts.services.CommandNotReadyException;
import accounts.services.DatafileServices;
import accounts.services.PostTransactionCommand;

/**
 * Contains a prelinary main() method and program initialization (much of which
 * will move to an eventual Client class). The remainder is a huge number of
 * Commands, instantiated programmatically, to set up *our* books. At some
 * point, probably after Balance Sheet, Income Statement, and Verification test
 * are complete, we will stop using this.
 * <P>
 * This is all programmatic - there's no GUI here.
 * <P>
 * <B>NOTE THAT RUNNING THIS DESTROYS ANY EXISTING INSTANCE OF THE DEMO_DATABASE</B>
 */
public class OprDynMockTransactions
{
	public final static String	DEMO_DATABASE	= OprDynBooksSetup.DEMO_DATABASE;

	public static void main(String[] args) {
		Debug.setProgname("mock");
		Debug.register("main");
		Debug.register("command");
		Debug.register("memory");

		args = Debug.init(args);
		Debug.print("main", "Starting OprDynMockTransactions");

		Debug.print("main", "Openning database " + DEMO_DATABASE);
		try {
			ObjectiveAccounts.store = DatafileServices.openDatafile(DEMO_DATABASE);
		} catch (FileNotFoundException fnfe) {
			System.err.println("\nDemo database not found! Did you run OprDynBooksSetup?\n");
			System.exit(1);
		}

		UnitOfWork uow = null;

		Debug.print("main", "Running commands...");
		try {
			uow = new UnitOfWork("Add Transactions to Demo Books");

			Ledger gstPayable = null;
			Ledger pettyCash = null;
			Ledger ownersEquity = null;

			/*
			 * Fetch some accounts' ledgers
			 */

			Ledger protoL = new Ledger();
			protoL.setName("Paid");

			// TODO need query account+ledger exposure on DataStore beside
			// getBooks

			List result = ObjectiveAccounts.store.query(protoL);

			if (result.size() == 1) {
				gstPayable = (Ledger) result.get(0);
			} else {
				throw new Exception(result.size() + " results returned, as opposed to the 1 we wanted");
			}

			Account protoA = new Account();
			protoA.setTitle("Petty Cash");

			result = ObjectiveAccounts.store.query(protoA);

			if (result.size() == 3) {
				Account p = (Account) result.get(2);
				Set s = p.getLedgers();
				Iterator iter = s.iterator();
				pettyCash = (Ledger) iter.next();
			} else {
				throw new Exception(result.size() + " results returned, as opposed to the 1 we wanted");
			}

			protoA = new Account();
			protoA.setTitle("Owner's Equity");

			result = ObjectiveAccounts.store.query(protoA);

			if (result.size() == 1) {
				Account p = (Account) result.get(0);
				Set s = p.getLedgers();
				Iterator iter = s.iterator();
				ownersEquity = (Ledger) iter.next();
			} else {
				throw new Exception(result.size() + " results returned, as opposed to the 1 we wanted");
			}

			/*
			 * Now start storing some transactions
			 */

			Transaction[] initialization = {
				new GenericTransaction("Initial capitalization", new Datestamp("19 Dec 03"), new Entry[] {
						new Credit(new Amount("1.00"), ownersEquity), new Debit(new Amount("1.00"), pettyCash),
				}),
			};

			for (int i = 0; i < initialization.length; i++) {
				PostTransactionCommand cmd = new PostTransactionCommand(initialization[i]);
				cmd.execute(uow);
			}

			Debug.print("main", "Committing.");
			uow.commit();
		} catch (CommandNotReadyException cnre) {
			uow.cancel();
			throw new IllegalStateException("Shouldn't have had a problem with any commands being not ready!");
		} catch (Exception e) {
			uow.cancel();
			e.printStackTrace();
		}

		ObjectiveAccounts.store.close();
		Debug.print("main", "Done.");
	}
}