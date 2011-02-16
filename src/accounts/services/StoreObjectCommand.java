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
package accounts.services;

import generic.persistence.DataClient;

/**
 * Store a generic Object to the DataClient. Like all Commands this is just a
 * wrapper around the basic DataClient.save() functionality (which in turn is
 * just a wrapper around db4o's ObjectContainer.set()) - however this one does
 * no domain specific validation and so is appropriate for the cases where
 * that simply isn't required, but you to still need (ought) to use the
 * Command and UnitOfWork signalling framework.
 * 
 * @author Andrew Cowie
 */
public class StoreObjectCommand extends Command
{
    private transient Object obj;

    public StoreObjectCommand(Object o) {
        super();
        if (o == null) {
            throw new IllegalArgumentException("Can't store null");
        }
        this.obj = o;
    }

    protected void action(DataClient store) throws CommandNotReadyException {
        store.save(obj);
    }

    protected void reverse(DataClient store) throws CommandNotUndoableException {
        throw new CommandNotUndoableException();
    }

    public String getClassString() {
        return "Store Object";
    }
}
