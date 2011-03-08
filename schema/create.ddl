PRAGMA foreign_keys = ON;

BEGIN;

CREATE TABLE currencies
(
	code TEXT PRIMARY KEY,
	name TEXT,
	symbol TEXT
);
INSERT INTO currencies VALUES ('AUD', 'Australian Dollar', '$');
INSERT INTO currencies VALUES ('CAD', 'Canadian Dollar', '$');
INSERT INTO currencies VALUES ('USD', 'United States Dollar', '$');
INSERT INTO currencies VALUES ('GBP', 'British Pound', '£');
INSERT INTO currencies VALUES ('CHF', 'Swiss Franc', 'SFr');
INSERT INTO currencies VALUES ('EUR', 'Eurpoean Union Euro', '€');
INSERT INTO currencies VALUES ('SGD', 'Singaporean Dollar', 'S$');
INSERT INTO currencies VALUES ('INR', 'Indian Rupee', 'Rs');

CREATE TABLE accounts
(
	account_id INTEGER PRIMARY KEY,
	type_id INTEGER REFERENCES types,
	title TEXT,
	direction INTEGER REFRENCES directions
);

CREATE TABLE types
(
	type_id INTEGER PRIMARY KEY,
	class TEXT
);

CREATE TABLE directions
(
	direction INTEGER PRIMARY KEY,
	code TEXT,
	name TEXT,
	nature TEXT
);
INSERT INTO directions VALUES (1, 'DR', 'Debit', 'Debit Positive');
INSERT INTO directions VALUES (-1, 'CR', 'Credit', 'Credit Positive');

CREATE TABLE ledgers
(
	ledger_id INTEGER PRIMARY KEY,
	account_id INTEGER REFERENCES accounts,
	name TEXT,
	currency TEXT REFERENCES currencies,
	direction INTEGER REFERENCES directions
);

CREATE TABLE transactions
(
	transaction_id INTEGER PRIMARY KEY,
	type_id INTEGER REFERENCES types,
	datestamp INTEGER,
	description TEXT,
	reference TEXT
);

CREATE TABLE entries
(
	entry_id INTEGER PRIMARY KEY,
	transaction_id INTEGER REFERENCES transactions,
	ledger_id INTEGER REFERENCES ledgers,
	amount INTEGER,
	currency TEXT REFERENCES currencies,
	value INTEGER,
	direction INTEGER REFERENCES directions
);

CREATE TABLE workers
(
	worker_id INTEGER PRIMARY KEY,
	type INTEGER,
	name TEXT,
	ledger_id INTEGER REFERENCES ledgers
);

--

CREATE VIEW balances AS
SELECT
	l.ledger_id,
	sum((e.currency NOTNULL) * e.amount * e.direction) * l.direction AS amount,
	sum(e.value * e.direction) * l.direction AS value
FROM
	entries e, ledgers l
WHERE
	e.ledger_id = l.ledger_id
GROUP BY
	l.ledger_id;

END;
-- vim: filetype=text
