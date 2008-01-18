/*
 * DummyInts.java
 * 
 * See LICENCE file for usage and redistribution terms
 * Copyright (c) 2005-2006 Operational Dynamics
 */
package generic.persistence;

/**
 * Something silly but storable to use when unit testing persistence.
 * 
 * @author Andrew Cowie
 */
public class DummyInts
{
    int num;

    public DummyInts(int i) {
        this.num = i;
    }

    public int getNum() {
        return num;
    }

    public void setNum(int num) {
        this.num = num;
    }

    public String toString() {
        return Integer.toString(num);
    }
}
