/*
 * NotFoundException.java
 * 
 * See LICENCE file for usage and redistribution terms
 * Copyright (c) 2006 Operational Dynamics
 */
package accounts.services;

/**
 * A finder's query ran but did not result in any matches.
 * 
 * @author Andrew Cowie
 */
public class NotFoundException extends Exception
{
    private static final long serialVersionUID = 1L;

    public NotFoundException() {
        super();
    }

    public NotFoundException(String message) {
        super(message);
    }
}
