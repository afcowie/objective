/*
 * ObjectiveAccounts, accounting for small professional services firms.
 *
 * Copyright © 2006-2011 Operational Dynamics Consulting, Pty Ltd
 *
 * The code in this file, and the program it is a part of, is made available
 * to you by its authors as open source software: you can redistribute it
 * and/or modify it under the terms of the GNU General Public License version
 * 2 ("GPL") as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GPL for more details.
 *
 * You should have received a copy of the GPL along with this program. If not,
 * see http://www.gnu.org/licenses/. The authors of this program may be
 * contacted through http://research.operationaldynamics.com/projects/objective/.
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
