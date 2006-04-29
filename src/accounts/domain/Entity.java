/*
 * Entity.java
 * 
 * See LICENCE file for usage and redistribution terms
 * Copyright (c) 2006 Operational Dynamics
 */
package accounts.domain;

/**
 * An entity we do business with. Can be an individual, business or
 * organization; both suppliers/vendors and clients are subclasses.
 * 
 * @author Andrew Cowie
 */
public class Entity
{
	/**
	 * The name of the person, business or organization. Will be used to name
	 * the Ledger associated with this entity.
	 */
	private String	name						= null;

	/**
	 * A number (or whatever) that uniquely identifies the business or entity.
	 * In western-world government circles, the term "Single Business
	 * Identifier" is generically used. Some countries don't have one (although
	 * Canada's GST registration number actually sufficies); Australia has ABN;
	 * etc.
	 * <p>
	 * Since we use "Identifier" for the hierarchy of database pseudo-constants,
	 * we use the term "reference" in association with String variables
	 * describing these identifying numbers.
	 * 
	 * @see Transaction#reference
	 */
	private String	singleBusinessReference	= null;

	public Entity() {

	}

	public Entity(String name) {
		setName(name);
	}

	public String getName() {
		return name;
	}

	/**
	 * Set the name of the Entity
	 * 
	 * @param name
	 *            keep in mind this name will be used for the name of the Ledger
	 *            associated with this Entity, so keep it real, y'dig?
	 */
	public void setName(String name) {
		if ((name == null) || (name.equals(""))) {
			throw new IllegalArgumentException("Can't set null or an empty String to be the name of this Entity");
		}
		this.name = name;
	}

	public String getSingleBusinessReference() {
		return singleBusinessReference;
	}

	/**
	 * 
	 * @param singleBusinessReference
	 *            Generally expected to be a number, nevertheless this is kept
	 *            as a String so you can format it (spaces, leading
	 *            alphanumeric, whatever) how you wish.
	 */
	public void setSingleBusinessReference(String reference) {
		this.singleBusinessReference = reference;
	}
}
