/*
 * EditorWindow.java
 * 
 * See LICENCE file for usage and redistribution terms
 * Copyright (c) 2005 Operational Dynamics
 */
package accounts.ui;

import org.gnu.gtk.Button;
import org.gnu.gtk.ButtonBoxStyle;
import org.gnu.gtk.GtkStockItem;
import org.gnu.gtk.HButtonBox;
import org.gnu.gtk.HSeparator;
import org.gnu.gtk.VBox;
import org.gnu.gtk.event.ButtonEvent;
import org.gnu.gtk.event.ButtonListener;

/**
 * A great number of UI windows [will] follow the pattern of being both viewers
 * and editors for some underlying, that is, they are used to view a transaction
 * or other interaction with the underlying system.
 * <P>
 * The assumption is made that the glade file being used has a VBox called
 * "top_vbox" to which the HButtonBox containing the commit and cancel buttons
 * will be added.
 * 
 * @author Andrew Cowie
 */
public abstract class EditorWindow extends GladeWindow
{
	private Button	_close;
	private Button	_edit;
	private Button	_cancel;
	private Button	_apply;
	protected VBox	_top_vbox;

	protected EditorWindow(String which, String filename) {
		super(which, filename);

		_top_vbox = (VBox) _glade.getWidget("top_vbox");
		HButtonBox buttonbox = new HButtonBox();
		buttonbox.setLayout(ButtonBoxStyle.END);

		_close = new Button(GtkStockItem.CLOSE);
		_close.addListener(new ButtonListener() {
			public void buttonEvent(ButtonEvent event) {
				if (event.getType() == ButtonEvent.Type.CLICK) {
					close();
				}
			}
		});
		buttonbox.add(_close);

		_edit = new Button(GtkStockItem.EDIT);
		_edit.addListener(new ButtonListener() {
			public void buttonEvent(ButtonEvent event) {
				if (event.getType() == ButtonEvent.Type.CLICK) {
					edit();
				}
			}
		});
		buttonbox.add(_edit);

		_cancel = new Button(GtkStockItem.CANCEL);
		_cancel.addListener(new ButtonListener() {
			public void buttonEvent(ButtonEvent event) {
				if (event.getType() == ButtonEvent.Type.CLICK) {
					cancel();
				}
			}
		});
		_cancel.setSensitive(false);
		buttonbox.add(_cancel);

		_apply = new Button(GtkStockItem.APPLY);
		_apply.addListener(new ButtonListener() {
			public void buttonEvent(ButtonEvent event) {
				if (event.getType() == ButtonEvent.Type.CLICK) {
					apply();
				}
			}
		});
		_apply.setSensitive(false);
		buttonbox.add(_apply);
		_top_vbox.packEnd(buttonbox, false, false, 0);
		HSeparator sep = new HSeparator();
		_top_vbox.packEnd(sep, false, false, 3);
	}

	protected void close() {
		super.deleteHook();
	}

	protected void edit() {
		_close.setSensitive(false);
		_edit.setSensitive(false);
		_cancel.setSensitive(true);
		_apply.setSensitive(true);
	}

	/**
	 * The cases of revert and apply need to be done on a specific basis by the
	 * implementing classes, as is appropriate to their situation.
	 */
	protected void cancel() {
		_close.setSensitive(true);
		_edit.setSensitive(true);
		_cancel.setSensitive(false);
		_apply.setSensitive(false);
	}

	protected void apply() {
		_close.setSensitive(true);
		_edit.setSensitive(true);
		_cancel.setSensitive(false);
		_apply.setSensitive(false);
	}
}