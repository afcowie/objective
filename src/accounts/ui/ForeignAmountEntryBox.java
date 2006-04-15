/*
 * ForeignAmountEntryBox.java
 * 
 * See LICENCE file for usage and redistribution terms
 * Copyright (c) 2005-2006 Operational Dynamics
 */
package accounts.ui;

import generic.util.Debug;

import java.util.HashMap;

import org.gnu.gdk.Color;
import org.gnu.gtk.Entry;
import org.gnu.gtk.HBox;
import org.gnu.gtk.Label;
import org.gnu.gtk.StateType;
import org.gnu.gtk.event.ComboBoxEvent;
import org.gnu.gtk.event.ComboBoxListener;
import org.gnu.gtk.event.EntryEvent;
import org.gnu.gtk.event.EntryListener;
import org.gnu.gtk.event.FocusEvent;
import org.gnu.gtk.event.FocusListener;

import accounts.client.ObjectiveAccounts;
import accounts.domain.Amount;
import accounts.domain.Books;
import accounts.domain.Currency;
import accounts.domain.ForeignAmount;

public class ForeignAmountEntryBox extends HBox
{
	private ForeignAmount		foreignAmount;

	private Currency			home;
	private Currency			lastCurrency;

	private AmountEntry			faceValue_AmountEntry;
	private Entry				rate_Entry;
	private AmountEntry			homeValue_AmountEntry;
	private CurrencySelector	foreign_CurrencySelector;
	private Label				homeCode_Label;

	private static HashMap		lastRates;

	static {
		lastRates = new HashMap();
	}

	public ForeignAmountEntryBox() {
		super(false, 3);

		Books root = ObjectiveAccounts.store.getBooks();
		home = root.getHomeCurrency();

		if (lastCurrency == null) {
			lastCurrency = home;
		}

		foreignAmount = new ForeignAmount("0.00", lastCurrency, "1.0");

		faceValue_AmountEntry = new AmountEntry();
		faceValue_AmountEntry.setAmount(new Amount()); // dummy
		packStart(faceValue_AmountEntry, true, true, 0);

		foreign_CurrencySelector = new CurrencySelector();
		foreign_CurrencySelector.setCurrency(lastCurrency);
		packStart(foreign_CurrencySelector, false, false, 0);

		rate_Entry = new Entry();
		rate_Entry.setWidth(8);
		packStart(rate_Entry, true, true, 0);

		homeValue_AmountEntry = new AmountEntry();
		packStart(homeValue_AmountEntry, true, true, 0);

		homeCode_Label = new Label(home.getCode());
		packStart(homeCode_Label, false, false, 0);

		faceValue_AmountEntry.addListener(new ChangeListener() {
			public void userChangedData() {
				/*
				 * faceValue_AmountEntry's Amount is a dummy object; we just use
				 * it as a placeholder to get a validated value String.
				 */
				foreignAmount.setForeignValue(faceValue_AmountEntry.getAmount());
				Debug.print("listeners", "faceValueEntry changed() " + foreignAmount.toString());

				rate_Entry.setText(foreignAmount.getRate());
				homeValue_AmountEntry.setAmount(foreignAmount);
			}

		});

		foreign_CurrencySelector.addListener(new ComboBoxListener() {
			public void comboBoxEvent(ComboBoxEvent event) {
				if (event.getType() == ComboBoxEvent.Type.CHANGED) {
					Debug.print("listeners", "currencySelector CHANGED");
					Currency cur = foreign_CurrencySelector.getCurrency();
					foreignAmount.setCurrency(cur);

					String rate = (String) lastRates.get(cur);
					if (rate == null) {
						rate = "1.0";
					}

					foreignAmount.setRate(rate);
					rate_Entry.setText(foreignAmount.getRate());
					homeValue_AmountEntry.setAmount(foreignAmount);

					grayOut();
				}
			}
		});

		rate_Entry.addListener(new EntryListener() {
			public void entryEvent(EntryEvent event) {
				if (event.getType() == EntryEvent.Type.CHANGED) {
					final String text = rate_Entry.getText();
					Debug.print("listeners", "rateEntry CHANGED " + text);
					if (!rate_Entry.hasFocus()) {
						return;
					}
					if (text.equals("")) {
						return;
					}

					try {
						foreignAmount.setRate(text);
						rate_Entry.setTextColor(StateType.NORMAL, Color.BLACK);
					} catch (NumberFormatException nfe) {
						rate_Entry.setTextColor(StateType.NORMAL, Color.RED);
						return;
					}
					homeValue_AmountEntry.setAmount(foreignAmount);
					lastRates.put(foreign_CurrencySelector.getCurrency(), foreignAmount.getRate());
				}

				if (event.getType() == EntryEvent.Type.ACTIVATE) {
					final String original = rate_Entry.getText();
					final String text = foreignAmount.getRate();
					rate_Entry.setText(text);
					rate_Entry.setCursorPosition(original.length());
				}
			}
		});

		/*
		 * If focus leaves or user presses enter, then apply the formatting
		 * inherent in ForeignAmount's rate String.
		 */
		rate_Entry.addListener(new FocusListener() {
			public boolean focusEvent(FocusEvent event) {
				if (event.getType() == FocusEvent.Type.FOCUS_OUT) {
					rate_Entry.setText(foreignAmount.getRate());
					rate_Entry.selectRegion(0, 0);
					rate_Entry.setTextColor(StateType.NORMAL, Color.BLACK);
				}
				return false;
			};
		});

		homeValue_AmountEntry.addListener(new ChangeListener() {
			public void userChangedData() {
				Debug.print("listeners", "homeValueEntry CHANGED " + foreignAmount.getValue());

				rate_Entry.setText(foreignAmount.getRate());
				lastRates.put(foreign_CurrencySelector.getCurrency(), foreignAmount.getRate());

				/*
				 * No need to set faceValue - after all, changing the home value
				 * manually only imacts the effective exchange rate.
				 */
			}
		});

		grayOut();
	}

	/*
	 * Utility methods ------------------------------------
	 */

	private void grayOut() {
		if (foreign_CurrencySelector.getCurrency() == home) {
			rate_Entry.setSensitive(false);
			homeValue_AmountEntry.setSensitive(false);
			homeCode_Label.setSensitive(false);
		} else {
			rate_Entry.setSensitive(true);
			homeValue_AmountEntry.setSensitive(true);
			homeCode_Label.setSensitive(true);
		}
	}

	/*
	 * Override inherited methods -------------------------
	 */

	public void grabFocus() {
		faceValue_AmountEntry.grabFocus();
	}

	/*
	 * Getters and Setters --------------------------------
	 */

	/**
	 * The whole point of the Widget is, of course, to generate a ForeignAmount.
	 * 
	 * @return the ForeignAmount as specified by the user.
	 */
	public ForeignAmount getForeignAmount() {
		return foreignAmount;
	}

	public void setForeignAmount(ForeignAmount amount) {
		this.foreignAmount = amount;
		faceValue_AmountEntry.setValue(foreignAmount.getForeignValue());
		foreign_CurrencySelector.setCurrency(foreignAmount.getCurrency());
		rate_Entry.setText(foreignAmount.getRate());
		homeValue_AmountEntry.setAmount(foreignAmount);
	}

}
