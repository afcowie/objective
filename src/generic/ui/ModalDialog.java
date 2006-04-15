/*
 * ModalDialog.java
 * 
 * See LICENCE file for usage and redistribution terms
 * Copyright (c) 2006 Operational Dynamics
 *
 * Based on xseq.ui.ModalDialog, GPL code originally from the xseq project
 * Copyright (c) 2005 Operational Dynamics
 */
package generic.ui;

import org.gnu.gtk.ButtonsType;
import org.gnu.gtk.DialogFlags;
import org.gnu.gtk.MessageDialog;
import org.gnu.gtk.MessageType;
import org.gnu.gtk.Window;
import org.gnu.gtk.WindowPosition;

/**
 * A simple wrapper, as the MessageDialog class (which is supposedly a
 * convenience) is a pain to use. Also, historically, Dialog.setSecondaryText()
 * was not available.
 * <p>
 * Instantiate the dialog then call run() to show &amp; block, or present() just
 * to show it. It will center on Screen.
 * <p>
 * Example of use:
 * 
 * <pre>
 * ModalDialog error = new ModalDialog(&quot;File not found&quot;, e.getMessage() + &quot;\n&quot; + &quot;Try again?&quot;, MessageType.WARNING);
 * error.run();
 * </pre>
 * 
 * @author Andrew Cowie
 */
public class ModalDialog
{
	MessageDialog	dialog	= null;

	/**
	 * Pop a MessageDialog. Parameters message and subtext are Pango markup
	 * enabled.
	 * 
	 * @param message
	 *            Will be rendered in a larger font as the main [error] message
	 *            text; This will be presented as big and bold by GTK.
	 * @param subtext
	 *            Rendered in normal font, can amplify the message. You can use
	 *            Pango markup here if you wish.
	 * @param type
	 *            Will control the icon used, and the text of the dismiss
	 *            button. The constants (current at GTK 2.8) are
	 *            MessageType.{ERROR, WARNING, INFO, and QUESTION}. We don't do
	 *            anything special with QUESTION for the time being.
	 */
	public ModalDialog(String message, String subtext, MessageType type) {
		ButtonsType buttons;

		if (type == MessageType.INFO) {
			buttons = ButtonsType.OK;
		} else {
			buttons = ButtonsType.CLOSE;
		}

		Window[] windows = Window.listToplevelWindows();

		dialog = new MessageDialog(windows[0], DialogFlags.DESTROY_WITH_PARENT, type, buttons, message, false);
		dialog.hide();
		/*
		 * According to the documentation, setting secondary markup takes care
		 * of making the primary message big and bold - so long as the main
		 * message wasn't set with Pango true. We'll stick with that so that we
		 * get GTK default theming and what not.
		 */
		dialog.setSecondaryMarkup(subtext);
		dialog.setPosition(WindowPosition.CENTER);
	}

	/**
	 * Modally display the dialog. Blocks, as is the behaviour with
	 * Dialog.run(), then destroys after action by the user causes it to return.
	 */
	public void run() {
		dialog.showAll();
		dialog.run();
		dialog.destroy();
	}

	/**
	 * Display the dialog. If you use this form, then you are responsible for
	 * disposing of the Dialog Window with destroy() yourself.
	 */
	public void present() {
		dialog.showAll();
		dialog.present();
	}
}