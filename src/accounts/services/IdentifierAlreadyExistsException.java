/*
 * IdentifierAlreadyExistsException.java
 * 
 * See LICENCE file for usage and redistribution terms
 * Copyright (c) 2005 Operational Dynamics
 */
package accounts.services;

/**
 * Blown if, when trying to add an Account or Ledger, the title is already in
 * use.
 * 
 * @author Andrew Cowie
 */
public class IdentifierAlreadyExistsException extends Exception
{

	private static final long	serialVersionUID	= 1L;

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
