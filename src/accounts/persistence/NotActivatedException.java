/*
 * NotActivatedException.java
 * 
 * See LICENCE file for usage and redistribution terms
 * Copyright (c) 2005 Operational Dynamics
 */
package accounts.persistence;

/**
 * A failure of the persistence mechanism to reach sufficient activation depth.
 * Not ever supposed to happen, of course, but some classes have markers which
 * make it obvious when we have breached max activation depth.
 * 
 * @see accounts.domain.Datestamp for an example.
 * @author Andrew Cowie
 */
public class NotActivatedException extends RuntimeException
{
	private static final long	serialVersionUID	= 1L;

	public NotActivatedException() {
		super();
	}

	public NotActivatedException(String msg) {
		super(msg);
	}
}
