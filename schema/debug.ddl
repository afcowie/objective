PRAGMA foreign_keys = ON;
.load ./amount.so

BEGIN;


CREATE TEMPORARY VIEW list_accounts AS
SELECT
	pad(a.name, 37),
	money(sum(b.value), 'AUD', a.direction, 1) AS debit,
	money(sum(b.value), 'AUD', a.direction, -1) AS credit
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
	pad(a.name, 18),
	pad(l.name, 18),
	money(b.value, 'AUD', l.direction, 1) AS debit,
	money(b.value, 'AUD', l.direction, -1) AS credit
FROM
	accounts a, ledgers l, balances b, directions d
WHERE
	l.account_id = a.account_id AND
	l.ledger_id = b.ledger_id
GROUP BY
	l.ledger_id;


--

CREATE TEMPORARY VIEW list_transactions AS
SELECT
	date(t.datestamp, 'unixepoch') AS datestamp,
	pad(t.description, 12) AS description,
	pad(a.name, 12) AS account,
	pad(l.name, 12) AS ledger,
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

COMMIT;
-- vim: filetype=text
