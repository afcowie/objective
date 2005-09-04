/*
 * DatePicker.java
 * 
 * See LICENCE file for usage and redistribution terms
 * Copyright (c) 2005 Operational Dynamics
 */
package accounts.ui;

import java.text.ParseException;

import org.gnu.gdk.KeySymbol;
import org.gnu.gtk.Button;
import org.gnu.gtk.Calendar;
import org.gnu.gtk.Entry;
import org.gnu.gtk.GtkStockItem;
import org.gnu.gtk.HBox;
import org.gnu.gtk.IconSize;
import org.gnu.gtk.Image;
import org.gnu.gtk.Label;
import org.gnu.gtk.Widget;
import org.gnu.gtk.Window;
import org.gnu.gtk.event.ButtonEvent;
import org.gnu.gtk.event.ButtonListener;
import org.gnu.gtk.event.CalendarEvent;
import org.gnu.gtk.event.CalendarListener;
import org.gnu.gtk.event.EntryEvent;
import org.gnu.gtk.event.EntryListener;
import org.gnu.gtk.event.KeyEvent;
import org.gnu.gtk.event.KeyListener;

import accounts.domain.Datestamp;

/**
 * A date picker widget, customized for use within the ObjectiveAccounts
 * application. Dates are converted to a Datestamp object which can be queried.
 * 
 * @author Andrew Cowie
 */
public class DatePicker extends HBox
{
	private Datestamp			_date				= null;

	private static Datestamp	_lastSelectedDate	= null;

	static {
		_lastSelectedDate = new Datestamp();
		_lastSelectedDate.setAsToday();
	}

	private Entry				_entry				= null;
	private Button				_pick				= null;
	private DatePickerPopup		_popup				= null;

	public DatePicker() {
		super(false, 3);
		_date = (Datestamp) _lastSelectedDate.clone();

		_entry = new Entry();
		_entry.setWidth(9);
		_entry.setText(_date.toString());

		// _pick = new Button("Pick", false);
		// _pick = new Button(new GtkStockItem("stock_calendar-view-month"));
		// GtkStockItem stock = new GtkStockItem("stock_calendar-view-month");
		// Image icon = new Image(stock, IconSize.BUTTON);
		// _pick = new Button("stock_calendar-view-month");

		_pick = new Button();
		Image icon = new Image(GtkStockItem.INDEX, IconSize.BUTTON);
		Label label = new Label("Pick", false);
		HBox box = new HBox(false, 1);

		box.packStart(icon, false, false, 0);
		box.packStart(label, false, false, 0);
		_pick.add(box);

		packStart(_entry, true, true, 0);
		packStart(_pick, false, false, 0);

		_pick.addListener(new ButtonListener() {
			public void buttonEvent(ButtonEvent event) {
				if (event.getType() == ButtonEvent.Type.CLICK) {
					if (_popup == null) {
						_popup = new DatePickerPopup("datepicker", "share/DatePickerPopup.glade");
					}
					_popup.present();
				}
			}
		});

		_entry.addListener(new EntryListener() {
			public void entryEvent(EntryEvent event) {
				Datestamp eval = (Datestamp) _date.clone();
				try {
					eval.setDate(_entry.getText());
					_date = eval;
				} catch (ParseException pe) {
					return;
				}

				if (event.getType() == EntryEvent.Type.CHANGED) {
					//
				} else if (event.getType() == EntryEvent.Type.ACTIVATE) {
					_entry.setText(_date.toString());
					_entry.setCursorPosition(9);
				}
			};
		});
	}

	/**
	 * A Window (constructed from a glade file) containing the Calendar Widget,
	 * and listeners to catch appropriate keystrokes.
	 */
	class DatePickerPopup extends GladeWindow
	{
		private Calendar	_calendar	= null;

		public DatePickerPopup(String which, String filename) {
			super(which, filename);

			_calendar = (org.gnu.gtk.Calendar) _glade.getWidget("calendar");
			_calendar.addListener(new CalendarListener() {
				public void calendarEvent(CalendarEvent event) {
					if (event.getType() == CalendarEvent.Type.DAY_SELECTED_DOUBLE_CLICK) {
						applySelection();
					}
				}
			});

			_window.addListener(new KeyListener() {
				public boolean keyEvent(KeyEvent event) {
					int key = event.getKeyval();
					if (key == KeySymbol.Escape.getValue()) {
						_window.hide();
						return true;
					} else if (key == KeySymbol.Home.getValue() || key == KeySymbol.t.getValue()) {
						_date.setAsToday();
						present();
						return true;
					} else if (key == KeySymbol.Return.getValue()) {
						applySelection();
						return true;
					} else {
						// pass through the keystroke
						return false;
					}
				}
			});
		}

		private void applySelection() {
			java.util.Calendar cal = _calendar.getDate();
			_window.hide();
			_date.setDate(cal);
			_entry.setText(_date.toString());
		}

		/*
		 * Overrides of inherited methods -----------------
		 */

		public void present() {
			java.util.Calendar cal = java.util.Calendar.getInstance();
			cal.setTime(_date.getDate());

			_calendar.selectDay(cal.get(java.util.Calendar.DAY_OF_MONTH));
			_calendar.selectMonth(cal.get(java.util.Calendar.MONTH), cal.get(java.util.Calendar.YEAR));

			super.present();
		}

		/**
		 * Raise the window that popped the picker.
		 */
		protected void hideHook() {
			/*
			 * So annoying. You'd think you should be able to cast from Widget
			 * to Window here, but it blows ClassCastException.
			 */
			Widget w = _entry.getToplevel();
			Window top = new Window(w.getHandle());
			top.present();
		}
		
		/**
		 * Only hide, don't destroy. More to the point, override the default
		 * return of false.
		 */
		public boolean deleteHook() {
			_window.hide();
			return true;
		}
	}

	/*
	 * Getters and Setters --------------------------------
	 */
	public Datestamp getDate() {
		return _date;
	}

	public void setDate(Datestamp date) {
		_date = date;
		_entry.setText(_date.toString());
		_entry.setCursorPosition(9);
	}
}