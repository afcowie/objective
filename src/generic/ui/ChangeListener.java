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
package generic.ui;

/**
 * Report a change in one of our mutator Widgets that occured as a result of
 * user action. The premise of this Listener interface is that its
 * userChangedData() method is fired at the end of a Widget's regular event
 * Listeners when they handle <code>CHANGED</code> signals but only as a
 * result of direct user data changes, which we take to be when the Widget in
 * question
 * <ol>
 * <li>has focus
 * <li>isn't blank/null
 * </ol>
 * There is no necessity for Widgets using this Listener to maintain a
 * registry of multiple sets of them. One is sufficient, especially as the
 * whole point of using this Listener is to work around the problem of
 * multiple invokations that happen when reacting to GTK events signals.
 * 
 * @see accounts.ui.AmountEntry which was the first use of this class.
 * @author Andrew Cowie
 */
public interface ChangeListener
{
    /**
     * There is no parameter to this method, strongly typed or otherwise. The
     * assumption is that any Window using this callback mechansim is going to
     * have an instance field pointing to the data anyway.
     */
    public abstract void userChangedData();
}
