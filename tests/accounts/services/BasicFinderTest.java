/*
 * BasicFinderTest.java
 * 
 * See LICENCE file for usage and redistribution terms
 * Copyright (c) 2006 Operational Dynamics
 */
package accounts.services;

import accounts.domain.Account;
import accounts.domain.Currency;
import accounts.domain.SalesTaxPayableAccount;
import accounts.persistence.BlankDatafileTestCase;

public class BasicFinderTest extends BlankDatafileTestCase
{
	static {
		DATAFILE = "tmp/unittests/BasicFinderTest.yap";
	}

	public final void testSpecificLedgerFinderFullyQualified() throws CommandNotReadyException {
		Command cmd;
		/*
		 * Setup an account
		 */
		Currency cur = new Currency("BLN", "Blue Nationals", ":-)");
		cmd = new InitBooksCommand(cur);
		cmd.execute(rw);

		Account a = new SalesTaxPayableAccount("GST");
		cmd = new AddAccountCommand(a);
		cmd.execute(rw);

		rw.commit();

		/*
		 * Now find it!
		 */

		SpecificLedgerFinder f = new SpecificLedgerFinder();
		f.setAccountTitle("GST");
		f.setLedgerName("Paid");

		try {
			f.query(rw);
			f.getLedger();
		} catch (NotFoundException nfe) {
			fail(nfe.getMessage());
		}
	}

	public final void testSpecificLedgerFinderNullArguments() throws CommandNotReadyException {
		SpecificLedgerFinder f = new SpecificLedgerFinder();
		f.setAccountTitle("GST");
		f.setLedgerName("");

		try {
			f.query(rw);
			f.getLedger();
			fail("Two ledgers should have been returned (assuming that's what SalesTaxPayableAccount still does) so this should have thrown UnsupportedOperationException");
		} catch (NotFoundException nfe) {
			fail(nfe.getMessage());
		} catch (UnsupportedOperationException uoe) {
			// good
		}

		f.setLedgerName("Pa");
		try {
			f.query(rw);
			f.getLedger();
		} catch (NotFoundException nfe) {
			fail(nfe.getMessage());
		} catch (UnsupportedOperationException uoe) {
			fail("Shouldn't have thrown UnsupportedOperationException with enough information to qualify it to GST|Paid");
		}

		f.setAccountTitle("");
		f.setLedgerName("Coll");

		try {
			f.query(rw);
			f.getLedger();
		} catch (NotFoundException nfe) {
			fail(nfe.getMessage());
		}

		last = true;
	}
}
