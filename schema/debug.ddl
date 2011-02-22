PRAGMA foreign_keys = ON;
.load ./amount.so

BEGIN;


CREATE TEMPORARY VIEW list_accounts AS
SELECT
	pad(a.name, 37),
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
	pad(a.name || ' » ' || l.name, 37),
	money(b.value, NULL, l.direction, 1) AS debit,
	money(b.value, NULL, l.direction, -1) AS credit
FROM
	accounts a, ledgers l, balances b
WHERE
	l.account_id = a.account_id AND
	l.ledger_id = b.ledger_id
GROUP BY
	l.ledger_id;

CREATE TEMPORARY VIEW list_amounts AS
SELECT
	pad(a.name || ' » ' || l.name, 37),
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


CREATE TEMPORARY VIEW debug_entries AS
SELECT
	entry_id,
	transaction_id,
	ledger_id,
	CASE
	WHEN currency NOTNULL
	THEN
		amount
	ELSE
		value
	END AS amount,
	currency,
	direction
FROM
	entries;


CREATE TEMPORARY VIEW list_transactions AS
SELECT
	date(t.datestamp, 'unixepoch') AS datestamp,
	pad(t.description, 13) AS description,
	pad(substr(a.name, 0, 12) || ' » ' || l.name, 25) AS "account,ledger",
	money(e.amount, e.currency, e.direction, 1) AS debit,
	money(e.amount, e.currency, e.direction, -1) AS credit
FROM
	transactions t, debug_entries e, ledgers l, accounts a, directions d
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
-- vim: filetype=text
