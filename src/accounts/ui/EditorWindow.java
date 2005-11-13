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
 * A great number of UI windows [will] follow the pattern of being either
 * editors, with complementary viewers named in parallel. This branch of
 * sublcasses from GladeWindow denote the editors.
 * <P>
 * EditorWindow subclasses must implement cancel() and ok(), corresponding to
 * the actions taken when buttons by those names are pressed.
 * <P>
 * An API assumption is made that the glade file being used has a VBox called
 * "top_vbox" to which the HButtonBox containing the commit and cancel buttons
 * will be added.
 * 
 * @author Andrew Cowie
 */
public abstract class EditorWindow extends GladeWindow
{
	private Button	_cancel;
	private Button	_ok;
	protected VBox	_top_vbox;

	protected EditorWindow(String which, String filename) {
		super(which, filename);

		_top_vbox = (VBox) _glade.getWidget("top_vbox");
		HButtonBox buttonbox = new HButtonBox();
		buttonbox.setLayout(ButtonBoxStyle.END);

		_cancel = new Button(GtkStockItem.CANCEL);
		_cancel.addListener(new ButtonListener() {
			public void buttonEvent(ButtonEvent event) {
				if (event.getType() == ButtonEvent.Type.CLICK) {
					cancel();
				}
			}
		});
		buttonbox.add(_cancel);

		_ok = new Button(GtkStockItem.OK);
		_ok.addListener(new ButtonListener() {
			public void buttonEvent(ButtonEvent event) {
				if (event.getType() == ButtonEvent.Type.CLICK) {
					ok();
				}
			}
		});
		buttonbox.add(_ok);

		_top_vbox.packEnd(buttonbox, false, false, 0);
		HSeparator sep = new HSeparator();
		_top_vbox.packEnd(sep, false, false, 3);
	}

	/**
	 * The cases of cancel and ok need to be done on a specific basis by the
	 * implementing classes, as is appropriate to their situation.
	 */
	protected void cancel() {
		deleteHook();
	}

	protected void ok() {
		deleteHook();
	}
}