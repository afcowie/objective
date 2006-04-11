/*
 * UpdateListener.java
 * 
 * See LICENCE file for usage and redistribution terms
 * Copyright (c) 2005-2006 Operational Dynamics
 */
package accounts.persistence;

/**
 * A listener interace allowing applications to recieve a callback when an
 * object they have UnitOfWork.registerInterest() is changed by another
 * UnitOfWork committing.
 * 
 * @author Andrew Cowie
 */
public interface UpdateListener
{
	public void changed(Object obj);
}
