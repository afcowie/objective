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
package accounts.services;

import generic.persistence.DataClient;
import generic.persistence.Engine;

import java.util.NoSuchElementException;

import objective.domain.Account;
import objective.domain.Currency;

import accounts.domain.Books;
import accounts.persistence.BlankDatafileTestCase;

public class BooksSetByCommandsTest extends BlankDatafileTestCase
{
    static {
        DATAFILE = "tmp/unittests/BooksSetByCommandsTest.yap";
    }

    public final void testDataStoreGetSetGetBooks() {
        Books root;
        try {
            root = (Books) rw.getRoot();
            fail("Should have thrown NoSuchElementException to point out uninitialized DataClient; instead you got "
                    + root);
        } catch (NoSuchElementException nsee) {
        }

        try {
            rw.setRoot(null);
            fail("Should have thrown IllegalArgumentException due to null argument");
        } catch (IllegalArgumentException iae) {
        }

        root = new Books();
        rw.setRoot(root);

        try {
            Books foo = new Books();
            rw.setRoot(foo);
            fail("Should have thrown UnsupportedOperationException due to already set (though cached, not saved) books");
        } catch (UnsupportedOperationException uoe) {
        }
        Books received = (Books) rw.getRoot();
        assertNotNull(received);
        assertSame(root, received);
    }

    public final void testInitBooksAndAddAccountsInOneUnitOfWork() throws Exception {
        /*
         * Reopen the database to make sure that the DataClients we get are
         * for sure uninitialized.
         */
        Engine.releaseClient(rw);
        Engine.shutdown();
        Engine.openDatafile(DATAFILE, Books.class);
        rw = Engine.gainClient();

        try {
            Books one = (Books) rw.getRoot();
            fail("Should have thrown NoSuchElementException to point out uninitialized database.");
        } catch (NoSuchElementException nsee) {
        }

        InitBooksCommand ibc = null;
        /*
         * Initialize a Books
         */
        try {
            Currency home = new Currency("USD", "US Dollar", "$");
            ibc = new InitBooksCommand(home);
            ibc.execute(rw);
        } catch (Exception e) {
            fail("InitBooksCommand shouldn't have thrown anything, let alone " + e);
        }

        /*
         * Now add an account, but NOT having committed the UnitOfWork. At
         * time of writing test, this caused a bug. Trap it in this clause and
         * fail.
         */
        try {
            Account a = new Account("Dummy");
            AddAccountCommand aac = new AddAccountCommand(a);
            aac.execute(rw);
        } catch (NoSuchElementException nsee) {
            rw.rollback();
            fail("aac.execute() threw a NoSuchElementException, as per bug");
        } catch (Exception e) {
            throw e;
        }
        /*
         * Makes it this far, then InitBooksCommand and DataClient cached the
         * Books instead of insisting on lookup from database. Go ahead and
         * commit now.
         */
        rw.commit();

        try {
            Books foo = new Books();
            rw.setRoot(foo);
            fail("Should have thrown UnsupportedOperationException due to already set (by InitBooksCommand) books");
        } catch (UnsupportedOperationException uoe) {
        }
    }

    public final void testUseCachedBooks() {
        /*
         * We should probably get the same client as above back, so its Books
         * is cached up...
         */
        Books received = (Books) rw.getRoot();
        assertNotNull(received);
        /*
         * But a brand new DataClient won't have a Books cached. Go fetch!
         */
        DataClient fresh = Engine.server.gainClient();
        Books another = (Books) rw.getRoot();
        assertNotNull(another);
        Engine.releaseClient(fresh);

        last = true;
    }
}
