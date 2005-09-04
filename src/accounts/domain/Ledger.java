/*
 * Ledger.java
 * 
 * See LICENCE file for usage and redistribution terms
 * Copyright (c) 2005 Operational Dynamics
 */
package accounts.domain;

import java.io.PrintWriter;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Base class for the ledgers within actual accounts.
 * 
 * @author Andrew Cowie
 */
public class Ledger
{
	/*
	 * Instance variables ---------------------------------
	 */
	private String				_name		= null;
	private Set					_entries	= null;

	/*
	 * Cached values --------------------------------------
	 */
	private transient Amount	_balance	= null;

	/*
	 * Constructors ---------------------------------------
	 */

	public Ledger() {
		/*
		 * The default empty constructor provides a null prototype, useful for
		 * searching. Otherwise, you use one of the subclasses.
		 */
	}

	/*
	 * Utility methods ------------------------------------
	 */

	/**
	 * Add an Entry to this Ledger.
	 * 
	 * @throws NullPointerException
	 *             if you try to add a null Entry
	 * @throws IllegalArgumentException
	 *             if you try to add an Entry which is neither Debit nor Credit
	 *             (both are concrete subclasses of Entry).
	 */
	public void addEntry(Entry entry) {
		/*
		 * validation
		 */
		if (entry == null) {
			throw new NullPointerException("attempted to add a null Entry!");
		}

		if (!((entry instanceof Debit) || (entry instanceof Credit))) {
			throw new IllegalArgumentException("attempted to add an Entry which is neither Debit nor Credit!");
		}
		/*
		 * setup
		 */
		if (_entries == null) {
			_entries = new LinkedHashSet();
		}
		if (_balance == null) {
			calculateBalance();
		}
		/*
		 * add
		 */
		_entries.add(entry);
		addToBalance(entry);
	}

	/**
	 * Read the list of entries and sum them to arrive at this account's current
	 * balance. Used to set an initial value for a Ledger's balance if not
	 * currently set.
	 */
	protected void calculateBalance() {
		_balance = new Amount("0");
		if (_entries == null) {
			return;
		}

		Iterator iter = _entries.iterator();
		while (iter.hasNext()) {
			Entry entry = (Entry) iter.next();
			addToBalance(entry);
		}
	}

	/**
	 * Add and Entry's Amount to to the Ledger's balance.
	 * 
	 * @param entry
	 *            The Entry whose amount we will add to the Ledger's balance. It
	 *            will be tested for Debit/Credit-ness and added accordingly.
	 */
	private void addToBalance(Entry entry) {
		if (this instanceof DebitPositiveLedger) {
			if (entry instanceof Debit) {
				_balance.incrementBy(entry.getAmount());
			} else {
				_balance.decrementBy(entry.getAmount());
			}
		} else if (this instanceof CreditPositiveLedger) {
			if (entry instanceof Credit) {
				_balance.incrementBy(entry.getAmount());
			} else {
				_balance.decrementBy(entry.getAmount());
			}
		} else {
			throw new IllegalStateException(
					"You're working with a raw Ledger object which is neither Debit nor Credit Postitive, so we can't get a balance for it.");
		}
	}

	/*
	 * Getters and Setters --------------------------------
	 */

	/**
	 * The ledger's current balance. Will calculate this if not yet available.
	 * The whole idea is NOT to calculate this until we actually need it, ie,
	 * certainly not on object instantiation.
	 */
	public Amount getBalance() {
		if (_balance == null) {
			calculateBalance();
		}

		return _balance;
	}

	/**
	 * Get the Set of Entry objects in this Ledger.
	 * 
	 * @return null if not yet established (and does NOT instantiate a blank one -
	 *         important for persistence).
	 */
	public Set getEntries() {
		return _entries;
	}

	/**
	 * Get the name tag (owner, customer, creditor, etc) of this Ledger
	 * 
	 * @return a String with the name of the Ledger.
	 */
	public String getName() {
		return _name;
	}

	/**
	 * Set the name of this Ledger.
	 */
	public void setName(String name) {
		_name = name;
	}

	/*
	 * Output ---------------------------------------------
	 */

	public void toOutput(PrintWriter out) {
		if ((_entries == null) || (_entries.size() == 0)) {
			out.println("<no entries>");
		} else {
			Iterator iter = _entries.iterator();
			while (iter.hasNext()) {
				Entry entry = (Entry) iter.next();
				entry.toOutput(out, true);
			}
		}
	}
	
	public String getClassString() {
		return "Ledger";
	}

	/**
	 * Yes, colour is spelled with a u... but in GTK it's spelled color. Fine.
	 */
	public String getColor() {
		if (this instanceof DebitPositiveLedger) {
			return Debit.COLOR;
		} else if (this instanceof CreditPositiveLedger) {
			return Credit.COLOR;
		} else {
			return "";
		}
	}

}