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
 * contacted through http://research.operationaldynamics.com/projects/objective/.
 */
package accounts.domain;

/**
 * Marks an account which has multiple Ledgers in it. In a sense this is
 * redundent, as that is the actual nature of the Account class. However, this
 * is used as a marker to allow appopriate UI generation, and becuase there's
 * no way to turn off an inhereted interface. TODO I may not actually use
 * this.
 * 
 * @author Andrew Cowie
 */
public interface MultipleLedger
{
    // TODO: maybe, instead of the addEntry() that SimpleLedger provides, we
    // use
    // something like addPrimaryEntry or addDepreciationEntry on a case by
    // case
    // basis.
}
