/*
 * GenericTransactionEditorWindow.java
 * 
 * See LICENCE file for usage and redistribution terms
 * Copyright (c) 2006 Operational Dynamics
 */
package accounts.ui;

import generic.ui.Align;
import generic.ui.EditorWindow;
import generic.ui.ModalDialog;
import generic.ui.TwoColumnTable;

import java.util.Iterator;

import org.gnu.gtk.Button;
import org.gnu.gtk.GtkStockItem;
import org.gnu.gtk.HBox;
import org.gnu.gtk.Label;
import org.gnu.gtk.MessageType;
import org.gnu.gtk.ReliefStyle;
import org.gnu.gtk.SizeGroup;
import org.gnu.gtk.SizeGroupMode;
import org.gnu.gtk.VBox;
import org.gnu.gtk.Widget;
import org.gnu.gtk.event.ButtonEvent;
import org.gnu.gtk.event.ButtonListener;

import accounts.domain.Amount;
import accounts.domain.Books;
import accounts.domain.Credit;
import accounts.domain.Currency;
import accounts.domain.Debit;
import accounts.domain.Entry;
import accounts.domain.ForeignAmount;
import accounts.domain.GenericTransaction;
import accounts.services.Command;
import accounts.services.CommandNotReadyException;
import accounts.services.PostTransactionCommand;
import accounts.services.UpdateTransactionCommand;

/**
 * Create or Edit a GenericTransaction (often known in other accounting programs
 * as a "General Ledger transaction"). This has an arbitrary number of Entries,
 * Credit or Debit as specified by the user, against arbitrarily specified
 * accounts.
 * 
 * @see accounts.domain.GenericTransaction
 * @author Andrew Cowie
 */
/*
 * We take a slightly different approach here, forming the Transaction object as
 * we go and using its methods to store data, rather than instantiating it in
 * ok().
 */
public class GenericTransactionEditorWindow extends EditorWindow
{
	/**
	 * The transaction object we are building up or editing.
	 */
	private GenericTransaction	t					= null;

	/**
	 * Is this a Transaction object being edited?
	 */
	private boolean				editing				= false;

	/*
	 * UI elements
	 */

	private DatePicker			dP					= null;
	private VBox				eB					= null;
	private TextEntry			dE					= null;

	private SizeGroup			accountSizeGroup	= null;
	private SizeGroup			amountSizeGroup		= null;
	private SizeGroup			flopSizeGroup		= null;
	private SizeGroup			addRemoveSizeGroup	= null;

	/**
	 * Instantiate a new GenericTransaction so the user can start filling it in.
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
		super();

		TwoColumnTable table = new TwoColumnTable(2);

		if (id == 0) {
			t = new GenericTransaction();
			setTitle("Enter a new Generic Transaction");
		} else {
			t = (GenericTransaction) store.fetchByID(id);
			setTitle(t.getDescription());
			editing = true;
		}

		/*
		 * We use block to hide all the silly variables (especially the Labels),
		 * and to help group things logically connected (ie, left right
		 * elements) so they're visually distinct in the code.
		 */
		{
			Label tL = new Label("<big><b>Generic Transaction</b></big>");
			tL.setUseMarkup(true);
			tL.setAlignment(0.0, 0.5);
			top.packStart(tL, false, false, 3);
		}
		{
			Label dL = new Label("Date:");
			dL.setAlignment(1.0, 0.5);
			table.attach(dL, Align.LEFT);
			dP = new DatePicker();
			dP.setDate(t.getDate());
			table.attach(dP, Align.RIGHT);
		}
		{
			Label dL = new Label("Description");
			dL.setAlignment(1.0, 0.5);
			table.attach(dL, Align.LEFT);
			dE = new TextEntry();
			dE.setText(t.getDescription());
			table.attach(dE, Align.RIGHT);
		}

		top.packStart(table, false, false, 0);

		accountSizeGroup = new SizeGroup(SizeGroupMode.HORIZONTAL);
		amountSizeGroup = new SizeGroup(SizeGroupMode.HORIZONTAL);
		flopSizeGroup = new SizeGroup(SizeGroupMode.HORIZONTAL);
		addRemoveSizeGroup = new SizeGroup(SizeGroupMode.HORIZONTAL);

		{
			HBox headings;

			headings = new HBox(false, 3);
			Label aL = new Label("Account / Ledger");
			aL.setAlignment(0.0, 0.5);

			Label faL = new Label("Amount");
			faL.setAlignment(0.0, 0.5);

			Label drL = new Label("Debits");
			Label crL = new Label("Credits");
			Label bL = new Label("");

			accountSizeGroup.addWidget(aL);
			amountSizeGroup.addWidget(faL);
			flopSizeGroup.addWidget(drL);
			flopSizeGroup.addWidget(crL);
			addRemoveSizeGroup.addWidget(bL);
			headings.packStart(aL, true, true, 0);
			headings.packStart(faL, false, false, 0);
			headings.packStart(drL, false, false, 0);
			headings.packStart(crL, false, false, 0);
			headings.packStart(bL, false, false, 0);

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

				eI = t.getEntries().iterator();
				/*
				 * Somewhat unusually, we remove the Entris from the Transaction
				 * as we add them to this EditorWindow This allows us to rebuild
				 * the Transaction later with whatever Entries we end up with
				 * (as some may be new objects having flip-flopped).
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

			Button addButton = new Button(GtkStockItem.ADD);
			addButton.addListener(new ButtonListener() {
				public void buttonEvent(ButtonEvent event) {
					if (event.getType() == ButtonEvent.Type.CLICK) {
						addRow(new Debit());
						eB.showAll();
					}
				}
			});

			tailings.packEnd(addButton, false, false, 0);
			eB.packEnd(tailings, true, false, 0);
			addRemoveSizeGroup.addWidget(addButton);
		}
	}

	private void addRow(Entry e) {
		EntryRow row = new EntryRow(e);
		eB.packStart(row, false, false, 0);
	}

	/**
	 * An HBox representing an Entry within the GenericTransaction. Because
	 * these are generic, they can be either Debits or Credits. The UI presented
	 * allows you to toggle between the two. If it is changed from Debit to
	 * Credit or vice versa, then a new Entry object will be instantiated. If it
	 * remains the same, then the original object will be updated.
	 * 
	 * @author Andrew Cowie
	 */
	class EntryRow extends HBox
	{
		/**
		 * Determine whether the current display is Debit or Credit.
		 */
		private Entry					state;

		/**
		 * The reference to the original Entry that was pased to this Row.
		 */
		private Entry					original;
		private Entry					alternate;

		/*
		 * UI elements
		 */

		private AccountPicker			accountPicker	= null;
		// private AmountEntry amountEntry = null;
		private ForeignAmountEntryBox	amountEntry		= null;
		private Button					flopButton		= null;
		private Label					sideLabel		= null;
		private Button					deleteButton	= null;

		/*
		 * Need a reference so that other
		 */
		private HBox					box;

		EntryRow(Entry e) {
			super(false, 1);

			this.original = e;
			this.state = e;

			if (state instanceof Debit) {
				alternate = new Credit();
			} else if (state instanceof Credit) {
				alternate = new Debit();
			} else {
				throw new IllegalArgumentException("Supplied Entry must be Debit or Credit");
			}
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

			flopButton = new Button();
			flopButton.setRelief(ReliefStyle.NONE);
			sideLabel = new Label("");

			flopSizeGroup.addWidget(sideLabel);
			flopSizeGroup.addWidget(flopButton);

			flipFlop();

			flopButton.addListener(new ButtonListener() {
				public void buttonEvent(ButtonEvent event) {
					if (event.getType() == ButtonEvent.Type.CLICK) {
						box.remove(sideLabel);
						box.remove(flopButton);

						if (state == original) {
							state = alternate;
						} else {
							state = original;
						}

						flipFlop();
					}
				}
			});

			deleteButton = new Button(GtkStockItem.DELETE);
			deleteButton.addListener(new ButtonListener() {
				public void buttonEvent(ButtonEvent event) {
					if (event.getType() == ButtonEvent.Type.CLICK) {
						eB.remove(box);
					}
				}
			});

			box.packEnd(deleteButton, false, false, 0);
			addRemoveSizeGroup.addWidget(deleteButton);
		}

		private void flipFlop() {
			if (state instanceof Debit) {
				sideLabel.setMarkup("<span color='" + Debit.COLOR_NORMAL + "'>Debit</span>");
				box.packStart(sideLabel, false, false, 3);
				box.packStart(flopButton, false, false, 3);
			} else if (state instanceof Credit) {
				sideLabel.setMarkup("<span color='" + Credit.COLOR_NORMAL + "'>Credit</span>");
				box.packStart(flopButton, false, false, 3);
				box.packStart(sideLabel, false, false, 3);
			} else {
				throw new IllegalStateException("Trying to flip flop but Entry not directional");
			}
		}

		/**
		 * Get the Entry object for this row. You <b>must</b> call this to get
		 * the result of converting the Entry from Debit to Credit or vise versa
		 * if its direction has been flopped.
		 * 
		 * @return the (possibly new) Entry.
		 */
		Entry getEntry() {
			state.setAmount(amountEntry.getForeignAmount());
			state.setParentLedger(accountPicker.getLedger());

			return state;
		}
	}

	public void ok() {
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
				t.addEntry(row.getEntry());
			}
		}

		/*
		 * Unlike other EditorWindows which just take care of matters, the
		 * biggest problem that might be present is that the Transaction has an
		 * unequal Debit and Credit balance. TransactionCommand checks this, but
		 * better to check it explicitly here as a guard.
		 */

		if (!t.isBalanced()) {
			ModalDialog dialog = new ModalDialog(
				window,
				"Transaction not balanced!",
				"Any accounting Transaction must have have an equal Debit and Credit value. You must either change the amounts you've entered, or add another Entry to bring the Transaction into balance.",
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
		hide();

		Command c = null;
		if (editing) {
			c = new UpdateTransactionCommand(t);
		} else {
			c = new PostTransactionCommand(t);
		}

		try {
			c.execute(store);
		} catch (CommandNotReadyException cnre) {
			ModalDialog dialog = new ModalDialog(window, "Command Not Ready!", cnre.getMessage(),
				MessageType.ERROR);
			dialog.run();

			/*
			 * Leave the Window open so user can fix, as opposed to calling
			 * cancel()
			 */
			present();
		}
	}
}
