.headers ON
.mode column

BEGIN;

CREATE TEMPORARY VIEW debits AS
SELECT
	sum(e.value) AS value
FROM
	entries e
WHERE
	e.direction = 1;

	
CREATE TEMPORARY VIEW credits AS
SELECT
	sum(e.value) AS value
FROM
	entries e
WHERE
	e.direction = -1;

--

SELECT
	money(d.value) AS Debits,
	money(c.value) AS Credits,
	money(d.value - c.value) AS Error
FROM
	debits d, credits c;

ROLLBACK;

.headers OFF
.mode list

-- vim: filetype=text
