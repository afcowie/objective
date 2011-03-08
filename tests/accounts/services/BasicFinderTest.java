/*
 * ObjectiveAccounts, accounting for small professional services firms.
 *
 * Copyright © 2006-2011 Operational Dynamics Consulting, Pty Ltd
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
package accounts.services;

import objective.domain.Account;
import objective.domain.Currency;
import objective.domain.SalesTaxPayableAccount;
import objective.services.NotFoundException;
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
