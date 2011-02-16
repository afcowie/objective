/*
 * ObjectiveAccounts, accounting for small professional services firms.
 *
 * Copyright © 2006-2011 Operational Dynamics Consulting, Pty Ltd
 *
 * The code in this file, and the program it is a part of, is made available
 * to you by its authors as open source software: you can redistribute it
 * and/or modify it under the terms of the GNU General Public License version
 * 2 ("GPL") as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GPL for more details.
 *
 * You should have received a copy of the GPL along with this program. If not,
 * see http://www.gnu.org/licenses/. The authors of this program may be
 * contacted through http://research.operationaldynamics.com/projects/objective/.
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
    private String name = null;

    /**
     * A number (or whatever) that uniquely identifies the business or entity.
     * In western-world government circles, the term "Single Business
     * Identifier" is generically used. Some countries don't have one
     * (although Canada's GST registration number actually sufficies);
     * Australia has ABN; etc.
     * <p>
     * Since we use "Identifier" for the hierarchy of database
     * pseudo-constants, we use the term "reference" in association with
     * String variables describing these identifying numbers.
     * 
     * @see Transaction#getReference() Transaction's reference
     */
    private String singleBusinessReference = null;

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
     *            keep in mind this name will be used for the name of the
     *            Ledger associated with this Entity, so keep it real, y'dig?
     */
    public void setName(String name) {
        if ((name == null) || (name.equals(""))) {
            throw new IllegalArgumentException(
                    "Can't set null or an empty String to be the name of this Entity");
        }
        this.name = name;
    }

    /**
     * @see #setSingleBusinessReference(String)
     * @return the reference String associated with this Entity.
     */
    public String getSingleBusinessReference() {
        return singleBusinessReference;
    }

    /**
     * @param reference
     *            Generally expected to be a number, nevertheless this is kept
     *            as a String so you can format it (spaces, leading
     *            alphanumeric, whatever) how you wish.
     */
    public void setSingleBusinessReference(String reference) {
        this.singleBusinessReference = reference;
    }
}
