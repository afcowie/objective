PRAGMA foreign_keys = ON;

CREATE VIEW list_accounts AS
SELECT
	a.name,
	l.name,
	CASE
	WHEN b.amount = 0 THEN
		'0.00'
	WHEN b.amount % 10 = 0 THEN
		CAST (b.amount / 100.0 AS TEXT) || '0'
	ELSE
		CAST (b.amount / 100.0 AS TEXT)
	END AS amount,
	d.code
FROM
	accounts a, ledgers l, balances b, directions d
WHERE
	l.account_id = a.account_id AND
	l.ledger_id = b.ledger_id AND
	l.direction = d.direction
GROUP BY
	l.ledger_id;

--

CREATE VIEW list_transactions AS
SELECT
	date(t.datestamp, 'unixepoch') AS datestamp,
	t.description,
	a.name AS account,
	l.name AS ledger,
	CASE
	WHEN e.amount = 0 THEN
		'0.00'
	WHEN e.amount % 10 = 0 THEN
		CAST (e.amount / 100.0 AS TEXT) || '0'
	ELSE
		CAST (e.amount / 100.0 AS TEXT)
	END AS amount,
	e.currency,
	d.code AS type
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

-- vim: filetype=text
