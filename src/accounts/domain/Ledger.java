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
	private String				name			= null;
	private Set					entries			= null;

	private Account				parentAccount	= null;

	/*
	 * Cached values --------------------------------------
	 */
	protected transient Amount	balance			= null;

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
		if (entries == null) {
			this.entries = new LinkedHashSet();
		}
		if (balance == null) {
			calculateBalance();
		}
		/*
		 * add
		 */
		entries.add(entry);
		addToBalance(entry);
	}

	/**
	 * Read the list of entries and sum them to arrive at this account's current
	 * balance. Used to set an initial value for a Ledger's balance if not
	 * currently set.
	 */
	protected void calculateBalance() {
		balance = new Amount("0");
		if (entries == null) {
			return;
		}

		Iterator iter = entries.iterator();
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
	protected void addToBalance(Entry entry) {
		throw new UnsupportedOperationException(
				"You're working with a raw Ledger object which is neither Debit nor Credit Postitive, so we can't add Entries to it.");

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
		if (balance == null) {
			calculateBalance();
		}

		return balance;
	}

	/**
	 * Get the Set of Entry objects in this Ledger.
	 * 
	 * @return null if not yet established (and does NOT instantiate a blank one -
	 *         important for persistence).
	 */
	public Set getEntries() {
		return entries;
	}

	/**
	 * Get the name tag (owner, customer, creditor, etc) of this Ledger
	 * 
	 * @return a String with the name of the Ledger.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Set the name of this Ledger.
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * parentAccount is a relation which allows one to walk "up" the object
	 * graph.
	 * 
	 * @return the Account which is the parent of this Ledger, which was set
	 *         when this Ledger was added to an Account.
	 * @see Entry#getParentLedger()
	 * @see Entry#getParentTransaction()
	 * @see Account#addLedger(Ledger)
	 */
	public Account getParentAccount() {
		return parentAccount;
	}

	/**
	 * A relation to allow you to track up the object graph, going the reverse
	 * direction to the Set which Account contains which caries the Ledgers.
	 * 
	 * @param parent
	 *            the Account which this Ledger belongs to.
	 * @see Account#addLedger(Ledger)
	 */
	public void setParentAccount(Account parent) {
		this.parentAccount = parent;
	}

	/*
	 * Output ---------------------------------------------
	 */

	public void toOutput(PrintWriter out) {
		if ((entries == null) || (entries.size() == 0)) {
			out.println("<no entries>");
		} else {
			Iterator iter = entries.iterator();
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