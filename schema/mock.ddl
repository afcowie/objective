PRAGMA foreign_keys = ON;
BEGIN;

INSERT INTO types VALUES (1, 'objective.domain.BankAccount');
INSERT INTO types VALUES (2, 'objective.domain.AccountsReceivableAccount');
INSERT INTO types VALUES (3, 'objective.domain.AccountsPayableAccount');
INSERT INTO types VALUES (4, 'objective.domain.ReimbursableExpensesPayableAccount');
INSERT INTO types VALUES (5, 'objective.domain.ProfessionalRevenueAccount');
INSERT INTO types VALUES (6, 'objective.domain.CurrencyGainLossAccount');
INSERT INTO types VALUES (7, 'objective.domain.GenericExpenseAccount');
INSERT INTO types VALUES (8, 'objective.domain.GenericTransaction');
INSERT INTO types VALUES (9, 'objective.domain.InvoiceTransaction');
INSERT INTO types VALUES (10, 'objective.domain.PaymentTransaction');

INSERT INTO accounts VALUES (1, 1, 'ANZ', 1);
INSERT INTO ledgers VALUES (1, 1, 'Current Account', 'AUD', 1);

INSERT INTO accounts VALUES (2, 2, 'Trade Debitors', 1);
INSERT INTO ledgers VALUES (2, 2 ,'ACME Inc', 'USD', 1);

INSERT INTO accounts VALUES (3, 3, 'Trade Creditors', -1);
INSERT INTO ledgers VALUES (3, 3, 'Internode', 'AUD', -1);

INSERT INTO accounts VALUES (4, 4, 'Expenses Payable', -1);
INSERT INTO ledgers VALUES (11, 4, 'Andrew Cowie', 'AUD', -1);

INSERT INTO accounts VALUES (5, 5, 'Procedures', -1);
INSERT INTO ledgers VALUES (4, 5, 'Fees', NULL, -1);
INSERT INTO ledgers VALUES (5, 5, 'Expense Reimbursement', NULL, -1);

INSERT INTO accounts VALUES (6, 7, 'Travel Expenses', 1);
INSERT INTO ledgers VALUES (6, 6, 'Ground', NULL, 1);
INSERT INTO ledgers VALUES (7, 6, 'Flights', NULL, 1);
INSERT INTO ledgers VALUES (8, 6, 'Meals', NULL, 1);

INSERT INTO accounts VALUES (7, 7, 'Communications Expenses', 1);
INSERT INTO ledgers VALUES (10, 7, 'Telephone', NULL, 1);

--

INSERT INTO transactions VALUES (1, 9, 1062164087, 'Automation', '1033');
INSERT INTO entries VALUES (NULL, 1, 2, 2250000, 'USD', 3381750, 1);
INSERT INTO entries VALUES (NULL, 1, 4, 0, NULL, 3381750, -1);

--

INSERT INTO transactions VALUES (2, 10, 1098170000, 'Payment', '1033');
INSERT INTO entries VALUES (NULL, 2, 1, 3573000, 'AUD', 3573000, 1);
INSERT INTO entries VALUES (NULL, 2, 2, 2250000, 'USD', 3573000, -1);
INSERT INTO entries VALUES (NULL, 2, 2, 0, NULL, 191250, 1);
INSERT INTO entries VALUES (NULL, 2, 4, 0, NULL, 191250, -1);

--

INSERT INTO transactions VALUES (3, 8, 1101040600, 'Phone Card', NULL);
INSERT INTO entries VALUES (NULL, 3, 11, 4000, 'CAD', 4267, -1);
INSERT INTO entries VALUES (NULL, 3, 10, 0, NULL, 4267, 1);

--

INSERT INTO transactions VALUES (4, 8, 1063102035, 'Flight to SFO', NULL);
INSERT INTO entries VALUES (NULL, 4, 7, 0, NULL, 329999, 1);
INSERT INTO entries VALUES (NULL, 4, 1, 329999, 'AUD', 329999, -1);

COMMIT;
-- vim: filetype=text
