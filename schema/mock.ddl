PRAGMA foreign_keys = ON;
BEGIN;

--
-- Mock Accounts
--

INSERT INTO accounts VALUES (1, 10, 'ANZ', 1);
INSERT INTO ledgers VALUES (1, 1, 'Current Account', 'AUD', 1);

INSERT INTO accounts VALUES (2, 12, 'Trade Debitors', 1);
INSERT INTO ledgers VALUES (2, 2 ,'ACME Inc', 'USD', 1);

INSERT INTO accounts VALUES (3, 22, 'Trade Creditors', -1);
INSERT INTO ledgers VALUES (3, 3, 'Internode', 'AUD', -1);

INSERT INTO accounts VALUES (4, 25, 'Expenses Payable', -1);
INSERT INTO ledgers VALUES (11, 4, 'Andrew Cowie', 'AUD', -1);
INSERT INTO ledgers VALUES (19, 4, 'Paul Drain', 'AUD', -1);

INSERT INTO accounts VALUES (5, 41, 'Procedures', -1);
INSERT INTO ledgers VALUES (4, 5, 'Fees', NULL, -1);
INSERT INTO ledgers VALUES (5, 5, 'Expense Reimbursement', NULL, -1);

INSERT INTO accounts VALUES (6, 61, 'Travel Expenses', 1);
INSERT INTO ledgers VALUES (6, 6, 'Ground', NULL, 1);
INSERT INTO ledgers VALUES (7, 6, 'Flights', NULL, 1);
INSERT INTO ledgers VALUES (8, 6, 'Meals', NULL, 1);

INSERT INTO accounts VALUES (7, 61, 'Communications Expenses', 1);
INSERT INTO ledgers VALUES (10, 7, 'Calling Card', NULL, 1);

INSERT INTO accounts VALUES (8, 61, 'Fees', 1);
INSERT INTO ledgers VALUES (12, 8, 'Foreign Exchange', NULL, 1);

INSERT INTO accounts VALUES (9, 23, 'GST', -1);
INSERT INTO ledgers VALUES (13, 9, 'Collected', 'AUD', -1);
INSERT INTO ledgers VALUES (14, 9, 'Paid', 'AUD', 1);

INSERT INTO accounts VALUES (10, 13, 'Furniture', 1);
INSERT INTO ledgers VALUES (15, 10, 'At Cost', NULL, 1);
INSERT INTO ledgers VALUES (16, 10, 'Accumulated Depreciation', NULL, -1);

INSERT INTO accounts VALUES (11, 10, 'Petty Cash', 1);
INSERT INTO ledgers VALUES (17, 11, 'Manly Office', 'AUD', 1);

INSERT INTO accounts VALUES (12, 31, 'Owner''s Equity', -1);
INSERT INTO ledgers VALUES (18, 12, 'Andrew Cowie', 'AUD', -1);

--
-- Mock Workers
--

INSERT INTO workers VALUES (1, 1, 'Andrew Cowie', 11);
INSERT INTO workers VALUES (2, 2, 'Paul Drain', 19);

--
-- Mock Transactions
--

INSERT INTO transactions VALUES (1, -9, 1062164087, 'Automation Project', '1033');
INSERT INTO entries VALUES (NULL, 1, 2, 2250000, 'USD', 3381750, 1);
INSERT INTO entries VALUES (NULL, 1, 4, 2250000, 'USD', 3381750, -1);


INSERT INTO transactions VALUES (2, -10, 1098170000, 'Payment', '1033');
INSERT INTO entries VALUES (NULL, 2, 1, 3573000, 'AUD', 3573000, 1);
INSERT INTO entries VALUES (NULL, 2, 2, 2250000, 'USD', 3381750, -1);
INSERT INTO entries VALUES (NULL, 2, 4, 191250, 'AUD', 191250, -1);


INSERT INTO transactions VALUES (3, -11, 1101040600, 'Phone Card', NULL);
INSERT INTO entries VALUES (NULL, 3, 10, 4000, 'CAD', 4267, 1);
INSERT INTO entries VALUES (NULL, 3, 11, 4267, 'AUD', 4267, -1);


INSERT INTO transactions VALUES (4, -8, 1063102035, 'Flight to SFO', NULL);
INSERT INTO entries VALUES (NULL, 4, 7, 329999, 'AUD', 329999, 1);
INSERT INTO entries VALUES (NULL, 4, 1, 329999, 'AUD', 329999, -1);


INSERT INTO transactions VALUES (5, -8, 1104969600, 'Chair for office', NULL);
INSERT INTO entries VALUES (NULL, 5, 1, 72500, 'AUD', 72500, -1);
INSERT INTO entries VALUES (NULL, 5, 15, 65910, 'AUD', 65910, 1);
INSERT INTO entries VALUES (NULL, 5, 14, 6590, 'AUD', 6590, 1);


INSERT INTO transactions VALUES (6, -8, 1040256000, 'Initial Capitalization', NULL);
INSERT INTO entries VALUES (NULL, 6, 17, 100, 'AUD', 100, 1);
INSERT INTO entries VALUES (NULL, 6, 18, 100, 'AUD', 100, -1);


COMMIT;
-- vim: filetype=text
