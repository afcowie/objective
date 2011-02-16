/*
 * ObjectiveAccounts, accounting for small professional services firms.
 *
 * Copyright © 2005-2011 Operational Dynamics Consulting, Pty Ltd
 *
 * The code in this file, and the program it is a part of, is made available
 * to you by its authors as open source software: you can redistribute it
 * and/or modify it under the terms of the GNU General Public License version
 * 2 ("GPL") as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GPL for more details.
 *
 * You should have received a copy of the GPL along with this program. If not,
 * see http://www.gnu.org/licenses/. The authors of this program may be
 * contacted via http://research.operationaldynamics.com/projects/objective/.
 */
package generic.ui;

/*
 * Based on xseq.ui.ModalDialog, GPL code originally from the xseq project
 */

import org.gnome.gtk.ButtonsType;
import org.gnome.gtk.MessageDialog;
import org.gnome.gtk.MessageType;
import org.gnome.gtk.Window;
import org.gnome.gtk.WindowPosition;

/**
 * A simple wrapper, as the {@link org.gnu.gtk.MessageDialog} class (which is
 * supposedly a convenience) is a pain to use. Also, historically,
 * {@link org.gnu.gtk.MessageDialog#setSecondaryText(String)
 * setSecondaryText()} was not available.
 * <p>
 * Instantiate the dialog then call {@link #run()} to show &amp; block, or
 * {@link #present()} just to show it. It will center on the screen.
 * <p>
 * Example of use:
 * 
 * <pre>
 * ModalDialog error = new ModalDialog(window, &quot;File not found&quot;, e.getMessage() + &quot;\n&quot; + &quot;Try again?&quot;,
 *         MessageType.WARNING);
 * error.run();
 * </pre>
 * 
 * @author Andrew Cowie
 */
public class ModalDialog
{
    MessageDialog dialog = null;

    /**
     * Pop a MessageDialog. Parameters message and subtext are Pango markup
     * enabled.
     * 
     * @param parentWindow
     *            the GTK Window from which you launched this dialog. This is
     *            passed to MessageDialog as the window that will be returned
     *            to focus after the Dialog is dismissed.
     * @param message
     *            Will be rendered in a larger font as the main [error]
     *            message text; This will be presented as big and bold by GTK.
     * @param subtext
     *            Rendered in normal font, can amplify the message. You can
     *            use Pango markup here if you wish.
     * @param type
     *            Will control the icon used, and the text of the dismiss
     *            button. The constants available (current as at GTK 2.8) are
     *            MessageType.{{@link MessageType#ERROR ERROR},
     *            {@link MessageType#WARNING WARNING},
     *            {@link MessageType#INFO INFO}, and
     *            {@link MessageType#QUESTION QUESTION} . We don't do anything
     *            special with QUESTION for the time being.
     */
    public ModalDialog(Window parentWindow, String message, String subtext, MessageType type) {
        ButtonsType buttons;

        if (type == MessageType.INFO) {
            buttons = ButtonsType.OK;
        } else {
            buttons = ButtonsType.CLOSE;
        }

        dialog = new MessageDialog(parentWindow, true, type, buttons, message);
        dialog.hide();
        /*
         * According to the documentation, setting secondary markup takes care
         * of making the primary message big and bold - so long as the main
         * message wasn't set with Pango true. We'll stick with that so that
         * we get GTK default theming and what not.
         */
        dialog.setSecondaryText(subtext, true);
        dialog.setPosition(WindowPosition.CENTER);
    }

    /**
     * Modally display the dialog. Blocks, as is the behaviour with
     * Dialog.run(), then destroys after action by the user causes it to
     * return.
     */
    public void run() {
        dialog.showAll();
        dialog.run();
        dialog.hide();
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
