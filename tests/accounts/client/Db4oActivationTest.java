/*
 * Db4oActivationTest.java
 * 
 * See LICENCE file for usage and redistribution terms
 * Copyright (c) 2005-2006 Operational Dynamics
 */
package accounts.client;

import java.util.Iterator;
import java.util.Set;

import junit.textui.TestRunner;
import accounts.domain.Account;
import accounts.domain.Amount;
import accounts.domain.Books;
import accounts.domain.CashAccount;
import accounts.domain.Credit;
import accounts.domain.Currency;
import accounts.domain.Datestamp;
import accounts.domain.Debit;
import accounts.domain.Entry;
import accounts.domain.GenericTransaction;
import accounts.domain.Ledger;
import accounts.domain.Transaction;
import accounts.persistence.BlankDatafileTestCase;
import accounts.services.AddAccountCommand;
import accounts.services.CommandNotReadyException;
import accounts.services.InitBooksCommand;
import accounts.services.PostTransactionCommand;

/**
 * A large (ie long duration and much computational effort) unit test to verify
 * that deep activation is working. More or less follows the path taken by `make
 * setup dump`. Should any activation problems be discovered, figure a way to
 * make this unit test show them.
 * <p>
 * Also note that can be used as a stand alone test, ie`java -classpath
 * /usr/share/junit/lib/junit.jar:tmp/classes accounts.client.Db4oActivationTest
 * [num]`
 * 
 * @author Andrew Cowie
 */
public class Db4oActivationTest extends BlankDatafileTestCase
{
	static {
		DATAFILE = "tmp/unittests/Db4oActivationTest.yap";
	}

	private static final int	DEFAULT			= 20;
	/**
	 * Controls the number of accounts created and the number of transactions
	 * pumped into them. Up this, considerably - to at least 100 - if you're
	 * using this unit test for tuning. Higher it is, lower fraction of time is
	 * spent in database open and what not.
	 */
	private static int			NUM_ACCOUNTS	= DEFAULT;

	private static final String	DATESTRING		= "15 Jul 04";

	final public void testSetupDeepGraph() {
		// Debug.init("all");
		// Debug.register("main");
		// Debug.register("command");
		// Debug.register("memory");
		// Debug.setProgname("activation");

		try {
			Currency home = new Currency("CUR", "Some Currency", "#");
			InitBooksCommand ibc = new InitBooksCommand(home);
			ibc.execute(rw);
			rw.commit();

			CashAccount[] as = new CashAccount[NUM_ACCOUNTS];

			for (int i = 0; i < NUM_ACCOUNTS; i++) {
				as[i] = new CashAccount("N:" + i, "L:" + i);
				AddAccountCommand aac = new AddAccountCommand(as[i]);
				aac.execute(rw);
				rw.commit();
			}

			for (int i = 0; i < NUM_ACCOUNTS; i++) {
				Ledger ll = as[i].getLedger();
				Ledger lr = as[NUM_ACCOUNTS - i - 1].getLedger();

				Entry left = new Debit(new Amount("1.00"), ll);
				Entry right = new Credit(new Amount("1.00"), lr);

				Transaction gt = new GenericTransaction("T:" + i, new Datestamp(DATESTRING), new Entry[] {
					left,
					right
				});

				PostTransactionCommand ptc = new PostTransactionCommand(gt);
				ptc.execute(rw);
				rw.commit();
			}

		} catch (CommandNotReadyException cnre) {
			cnre.printStackTrace();
			fail("Threw Exception");
		}
	}

	/*
	 * close, open
	 */

	final public void testActivation() {
		Amount totalDebits = new Amount("0.00");
		Amount totalCredits = new Amount("0.00");

		Books root = (Books) rw.getRoot();

		Set aS = root.getAccountsSet();
		Iterator aI = aS.iterator();
		while (aI.hasNext()) {
			Account a = (Account) aI.next();

			Set lS = a.getLedgers();
			Iterator lI = lS.iterator();
			while (lI.hasNext()) {
				Ledger l = (Ledger) lI.next();

				Set eS = l.getEntries();
				Iterator eI = eS.iterator();
				while (eI.hasNext()) {
					Entry e = (Entry) eI.next();

					Amount amt = e.getAmount();

					if (amt == null) {
						fail("Null Amount retreived. Activation failure?");
					}

					assertNotNull("parentTranasction.description is null", e.getParentTransaction().getDescription());

					assertEquals(DATESTRING, e.getDate().toString());

					if (e instanceof Debit) {
						totalDebits.incrementBy(e.getAmount());
					} else if (e instanceof Credit) {
						totalCredits.incrementBy(e.getAmount());
					} else {
						fail("Retrieved an Entry neither Credit nor Debit");
					}
				}
			}
		}

		assertEquals(Integer.toString(NUM_ACCOUNTS) + ".00", totalDebits.getValue());
		assertEquals(Integer.toString(NUM_ACCOUNTS) + ".00", totalCredits.getValue());

		last = true;
	}

	public static void main(String[] args) {
		if (args.length == 1) {
			int tries;
			try {
				tries = Integer.parseInt(args[0]);

				if (tries < 1) {
					throw new NumberFormatException();
				}
				if (tries > 1000) {
					throw new NumberFormatException("More than 1000 internal iterations is probably a bad idea.");
				}

				NUM_ACCOUNTS = tries;
			} catch (NumberFormatException nfe) {
				System.err.println(nfe.getMessage());
				usage();
				System.exit(1);
			}
		} else if (args.length > 1) {
			usage();
			System.exit(1);
		}
		// otherwise, leave default alone.
		TestRunner.run(Db4oActivationTest.class);
	}

	public static void usage() {
		final String[] msg = {
			"ERROR.",
			"Usage: java " + Db4oActivationTest.class.getClass() + " [size]",
			"",
			"The single optional argument must be a positive number which will determine",
			"the number of accounts, transactions and other internal iterations.",
			"Make it large (100-200) if doing tuning in order to average out startup costs.",
			"",
			"The current default is " + DEFAULT + ".",

		};
		for (int i = 0; i < msg.length; i++) {
			System.err.println(msg[i]);
		}
	}
}