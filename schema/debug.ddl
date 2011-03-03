PRAGMA foreign_keys = ON;
.load ../tmp/native/amount.so

BEGIN;


CREATE TEMPORARY VIEW list_accounts AS
SELECT
	pad(a.title, 37),
	money(sum(b.value), NULL, a.direction, 1) AS debit,
	money(sum(b.value), NULL, a.direction, -1) AS credit
FROM
	accounts a, ledgers l, balances b
WHERE
	l.account_id = a.account_id AND
	l.ledger_id = b.ledger_id
GROUP BY
	a.account_id;

--

CREATE TEMPORARY VIEW list_ledgers AS
SELECT
	pad(a.title || ' » ' || l.name, 37),
	money(b.value, NULL, l.direction, 1) AS debit,
	money(b.value, NULL, l.direction, -1) AS credit
FROM
	accounts a, ledgers l, balances b
WHERE
	l.account_id = a.account_id AND
	l.ledger_id = b.ledger_id
GROUP BY
	l.ledger_id;

--


CREATE TEMPORARY VIEW list_amounts AS
SELECT
	pad(a.title || ' » ' || l.name, 37),
	money(b.amount, l.currency, l.direction, 1) AS debit,
	money(b.amount, l.currency, l.direction, -1) AS credit
FROM
	accounts a, ledgers l, balances b
WHERE
	l.currency NOTNULL AND
	l.account_id = a.account_id AND
	l.ledger_id = b.ledger_id
GROUP BY
	l.ledger_id;


--

CREATE TEMPORARY VIEW list_transactions_amounts AS
SELECT
	date(t.datestamp, 'unixepoch') AS datestamp,
	pad(t.description, 13) AS description,
	pad(substr(a.title, 0, 12) || ' » ' || l.name, 25) AS "account,ledger",
	money(e.amount, e.currency, e.direction, 1) AS debit,
	money(e.amount, e.currency, e.direction, -1) AS credit
FROM
	transactions t, entries e, ledgers l, accounts a, directions d
WHERE
	e.ledger_id = l.ledger_id AND
	l.account_id = a.account_id AND
	e.transaction_id = t.transaction_id AND
	e.direction = d.direction
GROUP BY
	t.transaction_id, e.entry_id
ORDER BY
	t.datestamp;


CREATE TEMPORARY VIEW list_transactions_values AS
SELECT
	date(t.datestamp, 'unixepoch') AS datestamp,
	pad(t.description, 13) AS description,
	pad(substr(a.title, 0, 12) || ' » ' || l.name, 25) AS "account,ledger",
	money(e.value, NULL, e.direction, 1) AS debit,
	money(e.value, NULL, e.direction, -1) AS credit
FROM
	transactions t, entries e, ledgers l, accounts a, directions d
WHERE
	e.ledger_id = l.ledger_id AND
	l.account_id = a.account_id AND
	e.transaction_id = t.transaction_id AND
	e.direction = d.direction
GROUP BY
	t.transaction_id, e.entry_id
ORDER BY
	t.datestamp;



COMMIT;

SELECT 'Amounts:';
SELECT * FROM list_transactions_amounts;

SELECT 'Values:';
SELECT * FROM list_transactions_values;

-- vim: filetype=text
