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
package accounts.services;

/**
 * Blown when a Command.execute() is called, but the Command isn't ready yet.
 * 
 * @author Andrew Cowie
 */
public class CommandNotReadyException extends Exception
{

    private static final long serialVersionUID = 1L;

    public CommandNotReadyException() {
        super();
    }

    public CommandNotReadyException(String message) {
        super(message);
    }

    public CommandNotReadyException(Throwable throwable) {
        super(throwable);
    }

    public CommandNotReadyException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
