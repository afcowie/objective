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
package accounts.domain;

/**
 * Marks an account which only has one Ledger in it. In addition to a few
 * convenience methods (to be coded in implementing classes) for accessing the
 * single Ledger, this is a marker interface to allow appopriate UI
 * generation.
 * 
 * @author Andrew Cowie
 */
public interface SingleLedger
{
    /**
     * Add an entry to the single Ledger in this Account.
     */
    public void addEntry(Entry entry);

    /**
     * Get the [single] ledger associated with this account.
     */
    public Ledger getLedger();

    /**
     * Set the [sole] ledger associated with this account.
     */
    public void setLedger(Ledger ledger);
}
