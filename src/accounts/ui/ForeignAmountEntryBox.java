/*
 * ForeignAmountEntryBox.java
 * 
 * See LICENCE file for usage and redistribution terms
 * Copyright (c) 2005 Operational Dynamics
 */
package accounts.ui;

import generic.util.Debug;

import java.util.HashMap;

import org.gnu.gtk.Entry;
import org.gnu.gtk.HBox;
import org.gnu.gtk.Label;
import org.gnu.gtk.event.ComboBoxEvent;
import org.gnu.gtk.event.ComboBoxListener;
import org.gnu.gtk.event.EntryEvent;
import org.gnu.gtk.event.EntryListener;
import org.gnu.gtk.event.FocusEvent;
import org.gnu.gtk.event.FocusListener;

import accounts.client.ObjectiveAccounts;
import accounts.domain.Books;
import accounts.domain.Currency;
import accounts.domain.ForeignAmount;

public class ForeignAmountEntryBox extends HBox
{
	private ForeignAmount		_foreignAmount;

	private Currency			_home;
	private Currency			_lastCurrency;

	private Entry				_faceValueEntry;
	private Entry				_rateEntry;
	private Entry				_homeValueEntry;
	private CurrencySelector	_currencySelector;
	private Label				_homeCodeLabel;

	private static HashMap		_lastRates;

	static {
		_lastRates = new HashMap();
	}

	public ForeignAmountEntryBox() {
		super(false, 3);

		Books root = ObjectiveAccounts.store.getBooks();
		_home = root.getHomeCurrency();

		if (_lastCurrency == null) {
			_lastCurrency = _home;
		}

		_foreignAmount = new ForeignAmount("0.00", _lastCurrency, "1.0");

		_faceValueEntry = new Entry();
		_faceValueEntry.setWidth(8);
		_faceValueEntry.setAlignment(1.0f);
		packStart(_faceValueEntry, true, true, 0);

		_currencySelector = new CurrencySelector();
		_currencySelector.setCurrency(_lastCurrency);
		packStart(_currencySelector, false, false, 0);

		_rateEntry = new Entry();
		_rateEntry.setWidth(8);
		packStart(_rateEntry, true, true, 0);

		_homeValueEntry = new Entry();
		_homeValueEntry.setWidth(8);
		_homeValueEntry.setAlignment(1.0f);
		packStart(_homeValueEntry, true, true, 0);

		_homeCodeLabel = new Label(_home.getCode());
		packStart(_homeCodeLabel, false, false, 0);

		_faceValueEntry.addListener(new EntryListener() {
			public void entryEvent(EntryEvent event) {
				if (event.getType() == EntryEvent.Type.CHANGED) {
					final String text = _faceValueEntry.getText();
					Debug.print("listeners", "faceValueEntry CHANGED " + text);
					if (!_faceValueEntry.hasFocus()) {
						return;
					}
					if (text.equals("")) {
						return;
					}

					try {
						_foreignAmount.setForeignValue(text);
					} catch (NumberFormatException nfe) {
						/*
						 * If the user's imput is garbage (ie, a second decimal
						 * place, alpha characters) then a) that
						 * setForeignValue() call won't have changed anything,
						 * and b) just escape out of here. Worse case (the user
						 * doesn't do anything smart about it), when focus
						 * leaves or activate happens the diplayed value will be
						 * reverted.
						 */
						return;
					}
					_rateEntry.setText(_foreignAmount.getRate());
					_homeValueEntry.setText(_foreignAmount.getValue());
				}

				if (event.getType() == EntryEvent.Type.ACTIVATE) {
					final String text = _foreignAmount.getForeignValue();
					_faceValueEntry.setText(text);
					_faceValueEntry.setCursorPosition(text.length());
				}
			}
		});

		_faceValueEntry.addListener(new FocusListener() {
			public boolean focusEvent(FocusEvent event) {
				if (event.getType() == FocusEvent.Type.FOCUS_OUT) {
					_faceValueEntry.setText(_foreignAmount.getForeignValue());
					_faceValueEntry.selectRegion(0, 0);
				}
				return false;
			};
		});

		_currencySelector.addListener(new ComboBoxListener() {
			public void comboBoxEvent(ComboBoxEvent event) {
				if (event.getType() == ComboBoxEvent.Type.CHANGED) {
					Debug.print("listeners", "currencySelector CHANGED");
					Currency cur = _currencySelector.getCurrency();
					_foreignAmount.setCurrency(cur);

					String rate = (String) _lastRates.get(cur);
					if (rate == null) {
						rate = "1.0";
					}

					_foreignAmount.setRate(rate);
					_rateEntry.setText(_foreignAmount.getRate());
					_homeValueEntry.setText(_foreignAmount.getValue());

					grayOut();
				}
			}
		});

		_rateEntry.addListener(new EntryListener() {
			public void entryEvent(EntryEvent event) {
				if (event.getType() == EntryEvent.Type.CHANGED) {
					final String text = _rateEntry.getText();
					Debug.print("listeners", "rateEntry CHANGED " + text);
					if (!_rateEntry.hasFocus()) {
						return;
					}
					if (text.equals("")) {
						return;
					}

					try {
						_foreignAmount.setRate(text);
					} catch (NumberFormatException nfe) {
						// see comment above
						return;
					}
					_homeValueEntry.setText(_foreignAmount.getValue());
					_lastRates.put(_currencySelector.getCurrency(), _foreignAmount.getRate());
				}

				if (event.getType() == EntryEvent.Type.ACTIVATE) {
					final String original = _rateEntry.getText();
					final String text = _foreignAmount.getRate();
					_rateEntry.setText(text);
					_rateEntry.setCursorPosition(original.length());
				}
			}
		});

		/*
		 * If focus leaves or user presses enter, then apply the formatting
		 * inherent in ForeignAmount's rate String.
		 */
		_rateEntry.addListener(new FocusListener() {
			public boolean focusEvent(FocusEvent event) {
				if (event.getType() == FocusEvent.Type.FOCUS_OUT) {
					_rateEntry.setText(_foreignAmount.getRate());
					_rateEntry.selectRegion(0, 0);
				}
				return false;
			};
		});

		_homeValueEntry.addListener(new EntryListener() {
			public void entryEvent(EntryEvent event) {
				if (event.getType() == EntryEvent.Type.CHANGED) {
					final String text = _homeValueEntry.getText();
					Debug.print("listeners", "homeValueEntry CHANGED " + text);
					if (!_homeValueEntry.hasFocus()) {
						return;
					}
					if (text.equals("")) {
						return;
					}

					try {
						_foreignAmount.setValue(text);
					} catch (NumberFormatException nfe) {
						// see comment above
						return;
					}
					_rateEntry.setText(_foreignAmount.getRate());
					_lastRates.put(_currencySelector.getCurrency(), _foreignAmount.getRate());
				}

				if (event.getType() == EntryEvent.Type.ACTIVATE) {
					final String text = _foreignAmount.getValue();
					_homeValueEntry.setText(text);
					_homeValueEntry.setCursorPosition(text.length());
				}
			}
		});

		_homeValueEntry.addListener(new FocusListener() {
			public boolean focusEvent(FocusEvent event) {
				if (event.getType() == FocusEvent.Type.FOCUS_OUT) {
					_homeValueEntry.setText(_foreignAmount.getValue());
					_homeValueEntry.selectRegion(0, 0);
				}
				return false;
			};
		});

		grayOut();
	}

	/*
	 * Utility methods ------------------------------------
	 */

	private void grayOut() {
		if (_currencySelector.getCurrency() == _home) {
			_rateEntry.setSensitive(false);
			_homeValueEntry.setSensitive(false);
			_homeCodeLabel.setSensitive(false);
		} else {
			_rateEntry.setSensitive(true);
			_homeValueEntry.setSensitive(true);
			_homeCodeLabel.setSensitive(true);
		}
	}

	/*
	 * Override inherited methods -------------------------
	 */

	public void grabFocus() {
		_faceValueEntry.grabFocus();
	}
}
