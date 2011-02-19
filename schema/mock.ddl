PRAGMA foreign_keys = ON;
BEGIN;

INSERT INTO types VALUES (1, 'accounts.domain.BankAccount');
INSERT INTO types VALUES (2, 'accounts.domain.AccountsRecivableAccount');
INSERT INTO types VALUES (3, 'accounts.domain.AccountsPayableAccount');
INSERT INTO types VALUES (4, 'accounts.domain.ProfessionalRevenueAccount');
INSERT INTO types VALUES (5, 'accounts.domain.CurrencyGainLossAccount');
INSERT INTO types VALUES (6, 'accounts.domain.ExpenseAccount');

INSERT INTO accounts VALUES (1, 1, 'ANZ', 1);
INSERT INTO ledgers VALUES (1, 1, 'Current Account', 1);

INSERT INTO accounts VALUES (2, 2, 'Trade Debitors', 1);
INSERT INTO ledgers VALUES (2, 2 ,'ACME Inc', 1);

INSERT INTO accounts VALUES (3, 3, 'Trade Creditors', -1);
INSERT INTO ledgers VALUES (3, 3, 'Internode', -1);

INSERT INTO accounts VALUES (4, 4, 'Procedures', -1);
INSERT INTO ledgers VALUES (4, 4, 'Fees', -1);
INSERT INTO ledgers VALUES (5, 4, 'Expense Reimbursement', -1);

INSERT INTO accounts VALUES (5, 6, 'Travel Expenses', 1);
INSERT INTO ledgers VALUES (6, 5, 'Ground', 1);
INSERT INTO ledgers VALUES (7, 5, 'Flights', 1);
INSERT INTO ledgers VALUES (8, 5, 'Meals', 1);

INSERT INTO accounts VALUES (6, 5, 'Currency Gain/Loss', -1);
INSERT INTO ledgers VALUES (9, 6, '', -1);


INSERT INTO accounts VALUES (7, 6, 'Communications Expenses', 1);
INSERT INTO ledgers VALUES (10, 5, 'Telephone', 1);

--

INSERT INTO transactions VALUES (1, 1062164087, 'Automation', '1033');
INSERT INTO entries VALUES (NULL, 1, 2, 2250000, 'USD', 1.503, 1);
INSERT INTO entries VALUES (NULL, 1, 4, 3381750, 'AUD', 1, -1);

--

INSERT INTO transactions VALUES (2, 1098170000, 'Payment', '1033');
INSERT INTO entries VALUES (NULL, 2, 1, 3573000, 'AUD', 1, 1);
INSERT INTO entries VALUES (NULL, 2, 2, 2250000, 'USD', 1.588, -1);
--INSERT INTO entries VALUES (NULL, 2, 9, 191250, 'AUD', 1, -1);

--

INSERT INTO transactions VALUES (3, 1101040600, 'Telstra Bill', NULL);
INSERT INTO entries VALUES (NULL, 3, 1, 4267, 'AUD', 1, -1);
INSERT INTO entries VALUES (NULL, 3, 10, 4267, 'AUD', 1, 1);

--

INSERT INTO transactions VALUES (4, 1063102035, 'Flight to SFO', NULL);
INSERT INTO entries VALUES (NULL, 4, 7, 329999, 'AUD', 1, 1);
INSERT INTO entries VALUES (NULL, 4, 1, 329999, 'AUD', 1, -1);

COMMIT;
-- vim: filetype=text
