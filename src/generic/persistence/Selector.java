/*
 * Selector.java
 * 
 * See LICENCE file for usage and redistribution terms
 * Copyright (c) 2006 Operational Dynamics
 */
package generic.persistence;

import generic.domain.DomainObject;

import com.db4o.query.Predicate;

/**
 * Wrap the db4o Native Query mechanism. Mostly because that class is really
 * messy, and partly so that services classes do not have to do db4o specific
 * imports. Originally named this "NativePredicate", but given that an
 * anonymous instance of this is the argument to
 * {@link DataClient#nativeQuery(Selector)}, that seemed overly verbose.
 * "Selector" seems a good alternative.
 * <p>
 * Concrete anonymous classes must implement a method `boolean match()` with a
 * single argument: a typed parameter all instances of which will be iterated
 * over, fed to match(), and included in query result if true is returned.
 * 
 * @author Andrew Cowie
 * @see generic.persistence.DataClient#nativeQuery(Selector)
 */
public abstract class Selector<T> extends Predicate<T>
{
    protected DomainObject target = null;

    /**
     * Default.
     */
    public Selector() {
    // default
    }

    /**
     * Create a Selector with a query parameter.
     * 
     * @param target
     *            a DomainObject which will be set to protected field
     *            <code>target</code> which you can then use as an argument
     *            in a <code>match()</code> method when making a call to
     *            {@link DomainObject#congruent(Object) congruent()}.
     */
    public Selector(DomainObject target) {
        this.target = target;
    }
}
