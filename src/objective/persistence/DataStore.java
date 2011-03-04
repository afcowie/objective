/*
 * ObjectiveAccounts, accounting for small professional services firms.
 *
 * Copyright © 2011 Operational Dynamics Consulting, Pty Ltd
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
package objective.persistence;

import java.util.ArrayList;
import java.util.HashMap;

import objective.domain.Account;
import objective.domain.AccountsPayableAccount;
import objective.domain.AccountsReceivableAccount;
import objective.domain.BankAccount;
import objective.domain.Credit;
import objective.domain.CreditPositiveLedger;
import objective.domain.Currency;
import objective.domain.Datestamp;
import objective.domain.Debit;
import objective.domain.DebitPositiveLedger;
import objective.domain.Entry;
import objective.domain.GenericExpenseAccount;
import objective.domain.GenericTransaction;
import objective.domain.InvoiceTransaction;
import objective.domain.Ledger;
import objective.domain.PaymentTransaction;
import objective.domain.ProfessionalRevenueAccount;
import objective.domain.ReimbursableExpensesPayableAccount;
import objective.domain.Transaction;

import com.operationaldynamics.sqlite.Database;
import com.operationaldynamics.sqlite.Statement;

public class DataStore
{
    private Database db;

    /**
     * Operations using this connection.
     */
    private ArrayList<Operation> operations;

    public DataStore(String filename) {
        db = new Database(filename);
        operations = new ArrayList<Operation>();

        setupCaches();
        loadCurrencies();
        loadAccounts();
        loadLedgers();
        loadTransactions();
    }

    /**
     * Gain access to the database connection. This registers, so that we can
     * clean up later.
     */
    public Database gainConnection(Operation op) {
        operations.add(op);
        return db;
    }

    public void begin() {
        final Statement stmt;

        stmt = db.prepare("BEGIN");
        stmt.step();
        stmt.finish();
    }

    public void commit() {
        final Statement stmt;

        stmt = db.prepare("COMMIT");
        stmt.step();
        stmt.finish();
    }

    public void rollback() {
        final Statement stmt;

        stmt = db.prepare("ROLLBACK");
        stmt.step();
        stmt.finish();
    }

    public void close() {
        for (Operation op : operations) {
            op.release();
        }
        db.close();
    }

    private HashMap<Long, Account> accounts;

    private void cache(Account a) {
        accounts.put(a.getID(), a);
    }

    private HashMap<Long, Ledger> ledgers;

    private void cache(Ledger l) {
        ledgers.put(l.getID(), l);
    }

    private HashMap<Long, Transaction> transactions;

    private void cache(Transaction t) {
        transactions.put(t.getID(), t);
    }

    private HashMap<Long, Entry> entries;

    private void cache(Entry e) {
        entries.put(e.getID(), e);
    }

    private HashMap<String, Currency> currencies;

    private void cache(Currency c) {
        currencies.put(c.getCode(), c);
    }

    private void setupCaches() {
        accounts = new HashMap<Long, Account>();
        ledgers = new HashMap<Long, Ledger>();
        transactions = new HashMap<Long, Transaction>();
        entries = new HashMap<Long, Entry>();
        currencies = new HashMap<String, Currency>();
    }

    /**
     * Load and cache Currency objects.
     */
    private void loadCurrencies() {
        final Statement stmt;
        String code, name, symbol;
        Currency currency;

        stmt = db.prepare("SELECT c.code, c.name, c.symbol FROM currencies c");

        while (stmt.step()) {
            code = stmt.columnText(0);
            name = stmt.columnText(1);
            symbol = stmt.columnText(2);

            currency = makeCurrency(code, name, symbol);

            this.cache(currency);
        }

        stmt.finish();
    }

    private static Currency makeCurrency(String code, String name, String symbol) {
        return new Currency(code, name, symbol);
    }

    /**
     * Load and cache all Account objects from the database.
     */
    private void loadAccounts() {
        final Statement stmt;
        long accountId, direction;
        String type, title;
        Account account;

        stmt = db.prepare("SELECT a.account_id, a.title, y.class, a.direction FROM accounts a, types y WHERE a.type_id = y.type_id");

        while (stmt.step()) {
            accountId = stmt.columnInteger(0);
            title = stmt.columnText(1);
            type = stmt.columnText(2);
            direction = stmt.columnInteger(3);

            account = makeAccount(accountId, type, title, direction);

            this.cache(account);
        }

        stmt.finish();
    }

    /**
     * Load and cache all Ledgers from the database. Requires that Accounts
     * and Currencies already be loaded.
     */
    private void loadLedgers() {
        final Statement stmt;
        long ledgerId, accountId, direction;
        String type, name, code;
        Account parent;
        Currency currency;
        Ledger ledger;

        stmt = db.prepare("SELECT l.ledger_id, l.account_id, l.name, l.currency, l.direction FROM ledgers l");

        while (stmt.step()) {
            ledgerId = stmt.columnInteger(0);
            accountId = stmt.columnInteger(1);
            name = stmt.columnText(2);
            code = stmt.columnText(3);
            direction = stmt.columnInteger(4);

            parent = lookupAccount(accountId);
            currency = lookupCurrency(code);

            ledger = makeLedger(ledgerId, parent, name, currency, direction);

            this.cache(ledger);
        }

        stmt.finish();
    }

    /**
     * Construct a Ledger object corresponding to the given rowid.
     */
    private static Ledger makeLedger(long ledgerId, Account parent, String name, Currency currency,
            long direction) {
        final Ledger result;

        if (direction == 1) {
            result = new DebitPositiveLedger(ledgerId);
        } else if (direction == -1) {
            result = new CreditPositiveLedger(ledgerId);
        } else {
            throw new IllegalArgumentException("\n" + "Can't have directionless Entries");
        }

        result.setName(name);
        result.setParentAccount(parent);
        result.setCurrency(currency);

        return result;
    }

    /**
     * Get the specified Account object.
     */
    public Account lookupAccount(final long accountId) {
        final Account result;

        result = accounts.get(accountId);
        if (result == null) {
            throw new IllegalStateException("We require all Accounts to be preloaded");
        }

        return result;
    }

    /**
     * Get the specified Ledger object.
     */
    public Ledger lookupLedger(final long ledgerId) {
        final Ledger result;

        result = ledgers.get(ledgerId);
        if (result == null) {
            throw new IllegalStateException("We require all Ledgers to be preloaded");
        }

        return result;
    }

    /**
     * Get the Currency object corresponding to the specified currency code.
     */
    public Currency lookupCurrency(final String code) {
        final Currency result;

        /*
         * Special handling for the "home currency" case.
         */

        if (code == null) {
            return null;
        }

        result = currencies.get(code);
        if (result == null) {
            throw new IllegalStateException("We require all Currencies to be preloaded");
        }

        return result;
    }

    /**
     * Given values, build an Account object.
     */
    /*
     * Could have used reflection, of course but this is just as simple and
     * much more strongly typed.
     */
    private static Account makeAccount(long accountId, String type, String title, long direction) {
        final Account result;

        if (direction == 1) {
            if (type.equals("objective.domain.BankAccount")) {
                result = new BankAccount(accountId);
            } else if (type.equals("objective.domain.AccountsReceivableAccount")) {
                result = new AccountsReceivableAccount(accountId);
            } else if (type.equals("objective.domain.GenericExpenseAccount")) {
                result = new GenericExpenseAccount(accountId);
            } else {
                result = null;
            }
        } else if (direction == -1) {
            if (type.equals("objective.domain.AccountsPayableAccount")) {
                result = new AccountsPayableAccount(accountId);
            } else if (type.equals("objective.domain.ReimbursableExpensesPayableAccount")) {
                result = new ReimbursableExpensesPayableAccount(accountId);
            } else if (type.equals("objective.domain.ProfessionalRevenueAccount")) {
                result = new ProfessionalRevenueAccount(accountId);
            } else if (type.equals("objective.domain.CurrencyGainLossAccount")) {
                result = null; // REMOVE?
            } else {
                result = null;
            }
        } else {
            throw new IllegalStateException("Can't have directionless Accounts " + accountId);
        }

        if (result == null) {
            throw new IllegalArgumentException("\n" + "Unknown Account type " + type + " for ("
                    + accountId + ")");
        }

        result.setTitle(title);
        return result;
    }

    /*
     * This is not ideal; once we get up to speed we are NOT going to want to
     * load the entire list of Transactions, especially just to do normal data
     * entry.
     */
    private void loadTransactions() {
        Statement stmt;
        String[] sql;
        long transactionId, timestamp, entryId, ledgerId, amount, value, direction;
        String type, description, reference, code;
        Datestamp date;
        Transaction transaction;
        Ledger ledger;
        Entry entry;
        Currency currency;

        sql = new String[] {
            "SELECT t.transaction_id, y.class, t.datestamp, t.description, t.reference",
            "FROM transactions t, types y",
            "WHERE t.type_id = y.type_id"
        };

        stmt = db.prepare(combine(sql));

        while (stmt.step()) {
            transactionId = stmt.columnInteger(0);
            type = stmt.columnText(1);
            timestamp = stmt.columnInteger(2);
            description = stmt.columnText(3);
            reference = stmt.columnText(4);

            date = new Datestamp(timestamp);
            transaction = makeTransaction(transactionId, type, date, description, reference);

            this.cache(transaction);
        }

        stmt.finish();

        sql = new String[] {
            "SELECT e.entry_id, e.transaction_id, e.ledger_id, e.amount, e.currency, e.value, e.direction",
            "FROM entries e"
        };

        stmt = db.prepare(combine(sql));

        while (stmt.step()) {
            entryId = stmt.columnInteger(0);
            transactionId = stmt.columnInteger(1);
            ledgerId = stmt.columnInteger(2);
            amount = stmt.columnInteger(3);
            code = stmt.columnText(4);
            value = stmt.columnInteger(5);
            direction = stmt.columnInteger(6);

            transaction = lookupTransaction(transactionId);
            ledger = lookupLedger(ledgerId);
            currency = lookupCurrency(code);

            entry = makeEntry(entryId, transaction, ledger, amount, currency, value, direction);

            this.cache(entry);
        }

        stmt.finish();
    }

    /**
     * Get the specified Transaction.
     */
    public Transaction lookupTransaction(final long transactionId) {
        final Transaction result;

        result = transactions.get(transactionId);
        if (result == null) {
            /*
             * There's no reason we couldn't do lazy loading here.
             */
            throw new IllegalStateException("\n" + "Transaction (" + transactionId + ") isn't loaded");
        }

        return result;
    }

    public Transaction[] listTransactions() {
        final Transaction[] result;
        final int num;
        int i;
        long transactionId;
        Transaction transaction;
        Statement stmt;

        stmt = db.prepare("SELECT count() FROM transactions");
        stmt.step();
        num = (int) stmt.columnInteger(0);
        stmt.finish();

        result = new Transaction[num];

        stmt = db.prepare("SELECT transaction_id FROM transactions ORDER BY datestamp");

        i = 0;
        while (stmt.step()) {
            transactionId = stmt.columnInteger(0);
            transaction = lookupTransaction(transactionId);
            result[i] = transaction;
            i++;
        }
        stmt.finish();

        return result;
    }

    /**
     * Get the Entry object proxying the specified rowid.
     */
    public Entry lookupEntry(final long entryId) {
        final Entry result;

        result = entries.get(entryId);
        if (result == null) {
            /*
             * There's no reason we couldn't do lazy loading here.
             */
            throw new IllegalStateException("Entry (" + entryId + ") isn't loaded");
        }

        return result;
    }

    /*
     * Again, could have used reflection but this is cleaner and much more
     * strongly typed.
     */
    private static Transaction makeTransaction(long transactionId, String type, Datestamp date,
            String description, String reference) {
        final Transaction result;

        if (type.equals("objective.domain.GenericTransaction")) {
            result = new GenericTransaction(transactionId);
        } else if (type.equals("objective.domain.InvoiceTransaction")) {
            result = new InvoiceTransaction(transactionId);
        } else if (type.equals("objective.domain.PaymentTransaction")) {
            result = new PaymentTransaction(transactionId);
        } else {
            throw new IllegalStateException("\n" + "Unknown Account type " + type + " for ("
                    + transactionId + ")");
        }

        result.setDate(date);
        result.setDescription(description);
        result.setReference(reference);

        return result;
    }

    /**
     * Given queried values and existing proxy objects, construct an Entry
     * object representing the given rowid.
     */
    private static Entry makeEntry(long rowid, Transaction transaction, Ledger ledger, long amount,
            Currency currency, long value, long direction) {
        final Entry result;

        if (direction == 1) {
            result = new Debit(rowid);
        } else if (direction == -1) {
            result = new Credit(rowid);
        } else {
            throw new IllegalArgumentException();
        }

        result.setParentTransaction(transaction);
        result.setParentLedger(ledger);

        result.setAmount(amount);
        result.setCurrency(currency);
        result.setValue(value);

        return result;
    }

    /**
     * Combine an array of Strings into a single String. Use this for
     * constructing SQL statements.
     */
    private static String combine(String[] sql) {
        StringBuilder buf;
        int len, i;

        len = 0;

        for (i = 0; i < sql.length; i++) {
            len += sql[i].length();
        }

        buf = new StringBuilder(len);

        for (i = 0; i < sql.length; i++) {
            if (i > 0) {
                buf.append(' ');
            }

            buf.append(sql[i]);
        }

        return buf.toString();
    }

    /**
     * Insert a newly formed Transaction object into the database. You should
     * immediately follow this with a call to
     * {@link DataStore#updateTransaction(Transaction) updateTransaction()}.
     */
    public void createTransaction(Transaction t) {
        Statement stmt;
        final String name;
        final long typeId, rowId;

        name = t.getClass().getName();

        stmt = db.prepare("SELECT type_id FROM types WHERE class = ?");
        stmt.bindText(1, name);
        stmt.step();

        typeId = stmt.columnInteger(0);
        stmt.finish();

        stmt = db.prepare("INSERT INTO transactions VALUES (NULL, ?, -1, NULL, NULL)");
        stmt.bindInteger(1, typeId);
        stmt.step();
        stmt.finish();

        rowId = db.lastInsertRowID();
        t.setID(rowId);

        /*
         * Now that a new Transaction exists, cache our reference to it.
         */

        this.cache(t);
    }

    /**
     * Update a Transaction in the database.
     */
    public void updateTransaction(Transaction t) {
        final Statement stmt;
        final Datestamp date;
        final long transactionId, datestamp;
        final String description, reference;

        stmt = db.prepare("UPDATE transactions SET datestamp = ?, description = ?, reference = ? WHERE transaction_id = ?");

        date = t.getDate();
        datestamp = date.getInternalTimestamp();
        stmt.bindInteger(1, datestamp);

        description = t.getDescription();
        stmt.bindText(2, description);

        reference = t.getReference();
        if (reference == null) {
            stmt.bindNull(3);
        } else {
            stmt.bindText(3, reference);
        }

        transactionId = t.getID();
        stmt.bindInteger(4, transactionId);

        stmt.step();
        stmt.finish();
    }

    /**
     * Update an Entry in the database.
     */
    public void updateEntry(Entry e) {
        final String[] sql;
        final Statement stmt;
        final Ledger l;
        final long ledgerId, amount, value, entryId;
        final Currency currency;
        final String code;

        sql = new String[] {
            "UPDATE entries",
            "SET ledger_id = ?, amount = ?, currency = ?, value = ?",
            "WHERE entry_id = ?"
        };

        stmt = db.prepare(combine(sql));

        l = e.getParentLedger();
        ledgerId = l.getID();
        stmt.bindInteger(1, ledgerId);

        currency = e.getCurrency();
        if (currency == null) {
            throw new AssertionError();
        }

        amount = e.getAmount();
        stmt.bindInteger(2, amount);

        code = currency.getCode();
        stmt.bindText(3, code);

        value = e.getValue();
        stmt.bindInteger(4, value);

        entryId = e.getID();
        stmt.bindInteger(5, entryId);

        stmt.step();
        stmt.finish();
    }

    /**
     * Insert the given newly constructed Entry into the database.
     */
    public void createEntry(Entry e) {
        Statement stmt;
        final Transaction transaction;
        final Ledger ledger;
        final long transactionId, ledgerId, direction, rowId;

        transaction = e.getParentTransaction();
        if (transaction == null) {
            throw new AssertionError();
        }
        transactionId = transaction.getID();

        ledger = e.getParentLedger();
        if (ledger == null) {
            throw new AssertionError();
        }
        ledgerId = ledger.getID();

        if (e instanceof Debit) {
            direction = 1;
        } else if (e instanceof Credit) {
            direction = -1;
        } else {
            throw new AssertionError();
        }

        stmt = db.prepare("INSERT INTO entries VALUES (NULL, ?, ?, 0, NULL, 0, ?)");
        stmt.bindInteger(1, transactionId);
        stmt.bindInteger(2, ledgerId);
        stmt.bindInteger(3, direction);

        stmt.step();
        stmt.finish();

        rowId = db.lastInsertRowID();
        e.setID(rowId);

        /*
         * Cache our reference to the new Entry.
         */

        this.cache(e);
    }
}