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
 * contacted via http://research.operationaldynamics.com/projects/objective/.
 */
package accounts.services;

import generic.persistence.DataClient;
import generic.util.DebugException;

import java.util.List;

import accounts.domain.Account;
import accounts.domain.AccountsPayable;
import accounts.domain.AccountsReceivable;
import accounts.domain.Client;
import accounts.domain.ClientLedger;
import accounts.domain.Entity;
import accounts.domain.Ledger;
import accounts.domain.Supplier;
import accounts.domain.SupplierLedger;
import accounts.persistence.IdentifierAlreadyExistsException;

/**
 * Given a new Entity object, create the appropriate ClientLedger or
 * SupplierLedger, then persist them to the database.
 * 
 * @author Andrew Cowie
 */
public class AddEntityCommand extends Command
{
    private transient Entity entity = null;

    private transient Ledger ledger = null;

    /**
     * ... Assumes that a ledger in the name of this Entity has not yet been
     * created.
     * 
     * @param entity
     */
    public AddEntityCommand(Entity entity) {
        this.entity = entity;
        if (entity instanceof Client) {
            this.ledger = new ClientLedger((Client) entity);
        } else if (entity instanceof Supplier) {
            this.ledger = new SupplierLedger((Supplier) entity);
        } else {
            throw new IllegalArgumentException("Huh? How come neither Client nor Supplier?");
        }
    }

    /**
     * Creates the ItemsLedger appropriate to this Entity
     * 
     * @throws IdentifierAlreadyExistsException
     *             if the Entity you want to store is already present.
     */
    protected void action(DataClient store) throws CommandNotReadyException {
        if (ledger.getName() == null) {
            throw new CommandNotReadyException("The Ledger (Client or Supplier) passed has a null name!");
        }

        /*
         * Use DataClient (db4o)'s capability to search by example to see if
         * there's already an Entity by this name.
         */
        Entity prototype = new Entity();
        prototype.setName(entity.getName());

        List found = store.queryByExample(prototype);

        if (found.size() > 0) {
            throw new IdentifierAlreadyExistsException(entity.getName() + " already exists as an Entity");
        }

        /*
         * Ditto a {Client,Supplier}Ledger by this name
         */

        Ledger protoLedger = new Ledger();
        protoLedger.setName(entity.getName());

        found = store.queryByExample(protoLedger);

        if (found.size() > 0) {
            throw new IdentifierAlreadyExistsException("There is already an ItemsLedger with "
                    + entity.getName() + " as its name");
        }

        /*
         * Safety checks passed. Now fetch up the appropriate
         * Account{Receivable|Payable} Account.
         */

        if (entity instanceof Client) {
            found = store.queryByExample(AccountsReceivable.class);
        } else if (entity instanceof Supplier) {
            found = store.queryByExample(AccountsPayable.class);
        } else {
            throw new DebugException(
                    "Huh? How did this AddEntityCommmand come to have neither Client nor Supplier as its candidate Entity?");
        }

        if (found.size() > 1) {
            throw new DebugException(
                    "As coded, we only allow for there being one Accounts{Receivable|Payable} account");
        } else if (found.size() == 0) {
            throw new IllegalStateException("Where is the Accounts{Receivable|Payable} account?");
        }

        Account trade = (Account) found.get(0);
        trade.addLedger(ledger);

        // TODO automatically? Linking them in action()?

        store.save(entity);
        store.save(trade);
    }

    protected void reverse(DataClient store) throws CommandNotUndoableException {
        throw new UnsupportedOperationException();
    }

    public String getClassString() {
        return "Add Entity";
    }
}
