PRAGMA foreign_keys = ON;

CREATE VIEW list_accounts AS
SELECT
	a.name,
	l.name,
	b.amount,
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
	date(t.datestamp, 'unixepoch'),
	t.description,
	a.name,
	l.name,
	e.amount, 
	d.code
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

