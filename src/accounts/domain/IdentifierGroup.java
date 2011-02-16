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

import generic.domain.Cascade;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import accounts.persistence.IdentifierAlreadyExistsException;

/**
 * A group of Identifiers maintained in a specific order for display purposes.
 * Note that this is really just a named ordered Collection; more than one
 * IdentifierGroup can include any given Identifier.
 * 
 * @author Andrew Cowie
 */
public class IdentifierGroup implements Cascade
{
    private String label = null;

    private List group = null;

    /**
     * Construct a new IdentifierGroup with label as its "title"
     */
    public IdentifierGroup(String label) {
        setLabel(label);
    }

    public String getLabel() {
        return label;
    }

    /**
     * Set the "title" of this IdentifierGroup. Note that null and "" are
     * invalid.
     */
    public void setLabel(String label) {
        if ((label == null) || (label.equals(""))) {
            throw new IllegalArgumentException(
                    "Can't use null or an empty string as the label for a group");
        }
        this.label = label;
    }

    /**
     * Add an Identifier to this IdentifierGroup. Note that you can't add
     * null.
     * 
     * @param identifier
     *            the Identifier to add.
     * 
     * @throws IdentifierAlreadyExistsException
     *             if the identifier argument is already in the list (loosely
     *             enforces Set behaviour here)
     */
    public void addIdentifier(Identifier identifier) {
        if (group == null) {
            group = new LinkedList();
        }
        if (identifier == null) {
            throw new IllegalArgumentException("Can't add null to an IdentifierGroup");
        }

        Iterator iter = group.iterator();
        while (iter.hasNext()) {
            Identifier i = (Identifier) iter.next();
            if ((i == identifier) || (i.compareTo(identifier) == 0)) {
                throw new IdentifierAlreadyExistsException("Identifier " + identifier
                        + " is already in this group");
            }
        }

        /*
         * For a linked list, the behaviour of add() is to append the object
         * to the end of the List. [which is what addLast() achieves by simply
         * calling add(), so say the docs]
         */
        group.add(identifier);
    }

    /**
     * Remove an Identifier from this IdentifierGroup.
     * 
     * @param identifier
     *            the Identifier to add.
     */
    public void removeIdentifier(Identifier identifier) {
        if (group == null) {
            throw new IllegalStateException();
        }
        if (identifier == null) {
            throw new IllegalArgumentException("Can't remove null");
        }

        int i = group.indexOf(identifier);
        if (i == -1) {
            throw new IllegalArgumentException("This IdentifierGroup doesn't contain that Identifier");
        } else {
            group.remove(i);
        }
    }

    /**
     * Swap the positions of two identifiers in the group. You'd likely call
     * this in response to a move up or move down in a UI.
     */
    public void swap(Identifier identifier1, Identifier identifier2) {
        if (!group.contains(identifier1)) {
            throw new IllegalArgumentException("Identifier " + identifier1 + " not in this group");
        }
        if (!group.contains(identifier2)) {
            throw new IllegalArgumentException("Identifier " + identifier2 + " not in this group");
        }

        int i1 = group.indexOf(identifier1);
        int i2 = group.indexOf(identifier2);

        group.set(i1, identifier2);
        group.set(i2, identifier1);
    }

    /**
     * @return the backing List. You can get an Iterator from there.
     */
    public List getIdentifiers() {
        return group;
    }
}
