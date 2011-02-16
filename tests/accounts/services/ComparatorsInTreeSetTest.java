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

import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

import accounts.domain.Account;
import accounts.domain.Amount;
import accounts.domain.BankAccount;
import accounts.domain.CashAccount;
import accounts.domain.Credit;
import accounts.domain.Currency;
import accounts.domain.Datestamp;
import accounts.domain.Debit;
import accounts.domain.DepreciatingAssetAccount;
import accounts.domain.Entry;
import accounts.domain.GenericTransaction;
import accounts.domain.Ledger;
import accounts.domain.OwnersEquityAccount;
import accounts.domain.SalesTaxPayableAccount;
import accounts.domain.Transaction;
import accounts.persistence.BlankDatafileTestCase;

/**
 * A large (ie long duration and much computational effort) unit test to
 * verify that deep activation is working. More or less follows the path taken
 * by `make setup dump`. Should any activation problems be discovered, figure
 * a way to make this unit test show them.
 * <p>
 * Also note that can be used as a stand alone test, ie`java -classpath
 * /usr/share/junit/lib/junit.jar:tmp/classes
 * accounts.client.Db4oActivationTest [num]`
 * 
 * @author Andrew Cowie
 */
public class ComparatorsInTreeSetTest extends BlankDatafileTestCase
{
    static {
        DATAFILE = "tmp/unittests/ComparatorsTest.yap";
    }

    /**
     * Must be even!
     */
    private static int NUM = 10;

    private static final String DATESTRING = "11 Nov 97";

    public final void testSetupBasicLedgers() {
        try {
            Currency home = new Currency("CUR", "Some Currency", "#");
            InitBooksCommand ibc = new InitBooksCommand(home);
            ibc.execute(rw);

            /*
             * Create a bunch of expense accounts. We're actually using
             * CashAccounts for the simple expedient that they are
             * SingleLedger debit positive.
             */
            CashAccount[] as = new CashAccount[NUM];

            for (int i = 0; i < NUM; i++) {
                as[i] = new CashAccount("N:" + i, "L:" + i);
                AddAccountCommand aac = new AddAccountCommand(as[i]);
                aac.execute(rw);
            }

            Account[] primes = {
                    new BankAccount("Barkleys", "Main"), new OwnersEquityAccount("GST", "Paid"),
            };

            for (int i = 0; i < primes.length; i++) {
                AddAccountCommand aac = new AddAccountCommand(primes[i]);
                aac.execute(rw);
            }

            /*
             * Treat first ledger as money account and second as if it were
             * GST.
             */
            Ledger petty = ((BankAccount) primes[0]).getLedger();
            Ledger gstpaid = ((OwnersEquityAccount) primes[1]).getLedger();

            for (int i = 0; i < NUM; i++) {
                Ledger expense = as[i].getLedger();

                Entry leftone = new Debit(new Amount("100.00"), expense);
                Entry lefttwo = new Debit(new Amount("10.00"), gstpaid);
                Entry right = new Credit(new Amount("110.00"), petty);

                Transaction gt = new GenericTransaction("T:" + i, new Datestamp(DATESTRING),
                        new Entry[] {
                                leftone, lefttwo, right
                        });

                PostTransactionCommand ptc = new PostTransactionCommand(gt);
                ptc.execute(rw);
            }

            rw.commit();
        } catch (CommandNotReadyException cnre) {
            cnre.printStackTrace();
            fail("Shouldn't have thrown an Exception populating test accounts");
        }
    }

    public final void testInitialization() {
        Amount totalDebits = new Amount("0.00");
        Amount totalCredits = new Amount("0.00");

        List eL = rw.queryByExample(Entry.class);
        Iterator eI = eL.iterator();
        while (eI.hasNext()) {
            Entry e = (Entry) eI.next();

            Amount a = e.getAmount();
            assertNotNull(a);
            assertTrue(a.getNumber() != 0);

            if (e instanceof Debit) {
                totalDebits.incrementBy(a);
            } else if (e instanceof Credit) {
                totalCredits.incrementBy(a);
            } else {
                fail("Retrieved an Entry neither Credit nor Debit");
            }
        }

        assertEquals(Integer.toString(NUM * 110) + ".00", totalDebits.getValue());
        assertEquals(Integer.toString(NUM * 110) + ".00", totalCredits.getValue());
    }

    public final void testTransactionComparator() {
        List tL = rw.queryByExample(Transaction.class);
        assertEquals(NUM, tL.size());

        TreeSet tS = new TreeSet(new TransactionComparator());
        tS.addAll(tL);

        assertEquals(NUM, tS.size());
    }

    final public void testEntryComparatorInLedgerContext() {
        List eL = rw.queryByExample(Entry.class);
        assertEquals("Original number of Entries not as expected", NUM * 3, eL.size());

        TreeSet eS = new TreeSet(new EntryComparator(new Ledger()));
        eS.addAll(eL);

        assertEquals("EntryComparator reduced the number of Entries", NUM * 3, eS.size());
    }

    final public void testEntryComparatorInTransactionContext() {
        List eL = rw.queryByExample(Entry.class);
        assertEquals("Original number of Entries not as expected", NUM * 3, eL.size());

        TreeSet eS = new TreeSet(new EntryComparator(new Transaction()));
        eS.addAll(eL);

        assertEquals("EntryComparator reduced the number of Entries", NUM * 3, eS.size());
    }

    final public void testAccountComparatorAsInitialized() {
        List aL = rw.queryByExample(Account.class);
        assertEquals("Original number of Entries not as expected", NUM + 2, aL.size());

        TreeSet aS = new TreeSet(new AccountComparator());
        aS.addAll(aL);

        assertEquals("EntryComparator reduced the number of Entries", NUM + 2, aS.size());
    }

    final public void testAccountComparatorSimilarAccounts() {
        try {
            for (int i = 0; i < NUM; i++) {
                Account a = new SalesTaxPayableAccount("GST");
                AddAccountCommand aac = new AddAccountCommand(a);
                aac.execute(rw);
            }
            for (int i = 0; i < NUM; i++) {
                Account a = new DepreciatingAssetAccount("Furniture");
                a.setCode("1-" + padZeros(i, 4));
                AddAccountCommand aac = new AddAccountCommand(a);
                aac.execute(rw);
            }

            rw.commit();
        } catch (CommandNotReadyException cnre) {
            fail("Caught CommandNotReadyException");
        }

        final int expected = 3 * NUM + 2;

        List aL = rw.queryByExample(Account.class);
        assertEquals("Original number of Entries not as expected", expected, aL.size());

        TreeSet aS = new TreeSet(new AccountComparator());
        aS.addAll(aL);

        assertEquals("EntryComparator reduced the number of Entries", expected, aS.size());

        last = true;
    }

    /**
     * Quickly prepend zeros to a number. TODO move somewhere useful.
     * Duplicated in accounts.client.OprDynMockTransactions.
     */
    protected static String padZeros(int num, int width) {
        StringBuffer buf = new StringBuffer(Integer.toString(num));

        for (int i = width - buf.length(); i > 0; i--) {
            buf.insert(0, '0');
        }

        return buf.toString();
    }
}
