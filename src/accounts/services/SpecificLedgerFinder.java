/*
 * SpecificLedgerFinder.java
 * 
 * See LICENCE file for usage and redistribution terms
 * Copyright (c) 2006 Operational Dynamics
 */
package accounts.services;

import java.util.List;

import accounts.domain.Ledger;
import accounts.persistence.Selector;

/**
 * A simple Finder to lookup a specific Ledger given all or part of an Account
 * title and a Ledger name.
 * 
 * @author Andrew Cowie
 */
public class SpecificLedgerFinder extends Finder
{
	private transient String	accountTitle	= null;
	private transient String	ledgerName		= null;
	private transient Ledger	foundLedger		= null;

	/**
	 * Construct a blank finder. You'll have to set the account title and ledger
	 * name manually. Useful for cases where you do several finds at once.
	 * 
	 */
	public SpecificLedgerFinder() {
		super();
	}

	/**
	 * Construct a straight forward Finder.
	 * 
	 * @param accountTitle
	 *            calls setAccountTitle()
	 * @param ledgerName
	 *            calls setLedgerName()
	 * 
	 */
	public SpecificLedgerFinder(String accountTitle, String ledgerName) {
		super();
		setAccountTitle(accountTitle);
		setLedgerName(ledgerName);
	}

	/**
	 * Set the Account title you're looking up.
	 * 
	 * @param title
	 *            not to be null, but can be either all or part of the Account
	 *            title you are seeking. If it is blank, the Ledger name must be
	 *            specific enough to result in the return of a single Ledger
	 *            when {@link getLedger()} is run.
	 */
	public void setAccountTitle(String title) {
		if (title == null) {
			throw new IllegalArgumentException();
		}
		this.accountTitle = title;
		reset();
	}

	/**
	 * Set the Ledger name you're looking up.
	 * 
	 * @param name
	 *            not null or blank, but can be either all or part of the Ledger
	 *            name you are seeking. If it is blank, the Account title must
	 *            be specific enough to result in the return of a single Ledger
	 *            when {@link getLedger()} is run.
	 */
	public void setLedgerName(String name) {
		if (name == null) {
			throw new IllegalArgumentException();
		}
		this.ledgerName = name;
		reset();
	}

	/**
	 * As the point of this Finder is to return a single Ledger, we leave this
	 * protected.
	 */
	protected List query() throws NotFoundException {
		if ((accountTitle == null) || (ledgerName == null)) {
			throw new IllegalStateException("You can't run this finder with title or name null");
		}
		if ((accountTitle.equals("")) && (ledgerName.equals(""))) {
			throw new IllegalStateException("You can't run this finder with title and name both blank");
		}

		return store.nativeQuery(new Selector() {
			public boolean match(Ledger l) {
				if (l.getName().indexOf(ledgerName) != -1) {
					if (l.getParentAccount().getTitle().indexOf(accountTitle) != -1) {
						return true;
					}
				}
				return false;
			}
		});
	}

	/**
	 * Perform the requested lookup
	 * 
	 * @return the single Ledger that matches the title and name supplied to the
	 *         constructor or setters.
	 * @throws NotFoundException
	 *             if no Ledger matched the search critera.
	 * @throws UnsupportedOperationException
	 *             if your search results in more than a single Ledger (ie, you
	 *             have to narrow the search with more precise terms)
	 */
	public Ledger getLedger() throws NotFoundException {
		List result = null;
		/*
		 * Run query() if necessary.
		 */
		if (foundLedger == null) {
			result = query();
		}
		final int len = result.size();

		if (len > 1) {
			throw new UnsupportedOperationException(
				"When using SpecificLedgerFinder, you need to specify arguments such that only one ledger will be retreived. You retrieved "
					+ len);
		}

		final Object obj = result.get(0);

		if (!(obj instanceof Ledger)) {
			throw new IllegalStateException("In querying Ledgers, you managed to get something not a Ledger!");
		}

		return foundLedger = (Ledger) obj;
	}

	/**
	 * Reset the Finder (null the cached foundLedger) to force getLedger() to
	 * rerun the query() method.
	 */
	protected void reset() {
		foundLedger = null;
	}
}

/**
 * Code originally in DataStore. Included here in a null comment to record an
 * example of using the query SODA interface.
 * 
 * <pre>
 * public Ledger getLedger(String accountTitle, String ledgerName) {
 * 	Query query = container.query();
 * 
 * 	// We work inside-out here. Actually, we want ledgers
 * 	query.constrain(Ledger.class);
 * 
 * 	// Constrain it with the account and ledger information
 * 	query.descend(&quot;name&quot;).constrain(ledgerName).contains();
 * 
 * 	Query subquery = query.descend(&quot;parentAccount&quot;);
 * 	subquery.descend(&quot;title&quot;).constrain(accountTitle).contains();
 * 
 * 	ObjectSet os = query.execute();
 * 
 * 	final int len = os.size();
 * 
 * 	if (len &gt; 1) {
 * 		throw new UnsupportedOperationException(
 * 			&quot;When calling getLedger(), you need to specify arguments such that only one ledger will be retreived!&quot;);
 * 	}
 * 
 * 	Object obj = os.next();
 * 
 * 	if (!(obj instanceof Ledger)) {
 * 		throw new IllegalStateException(&quot;In querying Ledgers, you managed to get something not a Ledger!&quot;);
 * 	}
 * 
 * 	return (Ledger) obj;
 * }
 * </pre>
 */
