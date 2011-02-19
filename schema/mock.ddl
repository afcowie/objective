PRAGMA foreign_keys = ON;
BEGIN;

INSERT INTO types VALUES (1, 'accounts.domain.BankAccount');
INSERT INTO types VALUES (2, 'accounts.domain.AccountsRecivableAccount');
INSERT INTO types VALUES (3, 'accounts.domain.AccountsPayableAccount');
INSERT INTO types VALUES (4, 'accounts.domain.ProfessionalRevenueAccount');
INSERT INTO types VALUES (5, 'accounts.domain.ExpenseAccount');

INSERT INTO accounts VALUES (1, 1, 'ANZ', 1);
INSERT INTO ledgers VALUES (1, 1, 'Current Account', 1);

INSERT INTO accounts VALUES (2, 2, 'Trade Debitors', 1);
INSERT INTO ledgers VALUES (2, 2 ,'ACME Inc', 1);

INSERT INTO accounts VALUES (3, 3, 'Trade Creditors', -1);
INSERT INTO ledgers VALUES (3, 3, 'Internode', -1);

INSERT INTO accounts VALUES (4, 4, 'Procedures', -1);
INSERT INTO ledgers VALUES (4, 4, 'Fees', -1);
INSERT INTO ledgers VALUES (5, 4, 'Expense Reimbursement', -1);

INSERT INTO accounts VALUES (5, 5, 'Travel Expenses', 1);
INSERT INTO ledgers VALUES (6, 5, 'Ground', 1);
INSERT INTO ledgers VALUES (7, 5, 'Flights', 1);
INSERT INTO ledgers VALUES (8, 5, 'Meals', 1);

COMMIT;

--

BEGIN;
INSERT INTO transactions VALUES (1, 1062164087, 'Automation', '1033');
INSERT INTO entries VALUES (1, 1, 2, 22500, 'USD', 1);
INSERT INTO entries VALUES (2, 1, 4, 22500, 'USD', -1);
COMMIT;

--

BEGIN;
INSERT INTO transactions VALUES (2, 1098170000, 'Payment 1/2', '1033');
INSERT INTO entries VALUES (3, 2, 1, 15500, 'USD', 1);
INSERT INTO entries VALUES (4, 2, 2, 15500, 'USD', -1);
COMMIT;

--

BEGIN;
INSERT INTO transactions VALUES (3, 1101040600, 'Payment 2/2', '1033');
INSERT INTO entries VALUES (5, 3, 1, 7000, 'USD', 1);
INSERT INTO entries VALUES (6, 3, 2, 7000, 'USD', -1);
COMMIT;

--

BEGIN;
INSERT INTO transactions VALUES (4, 1063102035, 'Flight to SFO', NULL);
INSERT INTO entries VALUES (7, 4, 7, 3299, 'AUD', 1);
INSERT INTO entries VALUES (8, 4, 1, 3299, 'AUD', -1);
COMMIT;

