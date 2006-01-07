/*
 * OprDynBooksSetup.java
 * 
 * See LICENCE file for usage and redistribution terms
 * Copyright (c) 2005-2006 Operational Dynamics
 */
package accounts.client;

import generic.util.Debug;

import java.io.File;

import accounts.domain.Account;
import accounts.domain.AccountsPayable;
import accounts.domain.AccountsReceivable;
import accounts.domain.BankAccount;
import accounts.domain.CashAccount;
import accounts.domain.Currency;
import accounts.domain.DebitPositiveLedger;
import accounts.domain.DepreciatingAssetAccount;
import accounts.domain.GenericExpenseAccount;
import accounts.domain.Ledger;
import accounts.domain.LoanLedger;
import accounts.domain.LoanPayableAccount;
import accounts.domain.OwnersEquityAccount;
import accounts.domain.ProfessionalRevenueAccount;
import accounts.domain.TaxPayableAccount;
import accounts.persistence.UnitOfWork;
import accounts.services.AddAccountCommand;
import accounts.services.AddCurrencyCommand;
import accounts.services.CommandNotReadyException;
import accounts.services.DatafileServices;
import accounts.services.InitBooksCommand;

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
public class OprDynBooksSetup
{
	public final static String	DEMO_DATABASE	= "tmp/oprdyn.yap";

	public static void main(String[] args) {
		Debug.setProgname("setup");
		Debug.register("main");
		Debug.register("command");
		Debug.register("memory");

		args = Debug.init(args);
		Debug.print("main", "Starting OprDynBooksSetup");

		new File(DEMO_DATABASE).delete();

		Debug.print("main", "Creating database " + DEMO_DATABASE);
		ObjectiveAccounts.store = DatafileServices.newDatafile(DEMO_DATABASE);

		UnitOfWork uow = null;

		Debug.print("main", "Running commands...");
		try {
			uow = new UnitOfWork("Demo Books Setup");

			Currency home = new Currency("AUD", "Australian Dollar", "$");

			InitBooksCommand initBooks = new InitBooksCommand(home);
			initBooks.execute(uow);

			/*
			 * Create a whole ton of accounts
			 */

			Account[] realAccounts = {
					new CashAccount("Petty Cash", "Manly Office"),
					new BankAccount("ANZ", "Current Account"),
					new BankAccount("Citibank", "Chequing Account"),
					new AccountsReceivable("Trade Debtors"),
					new DepreciatingAssetAccount("Computer Equipment"),
					new DepreciatingAssetAccount("Office Equipment"),
					new DepreciatingAssetAccount("Furniture"),

					new AccountsPayable("Trade Creditors"),
					new TaxPayableAccount("GST"),
					new LoanPayableAccount("Shareholders' Loans", new LoanLedger[] {
						new LoanLedger("Andrew Cowie"),
					}),

					new OwnersEquityAccount("Owner's Equity", "Andrew Cowie"),

					new ProfessionalRevenueAccount("Strategic Planning and Board Governance", "Consulting Fees"),
					new ProfessionalRevenueAccount("Leadership & Teamwork", "Consulting Fees"),
					new ProfessionalRevenueAccount("Procedures", "Consulting Fees"),
					new ProfessionalRevenueAccount("Systems Performance", "Consulting Fees"),
					new ProfessionalRevenueAccount("Conference Speaking and Tutorials", "Speaking Fees"),
					new ProfessionalRevenueAccount("Publications", "Writing Fees"),
					new ProfessionalRevenueAccount("Internet Services", "Service Fees"),

					new GenericExpenseAccount("General and Administrative Expenses", new Ledger[] {
							new DebitPositiveLedger("Accounting Fees"),
							new DebitPositiveLedger("Legal Fees"),
							new DebitPositiveLedger("Government Fees")
					}),
					new GenericExpenseAccount("Telecommunications Expenses", new Ledger[] {
							new DebitPositiveLedger("Phone Lines, Office"),
							new DebitPositiveLedger("Phone Lines, International"),
							new DebitPositiveLedger("Phone Cards, Australia"),
							new DebitPositiveLedger("Phone Cards, International")
					}),
					new GenericExpenseAccount("Travel Expenses", new Ledger[] {
							new DebitPositiveLedger("Tickets, Air"),
							new DebitPositiveLedger("Tickets, Rail"),
							new DebitPositiveLedger("Ground Transfer"),
							new DebitPositiveLedger("Accommodation"),
							new DebitPositiveLedger("Meals"),
					}),
					new GenericExpenseAccount("Meals and Entertainment Expenses", new Ledger[] {
							new DebitPositiveLedger("Staff Meetings"),
							new DebitPositiveLedger("Client Meetings"),
					}),
					new GenericExpenseAccount("Employment Expenses", new Ledger[] {
							new DebitPositiveLedger("Staff Ammenities"),
							new DebitPositiveLedger("Salaries and Benefits"),
							new DebitPositiveLedger("Recruiting Costs"),
							new DebitPositiveLedger("Employee Medical Insurance"),
					}),
			};

			for (int i = 0; i < realAccounts.length; i++) {
				Account a = realAccounts[i];

				AddAccountCommand aac = new AddAccountCommand(a);
				aac.execute(uow);
			}

			/*
			 * Add any other accouts being used for testing purposes
			 */
			Account[] testAccounts = {
					new CashAccount("Petty Cash", "Toronto Office"),
					new CashAccount("Petty Cash", "London Office"),
			};

			for (int i = 0; i < testAccounts.length; i++) {
				Account a = testAccounts[i];

				AddAccountCommand aac = new AddAccountCommand(a);
				aac.execute(uow);
			}

			/*
			 * Now setup some other currencies
			 */

			Currency[] currencies = {
					home,
					new Currency("CAD", "Canadian Dollar", "$"),
					new Currency("USD", "United States Dollar", "$"),
					new Currency("GBP", "British Pound", "?"),
					new Currency("CHF", "Swiss Franc", "SFr"),
					new Currency("EUR", "Eurpoean Union Euro", "?"),
					new Currency("SGD", "Singaporean Dollar", "S$"),
					new Currency("INR", "Indian Rupee", "Rs")
			};

			for (int i = 0; i < currencies.length; i++) {
				Currency cur = currencies[i];

				AddCurrencyCommand acc = new AddCurrencyCommand(cur);
				acc.execute(uow);
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