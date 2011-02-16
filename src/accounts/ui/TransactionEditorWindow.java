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
package accounts.ui;

import generic.client.Master;
import generic.ui.EditorWindow;
import generic.util.Debug;
import generic.util.DebugException;

import org.gnome.gtk.Dialog;
import org.gnome.gtk.ErrorMessageDialog;

import accounts.domain.Transaction;
import accounts.services.Command;
import accounts.services.CommandNotReadyException;
import accounts.services.PostTransactionCommand;
import accounts.services.UpdateTransactionCommand;

/**
 * Transactions all have in common that they are either creating a new
 * Transaction or editing an existing on. In the former case,
 * PostTransactionCommand must be called, in the latter,
 * UpdateTransactionCommand is used. Both operate on generalized Transaction
 * objects and so it is feasible to abstract up common functionality.
 * <p>
 * Certain workaround have been necessary, notably doing the save and commit
 * calls to the database in a worker Thread. This is somewhat verbose code, so
 * we do it here in ok(), which subclasses are expected to call after they
 * have populated the transaction they are operating on.
 * <p>
 * The constructors pass straight through to EditorWindow. If a database id is
 * specified, the object is fetched and the TransactionEditorWindow marks
 * itself as editing.
 * 
 * @author Andrew Cowie
 */
public abstract class TransactionEditorWindow extends EditorWindow
{
    protected Transaction transaction;

    protected boolean editing;

    /**
     * A reflexive reference. We don't make this available externally,
     * following a convention that if you want <code>self</code>, define it
     * yourself in the class you need so it has the method visibility you'd
     * expect of this.
     */
    private TransactionEditorWindow self;

    /**
     * Set <code>self</code> and, for a non-zero id, fetch the object and
     * set <code>editing</code> to true.
     */
    private void setup(long id) {
        self = this;

        if (id == 0) {
            transaction = null;
            editing = false;
            /*
             * You will have to populate transaction with the new object you
             * are creating, and set a useful title.
             */
        } else {
            transaction = (Transaction) store.fetchByID(id);
            store.reload(transaction);

            // List lL = store.queryByExample(Ledger.class);
            // Ledger[] ledgers = (Ledger[]) lL.toArray(new
            // Ledger[lL.size()]);
            // for (int i = 0; i < ledgers.length ; i++) {
            // store.reload(ledgers[i]);
            // }

            setTitle(transaction.getDescription());
            editing = true;
        }
    }

    public TransactionEditorWindow(long id) {
        super();
        setup(id);
    }

    public TransactionEditorWindow(long id, String whichElement, String gladeFilename) {
        super(whichElement, gladeFilename);
        setup(id);
    }

    /**
     * Execute the transaction to store or update the Transaction, doing so in
     * a worker thread. Carry out any necessary guards before calling this.
     */
    protected void ok() {
        /*
         * Assumes any necessary guards have been passed. One more:
         */

        if (transaction == null) {
            throw new DebugException("Programmer: you must set transaction to your new object");
        }

        /*
         * Fork the processing off into a Thread so the UI doesn't freeze up:
         */

        window.hide();
        Master.ui.showAsWorking(true);

        new Thread() {
            public void run() {
                Debug.print("threads", "Carrying out update");
                hide();

                Command c = null;
                if (editing) {
                    c = new UpdateTransactionCommand(transaction);
                } else {
                    c = new PostTransactionCommand(transaction);
                }

                try {
                    c.execute(store);

                    store.commit();

                    Master.ui.showAsWorking(false);
                    self.deleteHook();

                } catch (final CommandNotReadyException cnre) {
                    final Dialog dialog;

                    Debug.print("events", "Command not ready: " + cnre.getMessage());
                    Master.ui.showAsWorking(false);

                    dialog = new ErrorMessageDialog(
                            self.window,
                            "Command Not Ready!",
                            "<i>An exception was raised when trying to commit this accounting Transaction to the database. "
                                    + "There's a chance this is harmless, ie, if you just try again, or perhaps cancel, reopen, and try again, then you'll succeed. "
                                    + "More likely, the program has managed to get itself into an inconsistent state, probably due to a bug, and you'll need to restart to reset things. "
                                    + "The specific message raised was:</i>\n" + cnre.getMessage());
                    dialog.run();

                    /*
                     * Leave the Window open so user can fix, as opposed to
                     * calling cancel()
                     */
                    window.present();
                }
            }
        }.start();
    }
}
