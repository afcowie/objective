/*
 * OprDynBooksSetup.java
 * 
 * See LICENCE file for usage and redistribution terms
 * Copyright (c) 2005 Operational Dynamics
 */
package accounts.client;

import generic.util.Debug;

import java.io.FileNotFoundException;

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
 * Commands, instantiated programmatically, to run a bunch of transactions.
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

			/*
			 * Fetch some accounts' ledgers
			 */

			Ledger pettyCash = ObjectiveAccounts.store.getLedger("Petty Cash", "Manly");
			Ledger ownersEquity = ObjectiveAccounts.store.getLedger("Owner's Equity", "");
			Ledger groundTransport = ObjectiveAccounts.store.getLedger("Travel Expenses", "Ground Transfer");

			/*
			 * Now start storing some transactions
			 */

			Transaction[] initialization = {
					new GenericTransaction("Initial capitalization", new Datestamp("19 Dec 03"), new Entry[] {
							new Credit(new Amount("1.00"), ownersEquity), new Debit(new Amount("1.00"), pettyCash),
					}),
					new GenericTransaction("Loan from owner", new Datestamp("21 Dec 03"), new Entry[] {
							new Credit(new Amount("52700.00"), ownersEquity),
							new Debit(new Amount("52700.00"), pettyCash),
					}),
					new GenericTransaction("Taxi from airport", new Datestamp("5 Nov 05"), new Entry[] {
							new Credit(new Amount("9.99"), pettyCash), new Debit(new Amount("9.99"), groundTransport),
					}),
			};

			for (int i = 0; i < initialization.length; i++) {
				initialization[i].setIdentifier("I" + padZeros(i, 4));
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

	/**
	 * Quickly prepend zeros to a number.
	 */
	protected static String padZeros(int num, int width) {
		StringBuffer buf = new StringBuffer(Integer.toString(num));

		for (int i = width - buf.length(); i > 0; i--) {
			buf.insert(0, '0');
		}

		return buf.toString();
	}
}