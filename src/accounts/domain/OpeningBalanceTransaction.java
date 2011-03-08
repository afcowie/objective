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

import java.util.Set;

import objective.domain.Datestamp;
import objective.domain.EquityAccount;
import objective.domain.GenericTransaction;

/**
 * The transaction used to import a set of existing accounts. In essence it's
 * just a big general ledger entry, but a single GL transaction UI is really
 * terrible for entry of this data - rather, an aspect of an account is it's
 * openning balance, which is recorded here.
 * 
 * @author Andrew Cowie
 */
public class OpeningBalanceTransaction extends GenericTransaction
{
    private final static String LABEL = "Opening Balance";

    private transient EquityAccount historicalBalance = null;

    public OpeningBalanceTransaction() {
        super();
    }

    /**
     * Create a new Opening Balance Transaction. This constructor assumes you
     * have a Set of entries you wish to apply.
     * 
     * @param entries
     * @param date
     *            The date to use for the openning balance entries.
     */
    /*
     * TODO this should probably be related to Books.dateOfInception
     */
    public OpeningBalanceTransaction(Set entries, Datestamp date) {
        // super(LABEL, entries);
        super.setDate(date);
    }

}
