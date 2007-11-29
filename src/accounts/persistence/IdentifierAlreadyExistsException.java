/*
 * IdentifierAlreadyExistsException.java
 * 
 * See LICENCE file for usage and redistribution terms
 * Copyright (c) 2005-2006 Operational Dynamics
 */
package accounts.persistence;

/**
 * Blown when, for example, if trying to add an Account or Ledger, the title
 * is already in use.
 * 
 * @author Andrew Cowie
 */
public class IdentifierAlreadyExistsException extends RuntimeException
{

    private static final long serialVersionUID = 2L;

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
