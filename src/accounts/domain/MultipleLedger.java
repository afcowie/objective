/*
 * MultipleLedger.java
 * 
 * See LICENCE file for usage and redistribution terms
 * Copyright (c) 2005 Operational Dynamics
 */
package accounts.domain;

/**
 * Marks an account which has multiple Ledgers in it. In a sense this is
 * redundent, as that is the actual nature of the Account class. However, this
 * is used as a marker to allow appopriate UI generation, and becuase there's no
 * way to turn off an inhereted interface. TODO I may not actually use this.
 * 
 * @author Andrew Cowie
 */
public interface MultipleLedger
{
	// TODO: maybe, instead of the addEntry() that SimpleLedger provides, we use
	// something like addPrimaryEntry or addDepreciationEntry on a case by case
	// basis.
}
