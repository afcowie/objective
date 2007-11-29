/*
 * Ledger.java
 * 
 * See LICENCE file for usage and redistribution terms
 * Copyright (c) 2005-2006 Operational Dynamics
 */
package accounts.domain;

import generic.domain.DomainObject;
import generic.domain.Normal;
import generic.util.DebugException;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Base class for the ledgers within actual accounts.
 * 
 * @author Andrew Cowie
 */
public class Ledger extends DomainObject implements Normal
{
    /*
     * Instance variables ---------------------------------
     */
    private String name = null;

    private Set entries = null;

    private Account parentAccount = null;

    /*
     * Cached values --------------------------------------
     */
    protected transient Amount balance = null;

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
     *             if you try to add an Entry which is neither Debit nor
     *             Credit (both are concrete subclasses of Entry).
     */
    public void addEntry(Entry entry) {
        /*
         * validation
         */
        if (entry == null) {
            throw new IllegalArgumentException("Attempted to add a null Entry!");
        }

        if (!((entry instanceof Debit) || (entry instanceof Credit))) {
            throw new DebugException("attempted to add an Entry which is neither Debit nor Credit!");
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
     * Tell the ledger than an Entry has changed. Somewhat artificial; all
     * this does is zero out the cached balance value forcing it to be
     * recalculated next time is is requested.
     */
    public void updateEntry(Entry one) {
        /*
         * TODO How can we possibly figure out what the old value of the Entry
         * was? If we could, it would surely be better to subtract that and
         * then add the current value, rather than forcing a full
         * recalculation.
         */
        balance = null;
    }

    /**
     * Remove an Entry from this ledger, adjusting the balance accordingly. We
     * don't set the Entry's parentLedger to null; that's the business of
     * whoever is removing this Entry; it'll either be deleted or reused
     * immediately so no need to mess with it in that way.
     */
    public void removeEntry(Entry entry) {
        if (entry == null) {
            throw new IllegalArgumentException("Can't remove a null Entry!");
        }
        if ((entries == null) || (!(entries.contains(entry)))) {
            throw new IllegalStateException("You've asked to remove an Entry that isn't in this Ledger");
        }
        if (balance == null) {
            calculateBalance();
        }
        entries.remove(entry);
        subtractFromBalance(entry);
    }

    /**
     * Read the list of entries and sum them to arrive at this account's
     * current balance. Used to set an initial value for a Ledger's balance if
     * not currently set.
     */
    public void calculateBalance() {
        if (balance == null) {
            balance = new Amount("0");
        } else {
            balance.setValue("0");
        }

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
     * Add and Entry's Amount to the Ledger's balance.
     * 
     * @param entry
     *            The Entry whose amount we will add to the Ledger's balance.
     *            It will be tested for Debit/Credit-ness and added
     *            accordingly.
     */
    protected void addToBalance(Entry entry) {
        throw new UnsupportedOperationException(
                "You're working with a raw Ledger object which is neither Debit nor Credit Postitive, so we can't add Entries to it.");

    }

    /**
     * Subtract an Entry's Amount from the Ledger's balance.
     * 
     * @param entry
     *            The Entry whose amount we will subtract from the Ledger's
     *            balance. It will be tested for Debit/Credit-ness and added
     *            accordingly.
     */
    protected void subtractFromBalance(Entry entry) {
        throw new UnsupportedOperationException(
                "You're working with a raw Ledger object which is neither Debit nor Credit Postitive, so we can't subtract an Entry from it.");
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
     * @return null if not yet established (and does NOT instantiate a blank
     *         one - important for persistence).
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
        if (name.equals("")) {
            throw new IllegalArgumentException("Can't set a blank String as the name of a Ledger");
        }
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

    public String getClassString() {
        return "Ledger";
    }

    public String toString() {
        return getClassString() + ": " + name + " [" + entries.size() + "]";
    }

    /**
     * Yes, colour is spelled with a u... but in GTK it's spelled color. Fine.
     */
    public String getColor(boolean active) {
        if (this instanceof DebitPositiveLedger) {
            if (active) {
                return Debit.COLOR_ACTIVE;
            } else {
                return Debit.COLOR_NORMAL;
            }
        } else if (this instanceof CreditPositiveLedger) {
            if (active) {
                return Credit.COLOR_ACTIVE;
            } else {
                return Credit.COLOR_NORMAL;
            }

        } else {
            return "";
        }
    }
}
