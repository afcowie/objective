/*
 * BasicOutputDemo.java
 * 
 * See LICENCE file for usage and redistribution terms
 * Copyright (c) 2005 Operational Dynamics
 */
package accounts.ui;

import java.io.PrintWriter;
import java.util.Set;

import accounts.domain.Amount;
import accounts.domain.BankAccount;
import accounts.domain.Books;
import accounts.domain.CashAccount;
import accounts.domain.Credit;
import accounts.domain.Debit;
import accounts.domain.GenericTransaction;
import accounts.domain.LoanLedger;
import accounts.domain.LoanPayableAccount;
import accounts.domain.OwnersEquityAccount;
import accounts.domain.RawBooks;

/**
 * Test the toOuput() routines by outputting to text a set of demo accounts.
 * 
 * @author Andrew Cowie
 */
public class BasicOutputDemo
{
	public static void main(String[] args) {
		Books root = populate();

		System.out.println();
		PrintWriter out = new PrintWriter(System.out, true);
		root.toOutput(out);
		out.flush();
		System.out.println();
		out.close();
	}

	/*
	 * TODO move this where it can be used by other classes... maybe to
	 * RawBooks! :) Actually, more likely to a helper class
	 */

	public static Books populate() {
		RawBooks root = new RawBooks();

		Set accounts = root.getAccountsSet();

		/*
		 * Create a couple of accounts
		 */
		CashAccount pettyCash = new CashAccount("Petty Cash", "Darley Road Office");
		accounts.add(pettyCash);

		OwnersEquityAccount ownerEquity = new OwnersEquityAccount("Owner's Equity", "Andrew Cowie");
		accounts.add(ownerEquity);

		/*
		 * Create a transaction.
		 */
		GenericTransaction capitalization = new GenericTransaction();
		capitalization.setDescription("Initial capitalization");

		Debit cash = new Debit(new Amount("1.00"), null);
		pettyCash.addEntry(cash);

		Credit share = new Credit(new Amount("1.00"), null);
		ownerEquity.addEntry(share);

		capitalization.addEntry(cash);
		capitalization.addEntry(share);
		if (!capitalization.isBalanced()) {
			throw new IllegalStateException("unbalanced transaction");
		}

		/*
		 * Create another transaction
		 */
		BankAccount currentAccount = new BankAccount("Big Bank", "Current Account");
		currentAccount.setCode("1-1101");
		accounts.add(currentAccount);

		LoanPayableAccount shareholdersLoans = new LoanPayableAccount();
		shareholdersLoans.setTitle("Shareholder's Loans");
		shareholdersLoans.setCode("2-5000");
		LoanLedger ledger = new LoanLedger();
		ledger.setName("Andrew Cowie");
		shareholdersLoans.addLedger(ledger);

		accounts.add(shareholdersLoans);

		GenericTransaction loan = new GenericTransaction();
		loan.setDescription("Loan from shareholder");
		Debit deposit = new Debit(new Amount("1600.00"), null);
		currentAccount.addEntry(deposit);

		Credit lent = new Credit(new Amount("1600.00"), null);
		ledger.addEntry(lent);

		loan.addEntry(deposit);
		loan.addEntry(lent);

		if (!loan.isBalanced()) {
			throw new IllegalStateException("unbalanced transaction");
		}

		/*
		 * And...
		 */
		return root;
	}
}