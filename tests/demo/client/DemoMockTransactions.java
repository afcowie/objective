/*
 * OprDynBooksSetup.java
 * 
 * See LICENCE file for usage and redistribution terms
 * Copyright (c) 2005-2006 Operational Dynamics
 */
package demo.client;

import generic.util.Debug;

import java.io.FileNotFoundException;

import accounts.client.ObjectiveAccounts;
import accounts.domain.Amount;
import accounts.domain.Client;
import accounts.domain.Credit;
import accounts.domain.Datestamp;
import accounts.domain.Debit;
import accounts.domain.Employee;
import accounts.domain.Entity;
import accounts.domain.Entry;
import accounts.domain.GenericTransaction;
import accounts.domain.Ledger;
import accounts.domain.Supplier;
import accounts.domain.Transaction;
import accounts.persistence.DataStore;
import accounts.persistence.UnitOfWork;
import accounts.services.AddEntityCommand;
import accounts.services.CommandNotReadyException;
import accounts.services.DatafileServices;
import accounts.services.PostTransactionCommand;
import accounts.services.SpecificLedgerFinder;
import accounts.services.StoreObjectCommand;

/**
 * Contains a prelinary main() method and program initialization (much of which
 * will move to an eventual Client class). The remainder is a huge number of
 * Commands, instantiated programmatically, to run a bunch of transactions.
 */
public class DemoMockTransactions
{
	public final static String	DEMO_DATABASE	= DemoBooksSetup.DEMO_DATABASE;

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
			System.err.println("\nDemo database not found! Did you run DemoBooksSetup?\n");
			System.exit(1);
		}

		UnitOfWork uow = null;

		Debug.print("main", "Running commands...");
		try {
			uow = new UnitOfWork("Add Transactions to Demo Books");

			/*
			 * Fetch some accounts' ledgers
			 */
			DataStore store = ObjectiveAccounts.store;

			SpecificLedgerFinder finder = new SpecificLedgerFinder();
			finder.setAccountTitle("ANZ");
			finder.setLedgerName("Current Account");
			Ledger bankAccount = finder.getLedger();

			finder.setAccountTitle("Petty Cash");
			finder.setLedgerName("Manly");
			Ledger pettyCash = finder.getLedger();

			finder.setAccountTitle("Furniture");
			finder.setLedgerName("Cost");
			Ledger furniture = finder.getLedger();

			finder.setAccountTitle("GST");
			finder.setLedgerName("Collected");
			Ledger gstCollected = finder.getLedger();

			finder.setAccountTitle("Shareholder");
			finder.setLedgerName("Cowie");
			Ledger shareholdersLoan = finder.getLedger();

			finder.setAccountTitle("GST");
			finder.setLedgerName("Paid");
			Ledger gstPaid = finder.getLedger();

			finder.setAccountTitle("Owner's Equity");
			finder.setLedgerName("");
			Ledger ownersEquity = finder.getLedger();

			finder.setAccountTitle("Procedures");
			finder.setLedgerName("Fees");
			Ledger consultingRevenue = finder.getLedger();

			finder.setAccountTitle("Travel Expenses");
			finder.setLedgerName("Ground Transfer");
			Ledger groundTransport = finder.getLedger();

			/*
			 * Now start storing some transactions
			 */

			Transaction[] initialization = {
				new GenericTransaction("Initial capitalization", new Datestamp("19 Dec 03"), new Entry[] {
					new Credit(new Amount("1.00"), ownersEquity),
					new Debit(new Amount("1.00"), pettyCash),
				}),
				// FIXME change to some kind of Loan Transaction?
				new GenericTransaction("Loan from owner", new Datestamp("21 Dec 03"), new Entry[] {
					new Debit(new Amount("52700.00"), bankAccount),
					new Credit(new Amount("52700.00"), shareholdersLoan),
				}),
				new GenericTransaction("Chair for office", new Datestamp("6 Jan 04"), new Entry[] {
					new Debit(new Amount("659.10"), furniture),
					new Debit(new Amount("65.90"), gstPaid),
					new Credit(new Amount("725.00"), bankAccount),
				}),
				// FIXME change Transaction type?
				new GenericTransaction("Procedures implementation ACME, Inc", new Datestamp("29 Aug 04"), new Entry[] {
					new Debit(new Amount("21500.00"), bankAccount),
					new Credit(new Amount("19545.45"), consultingRevenue),
					new Credit(new Amount("1954.55"), gstCollected),
				}),
				new GenericTransaction("Taxi from airport", new Datestamp("5 Nov 05"), new Entry[] {
					new Credit(new Amount("9.99"), pettyCash),
					new Debit(new Amount("9.99"), groundTransport),
				}),
			};

			for (int i = 0; i < initialization.length; i++) {
				initialization[i].setIdentifier("I" + padZeros(i + 1, 4));
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

		try {
			uow = new UnitOfWork("Add some clients and suppliers");

			Entity[] entities = {
				new Client("ACME, Inc"),
				new Supplier("Katoomba Telecom"),
			};

			for (int i = 0; i < entities.length; i++) {
				AddEntityCommand aec = new AddEntityCommand(entities[i]);
				aec.execute(uow);
			}

			uow.commit();
		} catch (CommandNotReadyException cnre) {
			uow.cancel();
			throw new IllegalStateException("Shouldn't have had a problem with any commands being not ready!");
		} catch (Exception e) {
			uow.cancel();
			e.printStackTrace();
		}

		try {
			uow = new UnitOfWork("Add some employees");

			Employee[] staff = {
				new Employee("Andrew Cowie"),
				new Employee("Katrina Ross"),
			};

			for (int i = 0; i < staff.length; i++) {
				// FIXME replace with domain specific command! Perhaps
				// AddEmployeeCommmand or StoreWorkerCommand or ... to do
				// Payroll Ledger setups and whatnot
				StoreObjectCommand soc = new StoreObjectCommand(staff[i]);
				soc.execute(uow);
			}

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