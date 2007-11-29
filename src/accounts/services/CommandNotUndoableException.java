/*
 * CommandNotUndoableException.java
 * 
 * See LICENCE file for usage and redistribution terms
 * Copyright (c) 2005 Operational Dynamics
 */
package accounts.services;

/**
 * Thrown when a Command is asked to Undo but it is not in a valid state to do
 * so, or undo is not implemented or implementable for this Command
 * [subclass].
 * 
 * @author Andrew Cowie
 */
public class CommandNotUndoableException extends Exception
{
    private static final long serialVersionUID = 1L;

    public CommandNotUndoableException() {
        super();
    }

    public CommandNotUndoableException(String message) {
        super(message);
    }

    public CommandNotUndoableException(Throwable throwable) {
        super(throwable);
    }

    public CommandNotUndoableException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
