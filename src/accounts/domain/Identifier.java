/*
 * Identifier.java
 * 
 * See LICENCE file for usage and redistribution terms
 * Copyright (c) 2006 Operational Dynamics
 */
package accounts.domain;

import generic.domain.DomainObject;
import generic.domain.Leaf;

/**
 * A class representing "constants" in the database. The archtypical cases for
 * this are taxation codes - the identifier used in the database needs to
 * correspond to a handle as thought of by users. This class makes that simple
 * translation.
 * <p>
 * It's assumed that you'll subclass this to your particular domain.
 * 
 * @author Andrew Cowie
 */
public class Identifier extends DomainObject implements Comparable, Leaf
{
    /**
     * Human readable (proper case, spaces) version of the Identifier
     * constant. Default null value gives an unactivated defence.
     */
    private String name = null;

    /**
     * A numerical index for debugging convenience only.
     */
    private int index = 0;

    /**
     * Constructs a new constant
     * 
     * @param name
     *            String The human readable string that goes with this
     *            identifier.
     */
    public Identifier(String name) {
        setName(name);
    }

    /**
     * Construct a new constant with a name and number.
     * 
     * @param name
     *            String The human readable string that goes with this
     *            identifier.
     * @param index
     *            A number to help select this Identifier. Set this as 0 if
     *            you're required to provide it and don't actually care about
     *            it.
     */
    public Identifier(String name, int index) {
        setName(name);
        setIndex(index);
    }

    protected Identifier() {
    // for searches
    }

    public final String getName() {
        return name;
    }

    /**
     * Change the plain text name associated with this identifier.
     */
    public void setName(String name) {
        if ((name == null) || name.equals("")) {
            throw new IllegalArgumentException("Can't use null or blank as the identifier name");
        }
        this.name = name;
    }

    public int getIndex() {
        return index;
    }

    /**
     * Set an index to help pick out this Identifier. This is <b>not</b>
     * guarunteed to be unique across Identifier space; nor is it enforced as
     * unique across the space of a given subclass.
     * 
     * @param index
     *            set as 0 if you're required to provide it and don't actually
     *            care about it.
     */
    public void setIndex(int index) {
        this.index = index;
    }

    public String toString() {
        if (index == 0) {
            return name;
        } else {
            return name + " (" + index + ")";
        }
    }

    /**
     * Override this in any subclasses.
     */
    public String getClassString() {
        return "Identifier";
    }

    /**
     * Implement Comparable. Note that if a subclass of Identifier adds any
     * new fields, then it needs to override this compareTo() [presumably
     * calling super.compareTo() as appropriate]
     */
    public int compareTo(Object x) {
        if (x == null) {
            throw new NullPointerException("Can't compareTo() against null");
        }
        if (!(x instanceof Identifier)) {
            throw new IllegalArgumentException("Can only compare Identifier objects");
        }
        Identifier i = (Identifier) x;

        int nameCmp = this.name.compareTo(i.name);
        if (nameCmp != 0) {
            return nameCmp;
        } else {
            if (this.index > i.index) {
                return +1;
            } else if (this.index < i.index) {
                return -1;
            } else {
                return 0;
            }
        }
    }
}
