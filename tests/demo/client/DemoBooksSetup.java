/*
 * ObjectiveAccounts, accounting for small professional services firms.
 *
 * Copyright © 2005-2011 Operational Dynamics Consulting, Pty Ltd
 *
 * The code in this file, and the program it is a part of, is made available
 * to you by its authors as open source software: you can redistribute it
 * and/or modify it under the terms of the GNU General Public License version
 * 2 ("GPL") as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GPL for more details.
 *
 * You should have received a copy of the GPL along with this program. If not,
 * see http://www.gnu.org/licenses/. The authors of this program may be
 * contacted via http://research.operationaldynamics.com/projects/objective/.
 */
package demo.client;

import generic.persistence.DataClient;
import generic.persistence.Engine;

import java.io.File;

import objective.domain.Account;
import objective.domain.AccountsPayableAccount;
import objective.domain.AccountsReceivableAccount;
import objective.domain.BankAccount;
import objective.domain.CashAccount;
import objective.domain.Currency;
import objective.domain.DebitPositiveLedger;
import objective.domain.DepreciatingAssetAccount;
import objective.domain.Employee;
import objective.domain.GenericExpenseAccount;
import objective.domain.Ledger;
import objective.domain.LoanPayableAccount;
import objective.domain.OwnersEquityAccount;
import objective.domain.PayrollTaxPayableAccount;
import objective.domain.ProfessionalRevenueAccount;
import objective.domain.ReimbursableExpensesPayableAccount;
import objective.domain.SalesTaxPayableAccount;
import accounts.domain.Books;
import accounts.domain.Client;
import accounts.domain.Entity;
import accounts.domain.Supplier;
import accounts.services.AddAccountCommand;
import accounts.services.AddCurrencyCommand;
import accounts.services.AddEntityCommand;
import accounts.services.AddWorkerCommand;
import accounts.services.CommandNotReadyException;
import accounts.services.InitBooksCommand;
import country.au.services.AustralianInitBooksCommand;

/**
 * Contains a prelinary main() method and program initialization (much of
 * which will move to an eventual Client class). The remainder is a huge
 * number of Commands, instantiated programmatically, to set up *our* books.
 * At some point, probably after Balance Sheet, Income Statement, and
 * Verification test are complete, we will stop using this.
 * <P>
 * This is all programmatic - there's no GUI here.
 * <P>
 * <B>NOTE THAT RUNNING THIS DESTROYS ANY EXISTING INSTANCE OF THE
 * DEMO_DATABASE</B>
 */
public class DemoBooksSetup
{
    /**
     * Location of the Demo accounts database used as mockup for trials and
     * evaluations.
     */
    public final static String DEMO_DATABASE = "tmp/demo.yap";

    public static void main(String[] args) {
        new File(DEMO_DATABASE).delete();

        Engine.newDatafile(DEMO_DATABASE, Books.class);

        DataClient rw = Engine.gainClient();

        try {
            InitBooksCommand initBooks = new AustralianInitBooksCommand();
            initBooks.execute(rw);

            /*
             * Create a whole ton of accounts
             */

            Account[] realAccounts = {
                new CashAccount("Petty Cash", "Manly Office"),
                new BankAccount("ANZ", "Current Account"),
                new BankAccount("Citibank", "Chequing Account"),
                new AccountsReceivableAccount("Trade Debtors"),
                new DepreciatingAssetAccount("Computer Equipment"),
                new DepreciatingAssetAccount("Office Equipment"),
                new DepreciatingAssetAccount("Furniture"),

                new AccountsPayableAccount("Trade Creditors"),
                new SalesTaxPayableAccount("GST"),
                new PayrollTaxPayableAccount("PAYG Withholding"),
                new ReimbursableExpensesPayableAccount("Expenses Payable"),
                new LoanPayableAccount("Shareholders' Loans", new LoanLedger[] {
                    new LoanLedger("Andrew Cowie"),
                }),

                new OwnersEquityAccount("Owner's Equity", "Andrew Cowie"),

                new ProfessionalRevenueAccount("Strategic Planning and Board Governance",
                        "Consulting Fees"),
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
                aac.execute(rw);
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
                aac.execute(rw);
            }

            rw.commit();

            /*
             * Now setup some other currencies
             */

            Currency[] currencies = {
                new Currency("CAD", "Canadian Dollar", "$"),
                new Currency("USD", "United States Dollar", "$"),
                new Currency("GBP", "British Pound", "\u00A3"),
                new Currency("CHF", "Swiss Franc", "SFr"),
                new Currency("EUR", "Eurpoean Union Euro", "\u20AC"),
                new Currency("SGD", "Singaporean Dollar", "S$"),
                new Currency("INR", "Indian Rupee", "Rs")
            };

            for (int i = 0; i < currencies.length; i++) {
                Currency cur = currencies[i];

                AddCurrencyCommand acc = new AddCurrencyCommand(cur);
                acc.execute(rw);
            }

            rw.commit();

            /*
             * Add a few notional business relations
             */

            Entity[] entities = {
                new Client("ACME, Inc"),
                new Supplier("Katoomba Telecom"),
            };

            for (int i = 0; i < entities.length; i++) {
                AddEntityCommand aec = new AddEntityCommand(entities[i]);
                aec.execute(rw);
            }

            rw.commit();

            /*
             * Add some Workers
             */

            Employee[] staff = {
                new Employee("Andrew Cowie"),
                new Employee("Katrina Ross"),
            };

            for (int i = 0; i < staff.length; i++) {
                AddWorkerCommand awc = new AddWorkerCommand(staff[i]);
                awc.execute(rw);
            }

            rw.commit();
        } catch (CommandNotReadyException cnre) {
            rw.rollback();
            throw new IllegalStateException(
                    "Shouldn't have had a problem with any commands being not ready!");
        } catch (Exception e) {
            rw.rollback();
            e.printStackTrace();
        }

        Engine.releaseClient(rw);
        Engine.shutdown();

    }
}
