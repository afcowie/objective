/*
 * ObjectiveAccounts, accounting for small professional services firms.
 *
 * Copyright © 2005-2011 Operational Dynamics Consulting, Pty Ltd
 *
 * The code in this file, and the program it is a part of, is made available
 * to you by its authors as open source software: you can redistribute it
 * and/or modify it under the terms of the GNU General Public License version
 * 2 ("GPL") as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GPL for more details.
 *
 * You should have received a copy of the GPL along with this program. If not,
 * see http://www.gnu.org/licenses/. The authors of this program may be
 * contacted via http://research.operationaldynamics.com/projects/objective/.
 */
package objective.services;

import generic.util.DebugException;

import java.util.Comparator;
import java.util.TreeMap;

import objective.domain.Account;
import objective.domain.AccountsPayableAccount;
import objective.domain.AccountsReceivableAccount;
import objective.domain.AssetAccount;
import objective.domain.BankAccount;
import objective.domain.CardAccount;
import objective.domain.CashAccount;
import objective.domain.CreditPositiveAccount;
import objective.domain.CurrencyGainLossAccount;
import objective.domain.DebitPositiveAccount;
import objective.domain.DepreciatingAssetAccount;
import objective.domain.EquityAccount;
import objective.domain.ExpenseAccount;
import objective.domain.GenericExpenseAccount;
import objective.domain.LiabilityAccount;
import objective.domain.LoanPayableAccount;
import objective.domain.OwnersEquityAccount;
import objective.domain.PayrollTaxPayableAccount;
import objective.domain.ProfessionalRevenueAccount;
import objective.domain.ReimbursableExpensesPayableAccount;
import objective.domain.RevenueAccount;
import objective.domain.SalesTaxPayableAccount;

/**
 * Sort Accounts to match our version of accounting order. Accounts, as
 * expressed in this program, have an ordering which is somewhat contrived;
 * since we deprecate acccount codes we use this Comparator to say how
 * accounts should be listed.
 * <p>
 * Although Account, DebitPositiveAccount and CreditPositiveAccount are
 * included in the ordering table - you really ought only to be comparing the
 * "concrete" subclasses of Account.
 */
/*
 * Move to Account? No, because it references so many deep classes.
 */
public class AccountComparator implements Comparator<Account>
{
    private final static TreeMap<String, Integer> lookupTable;

    static {
        final Class<?>[] ordering = {
            Account.class,
            DebitPositiveAccount.class,
            CreditPositiveAccount.class,

            AssetAccount.class,
            BankAccount.class,
            CashAccount.class,
            AccountsReceivableAccount.class,
            DepreciatingAssetAccount.class,

            LiabilityAccount.class,
            AccountsPayableAccount.class,
            CardAccount.class,
            SalesTaxPayableAccount.class,
            PayrollTaxPayableAccount.class,
            ReimbursableExpensesPayableAccount.class,
            LoanPayableAccount.class,

            EquityAccount.class,
            OwnersEquityAccount.class,

            RevenueAccount.class,
            ProfessionalRevenueAccount.class,
            CurrencyGainLossAccount.class,

            ExpenseAccount.class,
            GenericExpenseAccount.class,
        };

        lookupTable = new TreeMap<String, Integer>();

        for (int i = 0; i < ordering.length; i++) {
            lookupTable.put(ordering[i].getName(), new Integer(i));
        }
    }

    private final int indexOf(Object obj) {
        final String className = obj.getClass().getName();

        final Integer index = lookupTable.get(className);

        if (index == null) {
            throw new DebugException("You are attempting to look up a " + obj.getClass().getName()
                    + " object for which we don't have an entry in " + this.getClass().getName()
                    + "'s Account lookupTable, so add it already!");
        }

        return index.intValue();
    }

    public int compare(final Account a1, final Account a2) {
        final int i1, i2, hash1, hash2;
        final String code1, code2, title1, title2;

        i1 = indexOf(a1);
        i2 = indexOf(a2);

        if (i1 < i2) {
            return -1;
        } else if (i1 > i2) {
            return +1;
        } else {
            /*
             * Same Account subclass, so look into Account's code and order by
             * it. Note that we generally aren't using this at the moment, so
             * you're expected to hit nulls, but this is supported for future
             * alternate ordering. TODO perhaps this should rise above the
             * result of the indexOf call?
             */
            code1 = a1.getCode();
            code2 = a2.getCode();

            int codeCmp;
            if ((code1 == null) || (code2 == null)) {
                // force it to ignore this comparison and move to the
                // next
                codeCmp = 0;
            } else {
                codeCmp = code1.compareTo(code2);
            }

            if (codeCmp != 0) {
                return codeCmp;
            } else {
                /*
                 * Same (or as expected and more likely, null) code, so look
                 * into Account's name and order by that.
                 */
                title1 = a1.getTitle();
                title2 = a2.getTitle();

                int titleCmp;
                if ((title1 == null) || (title2 == null)) {
                    // force it to ignore this comparison and move to the
                    // next
                    titleCmp = 0;
                } else {
                    titleCmp = title1.compareTo(title2);
                }

                if (titleCmp != 0) {
                    return titleCmp;
                } else {
                    /*
                     * At this point, go to Object's hashCode. Do not expect
                     * to get this deep in normal usage.
                     */
                    hash1 = a1.hashCode();
                    hash2 = a2.hashCode();

                    if (hash1 < hash2) {
                        return -1;
                    } else if (hash1 > hash2) {
                        return +1;
                    } else {
                        return 0;
                    }
                }
            }
        }
    }
}
