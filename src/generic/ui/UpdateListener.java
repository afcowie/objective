/*
 * UpdateListener.java
 * 
 * See LICENCE file for usage and redistribution terms
 * Copyright (c) 2005-2006 Operational Dynamics
 */
package generic.ui;

/**
 * A listener interace allowing applications to recieve a callback when some
 * major domain element they are displaying has changed (ie, as a result of user
 * action). The argument to the interface's method, a long, is the database ID
 * of the Object which has changed.
 * <p>
 * Note that this is <i>not</i> {@link accounts.ui.ChangeListener}, which is
 * there for inter-Widget communication.
 * 
 * @author Andrew Cowie
 */
public interface UpdateListener
{
	public void redisplayObject(long id);
}
