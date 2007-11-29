/*
 * GenericTransactionEditorWindow.java
 * 
 * See LICENCE file for usage and redistribution terms
 * Copyright (c) 2006 Operational Dynamics
 */
package accounts.ui;

import generic.ui.Align;
import generic.ui.ChangeListener;
import generic.ui.ModalDialog;
import generic.ui.TextEntry;
import generic.ui.TwoColumnTable;

import java.util.Iterator;

import org.gnome.gtk.Button;
import org.gnome.gtk.HBox;
import org.gnome.gtk.Label;
import org.gnome.gtk.MessageType;
import org.gnome.gtk.ReliefStyle;
import org.gnome.gtk.SizeGroup;
import org.gnome.gtk.SizeGroupMode;
import org.gnome.gtk.Stock;
import org.gnome.gtk.Table;
import org.gnome.gtk.VBox;
import org.gnome.gtk.Widget;

import accounts.domain.Amount;
import accounts.domain.Books;
import accounts.domain.Credit;
import accounts.domain.CreditPositiveLedger;
import accounts.domain.Currency;
import accounts.domain.Debit;
import accounts.domain.DebitPositiveLedger;
import accounts.domain.Entry;
import accounts.domain.ForeignAmount;
import accounts.domain.GenericTransaction;

/**
 * Create or Edit a GenericTransaction (often known in other accounting
 * programs as a "General Ledger transaction"). This has an arbitrary number
 * of Entries, Credit or Debit as specified by the user, against arbitrarily
 * specified accounts.
 * 
 * @see accounts.domain.GenericTransaction
 * @author Andrew Cowie
 */
/*
 * We take a slightly different approach here, forming the Transaction object
 * as we go and using its methods to store data, rather than instantiating it
 * in ok().
 */
public class GenericTransactionEditorWindow extends TransactionEditorWindow
{
    /**
     * The transaction object we are building up or editing.
     */
    private GenericTransaction t = null;

    /*
     * UI elements
     */

    private DatePicker dP = null;

    private VBox eB = null;

    private TextEntry dE = null;

    private SizeGroup accountSizeGroup = null;

    private SizeGroup amountSizeGroup = null;

    private SizeGroup columnSizeGroup = null;

    private SizeGroup flopSizeGroup = null;

    private SizeGroup addRemoveSizeGroup = null;

    /*
     * We already have all the logic for adding up Debits and Credits in
     * Ledger. So use one to calculate the sum of each column.
     */
    private DebitPositiveLedger debitPos;

    private CreditPositiveLedger creditPos;

    private AmountDisplay debitBalance = null;

    private AmountDisplay creditBalance = null;

    private Button addButton;

    /**
     * Instantiate a new GenericTransaction so the user can start filling it
     * in.
     */
    public GenericTransactionEditorWindow() {
        this(0);
    }

    /**
     * Modify an existing GenericTransaction
     * 
     * @param id
     *            the database ID of the Transaction you intend to edit.
     */
    public GenericTransactionEditorWindow(long id) {
        super(id);

        if (id == 0) {
            t = new GenericTransaction();
            transaction = t;
            setTitle("Enter a new Generic Transaction");
        } else {
            t = (GenericTransaction) transaction;
        }

        TwoColumnTable table = new TwoColumnTable(2);

        /*
         * We use block to hide all the silly variables (especially the
         * Labels), and to help group things logically connected (ie, left
         * right elements) so they're visually distinct in the code.
         */
        {
            Label tL = new Label("<big><b>Generic Transaction</b></big>");
            tL.setUseMarkup(true);
            tL.setAlignment(0.0, 0.5);
            top.packStart(tL, false, false, 3);
        }
        {
            Label dL = new Label("Date:");
            dL.setAlignment(1.0f, 0.5f);
            table.attach(dL, Align.LEFT);
            dP = new DatePicker();
            dP.setDate(t.getDate());
            table.attach(dP, Align.RIGHT);
        }
        {
            Label dL = new Label("Description");
            dL.setAlignment(1.0f, 0.5f);
            table.attach(dL, Align.LEFT);
            dE = new TextEntry();
            dE.setText(t.getDescription());
            table.attach(dE, Align.RIGHT);
        }

        top.packStart(table, false, false, 0);

        accountSizeGroup = new SizeGroup(SizeGroupMode.HORIZONTAL);
        amountSizeGroup = new SizeGroup(SizeGroupMode.HORIZONTAL);
        columnSizeGroup = new SizeGroup(SizeGroupMode.HORIZONTAL);
        flopSizeGroup = new SizeGroup(SizeGroupMode.HORIZONTAL);
        addRemoveSizeGroup = new SizeGroup(SizeGroupMode.HORIZONTAL);

        /*
         * Now setup the UI for displaying the Entries. The instantiation of
         * the artificial balance Ledgers and their respective Entries is here
         * so they are not null when ChangeListeners fire while populating the
         * EntryRows.
         */

        debitPos = new DebitPositiveLedger();
        creditPos = new CreditPositiveLedger();
        debitBalance = new AmountDisplay();
        creditBalance = new AmountDisplay();

        {
            HBox headings;

            headings = new HBox(false, 3);
            Label aL = new Label("Account / Ledger");
            aL.setAlignment(0.0f, 0.5f);

            Label faL = new Label("Amount");
            faL.setAlignment(0.0f, 0.5f);

            Label drL = new Label("Debits");
            Label crL = new Label("Credits  ");
            drL.setAlignment(1.0f, 0.5f);
            crL.setAlignment(1.0f, 0.5f);

            /*
             * We put these two Labels into a Table so as to force the Widgets
             * to be wide, thus giving them the space to right align in.
             */
            Table flopTable = new Table(1, 2, true);
            flopTable.attach(drL, 0, 1, 0, 1);
            flopTable.attach(crL, 1, 2, 0, 1);

            Label blankL = new Label("");

            accountSizeGroup.addWidget(aL);
            amountSizeGroup.addWidget(faL);
            flopSizeGroup.addWidget(flopTable);

            addRemoveSizeGroup.addWidget(blankL);
            headings.packStart(aL, true, true, 0);
            headings.packStart(faL, false, false, 0);
            headings.packStart(flopTable, false, false, 0);
            headings.packStart(blankL, false, false, 0);

            top.packStart(headings, false, false, 0);
        }
        {
            eB = new VBox(true, 3);

            if (editing) {
                Iterator eI = t.getEntries().iterator();
                while (eI.hasNext()) {
                    Entry e = (Entry) eI.next();
                    addRow(e);
                }

                /*
                 * Somewhat unusually, we remove the Entris from the
                 * Transaction as we add them to this EditorWindow This allows
                 * us to rebuild the Transaction later with whatever Entries
                 * we end up with (as some may be new objects having
                 * flip-flopped).
                 */
                t.getEntries().clear();
            } else {
                /*
                 * We start with two rows
                 */
                Debit dr = new Debit();
                dr.setAmount(new Amount(0));
                addRow(dr);

                Credit cr = new Credit();
                cr.setAmount(new Amount(0));
                addRow(cr);
            }

            top.packStart(eB, false, false, 0);
        }
        {
            HBox tailings;

            tailings = new HBox(false, 3);

            addButton = new Button(Stock.ADD);
            addButton.addListener(new ButtonListener() {
                public void buttonEvent(ButtonEvent event) {
                    if (event.getType() == ButtonEvent.Type.CLICK) {
                        addRow(new Debit(new Amount("0"), null));
                        eB.showAll();
                    }
                }
            });
            addRemoveSizeGroup.addWidget(addButton);
            tailings.packEnd(addButton, false, false, 0);

            debitBalance.setAmount(debitPos.getBalance());
            creditBalance.setAmount(creditPos.getBalance());
            tailings.packEnd(creditBalance, false, false, 0);
            tailings.packEnd(debitBalance, false, false, 0);
            columnSizeGroup.addWidget(debitBalance);
            columnSizeGroup.addWidget(creditBalance);

            eB.packEnd(tailings, true, false, 0);
        }
    }

    private void addRow(Entry e) {
        EntryRow row = new EntryRow(e);
        eB.packStart(row, false, false, 0);
    }

    /**
     * An HBox representing an Entry within the GenericTransaction. Because
     * these are generic, they can be either Debits or Credits. The UI
     * presented allows you to toggle between the two. If it is changed from
     * Debit to Credit or vice versa, then a new Entry object will be
     * instantiated. If it remains the same, then the original object will be
     * updated.
     * 
     * @author Andrew Cowie
     */
    class EntryRow extends HBox
    {
        /**
         * Determine whether the current display is Debit or Credit.
         */
        private Entry state;

        /**
         * The reference to the original Entry that was pased to this Row.
         */
        private Entry original;

        private Entry alternate;

        /*
         * UI elements
         */

        private AccountPicker accountPicker = null;

        private ForeignAmountEntryBox amountEntry = null;

        private Button flopButton = null;

        private Table flopTable = null;

        private Label sideLabel = null;

        private Button deleteButton = null;

        /*
         * Need a reference so that other
         */
        private HBox box;

        EntryRow(Entry e) {
            super(false, 1);

            this.original = e;
            this.state = e;

            /*
             * Setup the state tracking references and add the Entry to the
             * appropriate balance tracker.
             */

            if (state instanceof Debit) {
                alternate = new Credit();
                debitPos.addEntry(e);
            } else if (state instanceof Credit) {
                alternate = new Debit();
                creditPos.addEntry(e);
            } else {
                throw new IllegalArgumentException("Supplied Entry must be Debit or Credit");
            }
            alternate.setParentLedger(e.getParentLedger());

            box = this;

            accountPicker = new AccountPicker(store);
            try {
                accountPicker.setLedger(e.getParentLedger());
            } catch (IllegalArgumentException iae) {
                // ignore
            }
            packStart(accountPicker, true, true, 0);
            accountSizeGroup.addWidget(accountPicker);

            amountEntry = new ForeignAmountEntryBox(store);
            amountEntry.addListener(new ChangeListener() {
                public void userChangedData() {
                    state.setAmount(amountEntry.getForeignAmount());
                    if (state instanceof Debit) {
                        debitPos.updateEntry(state);
                        debitBalance.setAmount(debitPos.getBalance());
                    } else if (state instanceof Credit) {
                        creditPos.updateEntry(state);
                        creditBalance.setAmount(creditPos.getBalance());
                    }

                }
            });

            Amount a = e.getAmount();
            if (a instanceof ForeignAmount) {
                amountEntry.setForeignAmount((ForeignAmount) e.getAmount());
            } else if (a != null) {
                Books root = (Books) store.getRoot();
                Currency home = root.getHomeCurrency();
                ForeignAmount fa = new ForeignAmount(a.getValue(), home, "1.0");
                amountEntry.setForeignAmount(fa);
            }
            box.packStart(amountEntry, false, false, 0);
            amountSizeGroup.addWidget(amountEntry);

            sideLabel = new Label("");
            sideLabel.setAlignment(1.0, 0.5);
            columnSizeGroup.addWidget(sideLabel);

            /*
             * Just put the sideLabel anywhere; it'll be moved by the forced
             * initial call to flipFlop() - it just needs to be present so
             * flipFlop() can remove it.
             */
            flopTable = new Table(1, 2, true);
            flopTable.attach(sideLabel, 0, 1, 0, 1);

            flopButton = new Button();
            flopButton.setRelief(ReliefStyle.NONE);
            flopButton.add(flopTable);

            box.packStart(flopButton, false, false, 3);
            flopSizeGroup.addWidget(flopButton);

            flipFlop();

            flopButton.addListener(new ButtonListener() {
                public void buttonEvent(ButtonEvent event) {
                    if (event.getType() == ButtonEvent.Type.CLICK) {

                        if (state instanceof Debit) {
                            debitPos.removeEntry(state);
                        } else if (state instanceof Credit) {
                            creditPos.removeEntry(state);
                        }

                        if (state == original) {
                            state = alternate;
                            state.setAmount(original.getAmount());
                        } else {
                            state = original;
                            state.setAmount(alternate.getAmount());
                        }

                        flipFlop();

                        if (state instanceof Debit) {
                            debitPos.addEntry(state);
                        } else if (state instanceof Credit) {
                            creditPos.addEntry(state);
                        }

                        debitBalance.setAmount(debitPos.getBalance());
                        creditBalance.setAmount(creditPos.getBalance());
                    }
                }
            });

            deleteButton = new Button(Stock.DELETE);
            deleteButton.addListener(new ButtonListener() {
                public void buttonEvent(ButtonEvent event) {
                    if (event.getType() == ButtonEvent.Type.CLICK) {
                        eB.remove(box);

                        if (state instanceof Debit) {
                            debitPos.removeEntry(state);
                        } else if (state instanceof Credit) {
                            creditPos.removeEntry(state);
                        }

                        debitBalance.setAmount(debitPos.getBalance());
                        creditBalance.setAmount(creditPos.getBalance());
                    }
                }
            });

            box.packEnd(deleteButton, false, false, 0);
            addRemoveSizeGroup.addWidget(deleteButton);
        }

        private void flipFlop() {
            if (state instanceof Debit) {
                sideLabel.setMarkup("<span color='" + Debit.COLOR_NORMAL + "'>Debit</span>");
                flopTable.remove(sideLabel);
                flopTable.attach(sideLabel, 0, 1, 0, 1);
            } else if (state instanceof Credit) {
                sideLabel.setMarkup("<span color='" + Credit.COLOR_NORMAL + "'>Credit</span>");
                flopTable.remove(sideLabel);
                flopTable.attach(sideLabel, 1, 2, 0, 1);
            } else {
                throw new IllegalStateException("Trying to flip flop but Entry not directional");
            }
        }

        /**
         * Get the Entry object for this row. You <b>must</b> call this to
         * get the result of converting the Entry from Debit to Credit or vise
         * versa if its direction has been flopped.
         * 
         * @return the (possibly new) Entry.
         */
        Entry getEntry() {
            state.setParentLedger(accountPicker.getLedger());

            return state;
        }
    }

    protected void ok() {
        /*
         * Basic data guards.
         */

        /*
         * Now extract the Entries and re-add them to the Transaction
         */

        Widget[] children = eB.getChildren();
        for (int i = 0; i < children.length; i++) {
            if (children[i] instanceof EntryRow) {
                EntryRow row = (EntryRow) children[i];
                Entry e = row.getEntry();

                if (e.getAmount().getNumber() == 0) {
                    ModalDialog dialog = new ModalDialog(
                            window,
                            "Amount blank",
                            "It doesn't make much sense for there to be an Entry in a Transaction with a 0.00 Amount. "
                                    + "Either set the Entry to the appropriate value, or remove the Entry.",
                            MessageType.WARNING);
                    dialog.run();
                    t.getEntries().clear();
                    row.amountEntry.grabFocus();
                    return;
                }

                if (e.getParentLedger() == null) {
                    ModalDialog dialog = new ModalDialog(
                            window,
                            "Account not set",
                            "You need to pick the Account and Ledger to which each Entry in the Transaction belongs.",
                            MessageType.WARNING);
                    dialog.run();
                    t.getEntries().clear();
                    row.accountPicker.grabFocus();
                    return;
                }

                t.addEntry(e);
            }
        }

        if (t.getEntries().size() == 0) {
            ModalDialog dialog = new ModalDialog(
                    window,
                    "No Entries!",
                    "The whole point of a GenericTransaction is to let you enter Debits and Credits manually. "
                            + "You must have at least two, and the net Debit and net Credit balance must be equal.",
                    MessageType.WARNING);
            dialog.run();
            addButton.grabFocus();
            return;
        }

        /*
         * Unlike other EditorWindows which just take care of matters, the
         * biggest problem that might be present is that the Transaction has
         * an unequal Debit and Credit balance. TransactionCommand checks
         * this, but better to check it explicitly here as a guard.
         */

        if (!t.isBalanced()) {
            ModalDialog dialog = new ModalDialog(window, "Transaction not balanced!",
                    "Any accounting Transaction must have have an equal Debit and Credit value. "
                            + "You must either change the Amounts you've entered, "
                            + "change a Debit to a Credit (or vice versa), "
                            + "or add another Entry to bring the Transaction into balance.",
                    MessageType.WARNING);
            dialog.run();
            /*
             * re-clear the Entries set.
             */
            t.getEntries().clear();
            present();
            return;
        }

        /*
         * Carry on:
         */
        super.ok();
    }
}
