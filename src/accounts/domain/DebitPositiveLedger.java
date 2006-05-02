/*
 * DebitPositiveLedger.java
 * 
 * See LICENCE file for usage and redistribution terms
 * Copyright (c) 2005-2006 Operational Dynamics
 */
package accounts.domain;

/**
 * An accountign ledger which is debit positive, ie a ledger representing a bank
 * account or an expense.
 * 
 * @author Andrew Cowie
 */
public class DebitPositiveLedger extends Ledger
{
	public DebitPositiveLedger() {
		super();
	}

	public DebitPositiveLedger(String name) {
		super();
		super.setName(name);
	}

	/*
	 * Overrides Ledger's addToBalance(). balance from Ledger.
	 */
	protected void addToBalance(Entry entry) {
		if (entry == null) {
			throw new IllegalArgumentException("Can't add null Entry to a Ledger");
		}
		if (entry instanceof Debit) {
			balance.incrementBy(entry.getAmount());
		} else {
			balance.decrementBy(entry.getAmount());
		}
	}

	protected void subtractFromBalance(Entry entry) {
		if (entry == null) {
			throw new IllegalArgumentException("Can't subtract null Entry from a Ledger");
		}
		if (entry instanceof Debit) {
			balance.decrementBy(entry.getAmount());
		} else {
			balance.incrementBy(entry.getAmount());
		}
	}

	public String getClassString() {
		return "Debit Positive";
	}
}