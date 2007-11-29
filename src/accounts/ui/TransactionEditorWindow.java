/*
 * TransactionEditorWindow.java
 * 
 * See LICENCE file for usage and redistribution terms
 * Copyright (c) 2006 Operational Dynamics
 */
package accounts.ui;

import generic.client.Master;
import generic.ui.EditorWindow;
import generic.ui.ModalDialog;
import generic.util.Debug;
import generic.util.DebugException;

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

                    CustomEvents.addEvent(new Runnable() {
                        public void run() {
                            Master.ui.showAsWorking(false);
                            self.deleteHook();
                        }
                    });

                } catch (final CommandNotReadyException cnre) {
                    Debug.print("events", "Command not ready: " + cnre.getMessage());
                    CustomEvents.addEvent(new Runnable() {
                        public void run() {
                            Master.ui.showAsWorking(false);

                            ModalDialog dialog = new ModalDialog(
                                    self.window,
                                    "Command Not Ready!",
                                    "<i>An exception was raised when trying to commit this accounting Transaction to the database. "
                                            + "There's a chance this is harmless, ie, if you just try again, or perhaps cancel, reopen, and try again, then you'll succeed. "
                                            + "More likely, the program has managed to get itself into an inconsistent state, probably due to a bug, and you'll need to restart to reset things. "
                                            + "The specific message raised was:</i>\n"
                                            + cnre.getMessage(), MessageType.ERROR);
                            dialog.run();

                            /*
                             * Leave the Window open so user can fix, as
                             * opposed to calling cancel()
                             */
                            window.present();
                        }
                    });
                }
            }
        }.start();
    }
}
