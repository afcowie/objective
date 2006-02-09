/*
 * Transaction.java
 * 
 * See LICENCE file for usage and redistribution terms
 * Copyright (c) 2005-2006 Operational Dynamics
 */
package accounts.domain;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Base class of the Transaction hierarchy. Transactions are operations which
 * are balanced double-entry accounting events. That also means that, per GAAP,
 * they have to have a source document somewhere.
 * 
 * @author Andrew Cowie
 */
public class Transaction
{
	/*
	 * Instance variables ---------------------------------
	 */
	protected String	description	= null;
	/**
	 * An identifier connected with the transaction. This is <b>not</b> a
	 * unique id or primary key, but rather some optional meta data originating
	 * from the external source document.
	 */
	protected String	identifier	= null;
	protected Datestamp	date		= null;
	protected Set		entries		= null;

	/*
	 * Constructors ---------------------------------------
	 */
	public Transaction() {
		/*
		 * Default constructor to permit generic top level searches. We don't
		 * instantiate the Set here, though - save that for addding an entry.
		 */
	}

	/**
	 * Create a new transaction prototype, setting the description field and
	 * providing an initial set of entries. This contstructor is largely
	 * intended for test cases and mockups.
	 * 
	 * @param description
	 * @param date
	 *            the Datestamp to assign to the Entries of this Transaction.
	 * @param entries
	 *            an array of Entry objects
	 */
	public Transaction(String description, Datestamp date, Entry[] entries) {
		setDescription(description);
		setDate(date);
		setEntries(entries);
	}

	/**
	 * Add an Entry to this Transaction.
	 * 
	 * <P>
	 * Note that this updates (sets) the date of the specified Entry to the date
	 * of this Transaction if that date has already been set. As you would
	 * expect, it also sets the parentTransaction field of the specified Entry.
	 * 
	 * @param entry
	 *            the Entry to add.
	 * @return true if the internal Entry Set was indeed modified (along the
	 *         lines of {@link java.util.Set#add})
	 */
	public boolean addEntry(Entry entry) {
		if (entry == null) {
			throw new NullPointerException("attempted to add a null entry!");
		}
		if (entries == null) {
			entries = new LinkedHashSet();
		}
		entry.setParentTransaction(this);
		if (date != null) {
			entry.setDate(date);
		}
		return entries.add(entry);
	}

	/**
	 * Perform checks on the Transaction object to make sure it is balanced
	 * Debits == Credits.
	 */
	public boolean isBalanced() {
		if (entries == null) {
			return true;
		}

		Amount total = new Amount("0");
		Iterator iter = entries.iterator();

		while (iter.hasNext()) {
			Entry entry = (Entry) iter.next();

			if (entry instanceof Debit) {
				total.incrementBy(entry.getAmount());
			} else if (entry instanceof Credit) {
				total.decrementBy(entry.getAmount());
			} else {
				throw new IllegalStateException("How did you get an Entry that's neither Debit nor Credit?");
			}
		}

		return total.isZero();
	}

	/*
	 * Getters and Setters --------------------------------
	 */

	public Set getEntries() {
		return entries;
	}

	/**
	 * Replace the internal list of entries, iterating over the supplied array
	 * and calling addEntry() one by one.
	 * 
	 * @param entries
	 *            an array of Entry objects to add to this Transaction.
	 */
	public void setEntries(Entry[] entries) {
		this.entries = null;
		for (int i = 0; i < entries.length; i++) {
			addEntry(entries[i]);
		}
	}

	/**
	 * Get the description of the transaction.
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Set a description for the transaction.
	 * 
	 * @param description
	 * @throws NullPointerException
	 *             if you try to set a null description.
	 */
	public void setDescription(String description) throws NullPointerException {
		if (description == null) {
			throw new NullPointerException("No null descriptions for transactions!");
		}
		this.description = description;
	}

	/**
	 * Get the identifier for the transaction, assuming one is set. Remember
	 * that this is <b>not</b> a unique id or primary key, but rather some meta
	 * data which may <b>optionally</b> be provided. Examples include cheque
	 * numbers, bill numbers, or statement numbers.
	 */
	public String getIdentifier() {
		return identifier;
	}

	/**
	 * Set an identifier for the transaction. In usage terms, it's optional.
	 * Typically this will be an invoice number or reciept number. Note that its
	 * <B>NOT</B> enforced to be unique across Transactions space.
	 * 
	 * @param identifier
	 *            the String to set as the Transaction's identifier.
	 */
	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	/**
	 * Get the date of this transaction
	 */

	public Datestamp getDate() {
		return date;
	}

	/**
	 * Set the date of the transaction.
	 * 
	 * <P>
	 * Works through all the entries to update their dates as well. (note that
	 * {@link Transaction#addEntry(Entry)} does the same update, assuming the
	 * date of the transaction is already available).
	 */

	public void setDate(Datestamp date) {
		this.date = date;
		if (entries == null) {
			return;
		}
		Iterator iter = entries.iterator();
		while (iter.hasNext()) {
			Entry entry = (Entry) iter.next();
			entry.setDate(date);
		}
	}

	/*
	 * Output ---------------------------------------------
	 */

	public String getClassString() {
		return "Transaction";
	}
}