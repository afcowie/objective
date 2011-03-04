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

import objective.persistence.DomainObject;

/**
 * Fundamental grouping. An Account consists of metadata for organizing, and
 * one or more actual Ledgers representing actual T accounts.
 * 
 * @author Andrew Cowie
 */
public abstract class Account extends DomainObject
{
    protected String title = null;

    protected String code = null;

    protected Account(long rowid) {
        super(rowid);
    }

    public boolean isDebitPositive() {
        throw new UnsupportedOperationException();
    }

    public boolean isCreditPositive() {
        throw new UnsupportedOperationException();
    }

    /**
     * Get the title (name) of the account.
     */
    public String getTitle() {
        return title;
    }

    /**
     * Set the title (name) of the account.
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Get the account code
     * 
     * @return the account code, as a String.
     */
    public String getCode() {
        return code;
    }

    /**
     * Set the account's "numeric" code. TODO I've enforecd the basic
     * Australian account code form here, but this could and should be made
     * switchable or generic to support others use.
     * 
     * @param code
     *            Is expected to be numeric, but is stored as a String to
     *            support codes of the form "4-1200"
     */
    public void setCode(String code) throws IllegalArgumentException {
        if (code.length() != 6) {
            throw new IllegalArgumentException(
                    "account code needs to be 6 characters long, in the form 'x-yyyy'");
        }
        if (code.charAt(1) != '-') {
            throw new IllegalArgumentException(
                    "second character of the account code needs to be a '-' character");
        }
        this.code = code;
    }

    /*
     * Output ---------------------------------------------
     */

    public String getClassString() {
        return "Account";
    }

    /**
     * Yes, colour is spelled with a u... but in GTK it's spelled color. Fine.
     */
    public String getColor(boolean active) {
        if (this instanceof DebitPositiveAccount) {
            if (active) {
                return Debit.COLOR_ACTIVE;
            } else {
                return Debit.COLOR_NORMAL;
            }
        } else if (this instanceof CreditPositiveAccount) {
            if (active) {
                return Credit.COLOR_ACTIVE;
            } else {
                return Credit.COLOR_NORMAL;
            }
        } else {
            return "";
        }
    }
}
