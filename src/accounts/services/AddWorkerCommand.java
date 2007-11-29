/*
 * AddWorkerCommand.java
 * 
 * See LICENCE file for usage and redistribution terms
 * Copyright (c) 2006 Operational Dynamics
 */
package accounts.services;

import generic.persistence.DataClient;
import generic.util.DebugException;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

import accounts.domain.Account;
import accounts.domain.CreditPositiveLedger;
import accounts.domain.Ledger;
import accounts.domain.ReimbursableExpensesPayableAccount;
import accounts.domain.Worker;
import accounts.persistence.IdentifierAlreadyExistsException;

/**
 * Add a Worker (Employee, Subcontractor) to the database. This Command wires
 * up a Ledger for the Worker into the ReimbursableExpensesPayableAccount.
 * 
 * @author Andrew Cowie
 */
public class AddWorkerCommand extends Command
{
    private Worker worker;

    private CreditPositiveLedger ledger;

    public AddWorkerCommand(Worker person) {
        super();
        if ((person.getName() == null) || (person.getName().equals(""))) {
            throw new IllegalStateException("Worker has to at least have a name set.");
        }
        this.worker = person;

        this.ledger = new CreditPositiveLedger(person.getName());
    }

    protected void action(DataClient store) throws CommandNotReadyException {
        /*
         * Make sure there isn't already a Worker by this name:
         */
        Worker proto = new Worker();
        proto.setName(worker.getName());

        List found = store.queryByExample(proto);

        if (found.size() > 0) {
            throw new IdentifierAlreadyExistsException(worker.getName() + " already exists as a Worker");
        }

        /*
         * Now fetch up the expenses payable account:
         */

        found = store.queryByExample(ReimbursableExpensesPayableAccount.class);

        if (found.size() > 1) {
            throw new DebugException(
                    "As coded, we only allow for there being one ReimbursableExpensesPayableAccount account.");
        } else if (found.size() == 0) {
            throw new IllegalStateException("Where is the ReimbursableExpensesPayableAccount?");
        }

        Account reimbursable = (Account) found.get(0);

        /*
         * Make sure there isn't a Ledger by this name already in there:
         */

        Set lS = reimbursable.getLedgers();
        if (lS != null) {
            Iterator lI = lS.iterator();
            String name = ledger.getName();
            while (lI.hasNext()) {
                Ledger l = (Ledger) lI.next();
                if (l.getName().equals(name)) {
                    throw new IllegalStateException("There is already an Ledger with "
                            + worker.getName()
                            + " as its name in the ReimbursableExpensesPayableAccount");
                }
            }
        }

        /*
         * Modify the domain objects:
         */

        reimbursable.addLedger(ledger);

        worker.setExpensesPayable(ledger);

        /*
         * And persist:
         */

        store.save(worker);
        store.save(reimbursable);
    }

    protected void reverse(DataClient store) throws CommandNotUndoableException {
        throw new UnsupportedOperationException();
    }

    public String getClassString() {
        return "Add Worker";
    }

}
