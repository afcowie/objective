/*
 * Supplier.java
 * 
 * See LICENCE file for usage and redistribution terms
 * Copyright (c) 2006 Operational Dynamics
 */
package accounts.domain;

/**
 * A company or person who supplies products or services to us.
 * 
 * @author Andrew Cowie
 */
public class Supplier extends Entity
{
    public Supplier() {

    }

    public Supplier(String name) {
        super(name);
    }
}
