/*
 * AmountDisplay.java
 * 
 * See LICENCE file for usage and redistribution terms
 * Copyright (c) 2006 Operational Dynamics
 */
package accounts.ui;

import org.gnu.gtk.HBox;
import org.gnu.gtk.Label;

import accounts.domain.Amount;

/**
 * Display an Amount. This widget wraps and delegates a Label, and:
 * <ul>
 * <li>Right aligns
 * <li>Pads the Label so that it vertically aligns with data in a column of
 * AmountEntry boxes.
 * </ul>
 * The implementation extends HBox so that the widget grows without swelling the
 * underlying Label as space is given to it.
 * 
 * @author Andrew Cowie
 * @see accounts.ui.AmountEntry the complementary mutator widget.
 */
public class AmountDisplay extends HBox
{
	private transient Amount	amount;

	private Label				amount_Label;

	/**
	 * Instantiate a new Label widget to display an Amount.
	 */
	public AmountDisplay() {
		super(false, 0);

		amount_Label = new Label("");
		amount_Label.setAlignment(1.0, 0.5);
		amount_Label.setWidthChars(10);
		amount_Label.setMaxWidthChars(10);

		/*
		 * Numbers on screen aren't much use if you can't copy them!
		 */
		// FIXME needs libgtk-java 2.8.5
		// amount_Label.setCanFocus(false);
		amount_Label.setSelectable(true);
		amount_Label.setBooleanProperty("can-focus", false);

		/*
		 * The following [2 in setPadding() and 5 in packStart()] pads the Label
		 * similarly to the AmountEntry, so that things space equally in a
		 * column of Amount{Entry,Label} widgets.
		 */
		amount_Label.setPadding(0, 2);
		this.packStart(amount_Label, false, false, 5);
	}

	/**
	 * Set or update the Amount displayed by this Widget.
	 * 
	 * @param a
	 *            The (not-null) Amount object that you wish to display.
	 */
	public void setAmount(Amount a) {
		if (a == null) {
			throw new IllegalArgumentException();
		}
		this.amount = a;
		amount_Label.setText(amount.getValue());
	}
}
