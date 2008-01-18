/*
 * WorkerCommandsTest.java
 * 
 * See LICENCE file for usage and redistribution terms
 * Copyright (c) 2006 Operational Dynamics
 */
package accounts.services;

import java.util.List;
import java.util.Set;

import accounts.domain.Account;
import accounts.domain.CreditPositiveLedger;
import accounts.domain.Currency;
import accounts.domain.Employee;
import accounts.domain.Ledger;
import accounts.domain.ReimbursableExpensesPayableAccount;
import accounts.domain.Worker;
import accounts.persistence.BlankDatafileTestCase;
import accounts.persistence.IdentifierAlreadyExistsException;

public class WorkerCommandsTest extends BlankDatafileTestCase
{
    static {
        DATAFILE = "tmp/unittests/WorkerCommandsTest.yap";
    }

    public final void testWorkerName() {
        Worker daMan = new Worker();
        try {
            daMan.setName("");
            fail("Worker's setName() method is expected to blow IllegalArgumentException if you try to set a blank name");
        } catch (IllegalArgumentException iae) {
            // good
        }
    }

    public final void testAddWorkerCommandSetsUpLedger() {
        /*
         * Do some setup:
         */
        Command c;
        try {
            Currency cur = new Currency("AFP", "Antarctic Frozen Penguin Tokens", "p");
            c = new InitBooksCommand(cur);
            c.execute(rw);

            ReimbursableExpensesPayableAccount r = new ReimbursableExpensesPayableAccount(
                    "Expenses Payable");
            c = new AddAccountCommand(r);
            c.execute(rw);
        } catch (CommandNotReadyException cnre) {
            fail(cnre.getMessage());
        }

        /*
         * Now test AddWorkerCommand:
         */
        AddWorkerCommand awc;

        try {
            Worker blank = new Worker();
            awc = new AddWorkerCommand(blank);
            fail("AddWorkerCommand is expected to throw IllegalStateException if the Worker has a null name");
        } catch (IllegalStateException ise) {
            // good
        }

        Employee max = new Employee("Max Bald");

        try {
            awc = new AddWorkerCommand(max);
            awc.execute(rw);
        } catch (CommandNotReadyException cnre) {
            fail(cnre.getMessage());
        }
        rw.commit();

        /*
         * Now evaluate the results:
         */

        List result = rw.queryByExample(Worker.class);
        assertEquals(1, result.size());
        assertSame(max, result.get(0));

        result = rw.queryByExample(CreditPositiveLedger.class);
        assertEquals(1, result.size());
        Ledger maxsLedger = (Ledger) result.get(0);
        assertEquals(max.getName(), maxsLedger.getName());

        result = rw.queryByExample(ReimbursableExpensesPayableAccount.class);
        assertEquals(1, result.size());

        Account expensesPayable = (Account) result.get(0);

        Set ledgers = expensesPayable.getLedgers();
        assertEquals(1, ledgers.size());

        Ledger ledgerInExpensesPayable = (Ledger) ledgers.iterator().next();
        assertSame(maxsLedger, ledgerInExpensesPayable);

        assertSame(expensesPayable, maxsLedger.getParentAccount());
    }

    public final void testAddingDuplicateWorkerCollisionChecks() throws CommandNotReadyException {
        Employee max2 = new Employee("Max Bald");

        try {
            AddWorkerCommand awc = new AddWorkerCommand(max2);
            awc.execute(rw);
            fail("Trying to add an Employee already present should have thrown an Exception");
        } catch (IdentifierAlreadyExistsException iaee) {
            // good
        }
        rw.rollback();

        last = true;
    }
}
