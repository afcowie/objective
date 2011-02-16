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
package accounts.persistence;

/**
 * Blown when, for example, if trying to add an Account or Ledger, the title
 * is already in use.
 * 
 * @author Andrew Cowie
 */
public class IdentifierAlreadyExistsException extends RuntimeException
{

    private static final long serialVersionUID = 2L;

    public IdentifierAlreadyExistsException() {
        super();
    }

    public IdentifierAlreadyExistsException(String message) {
        super(message);
    }

    public IdentifierAlreadyExistsException(Throwable throwable) {
        super(throwable);
    }

    public IdentifierAlreadyExistsException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
