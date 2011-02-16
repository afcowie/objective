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
package generic.persistence;

/**
 * A failure of the persistence mechanism to reach sufficient activation
 * depth. Not ever supposed to happen, of course, but some classes have
 * markers which make it obvious when we have breached max activation depth.
 * 
 * @see accounts.domain.Datestamp for an example.
 * @author Andrew Cowie
 */
public class NotActivatedException extends RuntimeException
{
    private static final long serialVersionUID = 1L;

    /**
     * Indicate a failure of the persistence mechanism to reach sufficient
     * activation depth.
     */
    public NotActivatedException() {
        super();
    }

    /**
     * Indicate a failure of the persistence mechanism to reach sufficient
     * activation depth, with a message explaining why.
     */
    public NotActivatedException(String msg) {
        super(msg);
    }
}
