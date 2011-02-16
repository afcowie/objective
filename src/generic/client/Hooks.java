/*
 * ObjectiveAccounts, accounting for small professional services firms.
 *
 * Copyright © 2006-2011 Operational Dynamics Consulting, Pty Ltd
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
package generic.client;

/**
 * A set of callbacks which can be registered with the {@link Master} global
 * UI entry point for action on specified Life Cycle conditions. Use as if it
 * were an interface but it is not complusary to override any of the
 * particular methods here; all the methods here are empty.
 * 
 * @author Andrew Cowie
 */
public abstract class Hooks
{
    /**
     * Essential emergency cleanup to attempt before the application thunders
     * in. At the end of the abort callback sequence, System.exit() will be
     * called.
     * 
     * @see Master#abort(String)
     */
    public void abort() {
    // no-op
    }

    /**
     * Actions to take on a gracefully shutdown of the application. GTK is
     * still active at this time; it is appropriate to close any open Windows
     * that you have reference to and to stop Timers, etc.
     * 
     * @see Master#shutdown()
     */
    public void shutdown() {
    // no-op
    }

}
