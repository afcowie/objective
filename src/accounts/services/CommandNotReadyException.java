/*
 * IdentifierAlreadyExistsException.java
 * 
 * See LICENCE file for usage and redistribution terms
 * Copyright (c) 2005 Operational Dynamics
 */
package accounts.services;

/**
 * Blown when a Command.execute() is called, but the Command isn't ready yet.
 * 
 * @author Andrew Cowie
 */
public class CommandNotReadyException extends Exception
{

	private static final long	serialVersionUID	= 1L;

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
