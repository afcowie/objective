/*
 * Selector.java
 * 
 * See LICENCE file for usage and redistribution terms
 * Copyright (c) 2006 Operational Dynamics
 */
package generic.persistence;

import com.db4o.query.Predicate;

/**
 * Wrap the db4o Native Query mechanism. Mostly because that class is really
 * messy, and partly so that services classes do not have to do db4o specific
 * imports. Originally named this "NativePredicate", but given that an anonymous
 * instance of this is the argument to {@link DataClient.nativeQuery()}, that
 * seemed overly verbose. "Selector" seems a good alternative.
 * <p>
 * Concrete anonymous classes must implement a method `boolean match()` with a
 * single argument: a typed parameter all instances of which will be iterated
 * over, fed to match(), and included in query result if true is returned.
 * 
 * @author Andrew Cowie
 * @see generic.persistence.DataClient#nativeQuery(Selector)
 */
public abstract class Selector extends Predicate
{
	/*
	 * boolean match(Type t);
	 */
}