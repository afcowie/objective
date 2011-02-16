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
package generic.ui;

/**
 * A listener interace allowing applications to recieve a callback when some
 * major domain element they are displaying has changed (ie, as a result of
 * user action). The argument to the interface's method, a long, is the
 * database ID of the Object which has changed.
 * <p>
 * Note that this is <i>not</i> {@link generic.ui.ChangeListener}, which is
 * there for inter-Widget communication.
 * 
 * @author Andrew Cowie
 */
public interface UpdateListener
{
    /**
     * @param id
     *            The database ID of the object that is has been signalled as
     *            updated. You <i>really</i> want to call
     *            {@link generic.persistence.DataClient#reload(Object)} once
     *            you look it up.
     */
    public void redisplayObject(long id);
}
