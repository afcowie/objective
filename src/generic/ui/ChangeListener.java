/*
 * ChangeListener.java
 * 
 * See LICENCE file for usage and redistribution terms
 * Copyright (c) 2006 Operational Dynamics
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
