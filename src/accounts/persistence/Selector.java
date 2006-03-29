/*
 * Selector.java
 * 
 * See LICENCE file for usage and redistribution terms
 * Copyright (c) 2006 Operational Dynamics
 */
package accounts.persistence;

import com.db4o.query.Predicate;

/**
 * Wrap the db4o Native Query mechanism. Mostly because that class is really
 * messy, and partly so that services classes do not have to do db4o specific
 * imports. Originally named this "NativePredicate", but given that an anonymous
 * instance of this is the argument to {@link DataStore.nativeQuery()}, that
 * seemed overly verbose. "Selector" seems a good alternative.
 * 
 * @author Andrew Cowie
 */
public abstract class Selector extends Predicate
{
	public static final long	serialVersionUID	= 1L;
}
