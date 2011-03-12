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
package objective.domain;

/**
 * A generic transaction in the "general ledger" as traditional accounting
 * systems would call it. Simply consists of a balanced set of Entries, and
 * does not link to any special user interface.
 * 
 * <p>
 * This would have been called GeneralLedgerTransaction but ther was potential
 * for naming confustion as we have modelled the actual ledger(s) in any given
 * account with the Ledger class. This is most assuredly an accounting
 * Transaction.
 * 
 * @author Andrew Cowie
 */
public class GenericTransaction extends Transaction
{
    public GenericTransaction(long rowid) {
        super(rowid);
    }

    public String getClassString() {
        return "Generic Transaction";
    }

    public long getType() {
        return -8;
    }
}
