/*
 * Debit.java
 * 
 * See LICENCE file for usage and redistribution terms
 * Copyright (c) 2005-2006 Operational Dynamics
 */
package accounts.domain;

/**
 * A debit positive entry in a ledger.
 * 
 * @author Andrew Cowie
 */
public class Debit extends Entry
{
    public static final String COLOR_NORMAL = "#173faf";

    public static final String COLOR_ACTIVE = "#32fdff";

    public Debit() {
        super();
        /*
         * an actual null Entry...
         */
    }

    public Debit(Amount value, Ledger parent) {
        super(value, parent);
    }
}
